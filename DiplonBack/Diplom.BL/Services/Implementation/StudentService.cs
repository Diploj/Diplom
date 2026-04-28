using System.Linq.Expressions;
using System.Transactions;
using AutoMapper;
using Diplom.BL.Filters;
using Diplom.BL.Models;
using Diplom.BL.Options;
using Diplom.BL.Services.Interface;
using Diplom.Client.Exceptions;
using Diplom.Client.Services;
using Diplom.Data.Entities;
using Diplom.Data.Repository.Interface;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using Serilog;

namespace Diplom.BL.Services.Implementation;

public class StudentService : IStudentService
{
    private readonly UserManager<User> _userRepository;
    private readonly IRepository<Student> _studentRepository;
    private readonly IRepository<Group> _groupRepository;
    private readonly IRepository<Course> _courseRepository;
    private readonly IFaceEmbeddingRepository _faceEmbeddingRepository;
    private readonly IRecognitionClient _recognitionClient;
    private readonly IMinIOService _minioService;
    private readonly DistanceOptions _distanceOptions;
    private readonly MinIOOptions _minioOptions;
    private readonly PhotoOptions _photoOptions;
    private readonly IMapper _mapper;
    private readonly ILogger _logger;

    public StudentService(
        UserManager<User> userRepository,
        IRepository<Student> studentRepository,
        IRepository<Group> groupRepository,
        IRepository<Course> courseRepository,
        IRecognitionClient recognitionClient,
        IFaceEmbeddingRepository faceEmbeddingRepository,
        IMinIOService minioService,
        IOptions<DistanceOptions> distanceOptions,
        IOptions<MinIOOptions> minioOptions,
        IOptions<PhotoOptions> photoOptions,
        IMapper mapper,
        ILogger logger)
    {
        _userRepository = userRepository;
        _studentRepository = studentRepository;
        _groupRepository = groupRepository;
        _courseRepository = courseRepository;
        _recognitionClient = recognitionClient;
        _faceEmbeddingRepository = faceEmbeddingRepository;
        _minioService = minioService;
        _distanceOptions = distanceOptions.Value;
        _minioOptions = minioOptions.Value;
        _photoOptions = photoOptions.Value;
        _mapper = mapper;
        _logger = logger;
    }

    public async Task<IEnumerable<StudentModel>> GetAll()
    {
        return _mapper.Map<IEnumerable<StudentModel>>(await _studentRepository.GetAllAsync(s => true));
    }

    public async Task<IEnumerable<StudentModel>> GetFiltered(StudentFilter filter)
    {
        Expression<Func<Student, bool>> expression = s =>
            (filter.GroupId == null || s.GroupId == filter.GroupId) &&
            (filter.Name == null || s.User.Name.Contains(filter.Name)) &&
            (filter.Surname == null || s.User.Surname.Contains(filter.Surname)) &&
            (filter.Email == null || s.User.Email.Contains(filter.Email)) &&
            (filter.Patronymic == null || s.User.Patronymic.Contains(filter.Patronymic)) &&
            (filter.StudentIdNumber == null || s.StudentIdNumber == filter.StudentIdNumber);
        return _mapper.Map<IEnumerable<StudentModel>>(await _studentRepository.GetAllAsync(expression));
    }

    public async Task<IEnumerable<StudentModel>> GetAllStudentsByCourse(int courseId)
    {
       var course = await _courseRepository.GetByIdAsync(courseId);
       if (course == null)
       {
           _logger.Warning($"Course {courseId} not found");
           throw new KeyNotFoundException("Course not found");
       }
       return _mapper.Map<IEnumerable<StudentModel>>(course.Groups.SelectMany(g => g.Students).ToList());
    }

    public async Task<StudentModel?> GetById(int studentId)
    {
        return _mapper.Map<StudentModel>(await _studentRepository.GetByIdAsync(studentId));
    }

    public async Task<IdentityResult> Create(StudentModel student, string password)
    {
        try
        {
            using (var scope = new TransactionScope(TransactionScopeAsyncFlowOption.Enabled))
            {
                var userEntity = _mapper.Map<User>(student);
                var studentEntity = _mapper.Map<Student>(student);
                var result = await _userRepository.CreateAsync(userEntity, password);
                if (result.Succeeded)
                {
                    studentEntity.UserId = userEntity.Id;
                    await _studentRepository.AddAsync(studentEntity);
                    await _userRepository.UpdateSecurityStampAsync(userEntity);
                    await _userRepository.AddToRoleAsync(userEntity, "Student");
                }

                scope.Complete();
                return result;
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IdentityResult> Update(StudentModel student, string password)
    {
        try
        {
            var userEntity = _mapper.Map<User>(student);
            var studentEntity = _mapper.Map<Student>(student);
            userEntity.PasswordHash = _userRepository.PasswordHasher?.HashPassword(userEntity, password);
            await _studentRepository.UpdateAsync(studentEntity);
            var result = await _userRepository.UpdateAsync(userEntity);
            return result;
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task SetGroup(int studentId, int groupId)
    {
        try
        {

            var studentEntity = await _studentRepository.GetByIdAsync(studentId);
            if (await _groupRepository.ExistsAsync(groupId))
            {
                studentEntity.GroupId = groupId;
                await _studentRepository.UpdateAsync(studentEntity);
            }
            else
            {
                _logger.Warning($"Group with id {groupId} does not found");
                throw new KeyNotFoundException("Group does not found");
            }
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task AddPhoto(int studentId, IFormFile photo)
    {
        try
        {
            if (await _faceEmbeddingRepository.GetCount(studentId) >= _photoOptions.MaxCountOfPhotos)
            {
                _logger.Warning($"Student {studentId} already has to many photos");
                throw new ArgumentException($"Student {studentId} already has to many photos");
            }
            using var originalStream = new MemoryStream();
            await photo.OpenReadStream().CopyToAsync(originalStream);
            originalStream.Position = 0;
            using var pythonStream = new MemoryStream();
            await originalStream.CopyToAsync(pythonStream);
            pythonStream.Position = 0;
            var embedding = await _recognitionClient.ExtractEmbeddingsAsync(pythonStream);
            if (embedding.Count == 1)
            {
                var dist = await _faceEmbeddingRepository.GetFurtherStudentsEmbeddingAsync(embedding.First(),
                    studentId);
                if (dist == default || dist < _distanceOptions.Distance)
                {
                    originalStream.Position = 0;
                    var url = await _minioService.UploadStudentPhotoAsync(studentId, originalStream, photo.ContentType);
                    FaceEmbedding faceEmbeddingEntity = new FaceEmbedding()
                        { StudentId = studentId, Embedding = embedding.First(), PhotoUrl = url };
                    await _faceEmbeddingRepository.AddAsync(faceEmbeddingEntity);
                }
                else
                {
                    _logger.Warning($"Face on photo don't looks like student");
                    throw new Exception($"Face on photo don't looks like student");
                }
            }
            else
            {
                _logger.Warning("On photos must contains one face");
                throw new ArgumentException("On photos must contains one face");
            }
        }
        catch (ClientException e)
        {
            _logger.Error(e.Message);
            throw;
        }
        catch (DbUpdateException e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<IList<FaceImageModel>> GetPhotoUrls(int studentId)
    {
        try
        {
            var faces = await _faceEmbeddingRepository.GetAllAsync(x => x.StudentId == studentId);
            return faces.Select(x => new FaceImageModel(){Id = x.Id,ImageUrl = x.PhotoUrl}).ToList();
        }
        catch (Exception e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task<(Stream,string)> GetPhoto(string imageUrl)
    {
        try
        {
            return await _minioService.GetFileAsync(_minioOptions.BucketStudentsPhoto,imageUrl);
        }
        catch (Exception e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }

    public async Task DeletePhoto(int studentId, int photoId)
    {
        try
        {
            var photo = await _faceEmbeddingRepository.GetByIdAsync(photoId);
            if (photo == null)
            {
                _logger.Warning($"Photo with id {photoId} does not found");
                throw new KeyNotFoundException("Photo does not found");
            }
            if (studentId != photo.StudentId)
            {
                _logger.Warning($"You can't delete another student's photo");
                throw new KeyNotFoundException("You can't delete another student's photo");
            }
            await _minioService.DeleteFileAsync(_minioOptions.BucketStudentsPhoto, photo.PhotoUrl);
            await _faceEmbeddingRepository.DeleteAsync(photo.Id);
        }
        catch (Exception e)
        {
            _logger.Error(e.Message);
            throw;
        }
    }
}