using Diplom.Data.Entities;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using Npgsql.EntityFrameworkCore.PostgreSQL.Storage.Internal.Mapping;
using NpgsqlTypes;
namespace Diplom.Data;

public class AppDbContext: IdentityDbContext<User,IdentityRole<int>, int>
{
    public DbSet<Student> Students { get; set; }
    public DbSet<Lector> Lectors { get; set; }
    public DbSet<Group> Groups { get; set; }
    public DbSet<Subject> Subjects { get; set; }
    public DbSet<Course> Courses{ get; set; }
    public DbSet<Lecture> Lectures { get; set; }
    public DbSet<Attendance> Attendances { get; set; }
    public DbSet<FaceEmbedding> FaceEmbeddings { get; set; }
    
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) {}

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);
        
        builder.Entity<Student>()
            .HasKey(s => s.UserId);
        builder.Entity<Lector>()
            .HasKey(l => l.UserId);
        builder.Entity<Group>()
            .HasKey(g => g.Id);
        builder.Entity<Course>()
            .HasKey(c =>c.Id);
        builder.Entity<Subject>()
            .HasKey(s => s.Id);
        builder.Entity<Lecture>()
            .HasKey(l => l.Id);
        builder.Entity<Attendance>()
            .HasKey(a => a.Id);
        builder.Entity<FaceEmbedding>()
            .HasKey(f => f.Id);

        
        builder.Entity<Student>()
            .HasOne(s => s.User)
            .WithOne()
            .HasForeignKey<Student>(s => s.UserId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Entity<Student>()
            .HasOne(s => s.Group)
            .WithMany(g => g.Students)
            .HasForeignKey(s => s.GroupId)
            .OnDelete(DeleteBehavior.SetNull);
        
        builder.Entity<Lector>()
            .HasOne(l => l.User)
            .WithOne()
            .HasForeignKey<Lector>(l => l.UserId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Entity<Group>()
            .HasMany(g => g.Courses)
            .WithMany(c => c.Groups);
        
        builder.Entity<Group>()
            .HasMany(g => g.Lectures)
            .WithMany(l => l.Groups);

        builder.Entity<Course>()
            .HasOne(c => c.Subject)
            .WithMany(s => s.Courses)
            .HasForeignKey(ct => ct.SubjectId);

        builder.Entity<Course>()
            .HasOne(c => c.Lector)
            .WithMany(l => l.Courses)   
            .HasForeignKey(c => c.LectorId);
        
        builder.Entity<Lecture>()
            .HasOne(l => l.Course)
            .WithMany(c => c.Lectures)
            .HasForeignKey(l => l.CourseId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Entity<Attendance>()
            .HasOne(a => a.Lecture)
            .WithMany(l => l.Attendances)
            .HasForeignKey(a => a.LectureId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Entity<Attendance>()
            .HasOne(a => a.Student)
            .WithMany(s => s.Attendances)
            .HasForeignKey(a => a.StudentId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Entity<FaceEmbedding>()
            .HasOne(f => f.Student)
            .WithMany(s => s.FaceEmbeddings)
            .HasForeignKey(f => f.StudentId)
            .OnDelete(DeleteBehavior.Cascade);

        
        builder.Entity<User>().HasIndex(u => u.Email).IsUnique();
        builder.Entity<Student>().HasIndex(s => s.StudentIdNumber).IsUnique();
        builder.Entity<Attendance>().HasIndex(a => new { a.StudentId, a.LectureId }).IsUnique();
    }
        
}