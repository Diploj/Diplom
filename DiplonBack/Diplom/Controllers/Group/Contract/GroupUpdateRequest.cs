namespace Diplom.Controllers.Group.Contract;

public class GroupUpdateRequest
{
    public int Id { get; set; }
    public int Number { get; set; }
    public int Year { get; set; }
    public DateTime CreateDate { get; set; }
}