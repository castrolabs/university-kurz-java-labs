using System.ComponentModel.DataAnnotations;

namespace ValidationLab;

public sealed class NotFutureAttribute : ValidationAttribute
{
    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext) =>
        value is DateOnly date && date > DateOnly.FromDateTime(DateTime.UtcNow)
            ? new ValidationResult("Release date cannot be in the future.")
            : ValidationResult.Success;
}
