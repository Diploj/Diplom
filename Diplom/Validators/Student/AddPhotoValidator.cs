using Diplom.Controllers.User.Contract;
using FluentValidation;

namespace Diplom.Validators.Student;

public class AddPhotoValidator : AbstractValidator<AddPhotoRequest>
{
    private readonly string[] _allowedMimeTypes = ["image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp" ];
    private readonly string[] _allowedExtensions = [".jpg", ".jpeg", ".png", ".gif", ".webp" ];
    private readonly long _maxFileSize = 10 * 1024 * 1024;
    public AddPhotoValidator()
    {
        RuleFor(x => x.File)
            .NotNull().WithMessage("File is required.")
            .Must(f => f.Length > 0).WithMessage("File is empty.");
        
        RuleFor(x => x.File)
            .Must(f => f.Length <= _maxFileSize)
            .WithMessage($"File size must not exceed {_maxFileSize / (1024 * 1024)} MB.");
        
        RuleFor(x => x.File)
            .Must(f => _allowedMimeTypes.Contains(f.ContentType.ToLowerInvariant()))
            .WithMessage($"File type not allowed. Allowed: {string.Join(", ", _allowedMimeTypes)}");
        
        RuleFor(x => x.File)
            .Must(f => _allowedExtensions.Contains(Path.GetExtension(f.FileName).ToLowerInvariant()))
            .WithMessage($"File extension not allowed. Allowed: {string.Join(", ", _allowedExtensions)}");
    }
}