namespace Diplom.Controllers.Attendance.Contract;

public class AddLecturePhotoRequest
{
    public int LectureId { get; set; }
    public IFormFile File { get; set; }
}