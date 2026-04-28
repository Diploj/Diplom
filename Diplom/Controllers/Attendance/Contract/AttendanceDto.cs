namespace Diplom.Controllers.Attendance.Contract;

public class AttendanceDto
{
    public int? Id { get; set; }
    public int LectureId { get; set; }
    public int StudentId { get; set; }      
    public bool Attended { get; set; }
}