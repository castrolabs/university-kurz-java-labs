using System.ComponentModel.DataAnnotations;

public sealed class UniqueEmailAttribute : AsyncValidationAttribute
{
    // IsValid is abstract in AsyncValidationAttribute, so it must exist.
    // The Minimal API calls IsValidAsync instead.
    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext) =>
        throw new NotSupportedException("The async path is the one that runs.");

    protected override Task<ValidationResult?> IsValidAsync(
        object? value, ValidationContext validationContext, CancellationToken cancellationToken)
    {
        // TODO-01: Ask IUserRepository (take it from the validation context, it is a DI service)
        // whether the email exists. If it does, return a ValidationResult with the message
        // "Email already registered." and the member name; otherwise ValidationResult.Success.
        // Pass the cancellationToken to the repository call.
        return Task.FromResult<ValidationResult?>(ValidationResult.Success);
    }
}
