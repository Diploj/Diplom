using Diplom.Controllers.Group.Contract;
using FluentValidation;

namespace Diplom.Validators.Group;

public class GroupUpdateValidator : AbstractValidator<GroupUpdateRequest>
{
    public GroupUpdateValidator()
    {
        RuleFor(g => g.Number)
            .GreaterThan(0)
            .WithMessage("Number must be greater than 0");
        RuleFor(g=>g.Year)
            .GreaterThan(0)
            .WithMessage("Year must be greater than 0");
    }
}