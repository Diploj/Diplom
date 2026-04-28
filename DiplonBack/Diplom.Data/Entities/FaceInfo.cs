using System.ComponentModel.DataAnnotations;

namespace Diplom.Data.Entities;

public class FaceInfo
{
    public int Id { get; set; }
    
    public int StudentId { get; set; }
    
    [MaxLength(500)]
    public string? PhotoUrl { get; set; }
    
    
    public bool IsActive { get; set; } = true;
}