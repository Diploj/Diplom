using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Diplom.Data.Entities;

public class FaceEmbedding
{
    public int Id { get; set; }
    
    public int StudentId { get; set; }
    public Student Student { get; set; } = null!;
    
    [MaxLength(500)]
    public string? PhotoUrl { get; set; }
    
    [Required]
    public float[] Embedding { get; set; } = null!;
    
    public bool IsActive { get; set; } = true;
    
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}