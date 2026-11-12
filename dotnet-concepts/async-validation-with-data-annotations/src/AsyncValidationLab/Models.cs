using System.ComponentModel.DataAnnotations;
using System.Runtime.CompilerServices;
using Microsoft.Extensions.Validation;

public record Registration(
    [property: Required] string Name,
    [property: EmailAddress, UniqueEmail] string Email);

public record Order(DateOnly Start, DateOnly End, [property: Range(1, 10)] int Quantity)
    : IAsyncValidatableObject
{
    // The interface inherits IValidatableObject, so the sync method must exist.
    // The Minimal API never calls it when the async one is available.
    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext) =>
        throw new NotSupportedException("The async path is the one that runs.");

    public async IAsyncEnumerable<ValidationResult> ValidateAsync(
        ValidationContext validationContext,
        [EnumeratorCancellation] CancellationToken cancellationToken)
    {
        // TODO-02: When End is before Start, yield a ValidationResult with the message
        // "End must be after Start." attached to the member name "End".
        await Task.CompletedTask;
        yield break;
    }
}
