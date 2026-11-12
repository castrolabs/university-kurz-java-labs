using System.ComponentModel.DataAnnotations;

public sealed class UniqueEmailAttribute : AsyncValidationAttribute
{
    // IsValid is abstract in AsyncValidationAttribute, so it must exist.
    // The Minimal API calls IsValidAsync instead.
    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext) =>
        throw new NotSupportedException("The async path is the one that runs.");

    protected override async Task<ValidationResult?> IsValidAsync(
        object? value, ValidationContext validationContext, CancellationToken cancellationToken)
    {
        var repository = validationContext.GetRequiredService<IUserRepository>();
        var exists = await repository.EmailExistsAsync((string)value!, cancellationToken);
        return exists
            ? new ValidationResult("Email already registered.", [validationContext.MemberName ?? "Email"])
            : ValidationResult.Success;
    }
}
