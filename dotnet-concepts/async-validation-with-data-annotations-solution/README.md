# Async Validation with Data Annotations (Solution)

Official solution for the `async-validation-with-data-annotations` lab. It needs the .NET 11 SDK.

## What changed from the starter

- `AddValidation()` in `Program.cs`. Without it the attributes are metadata nobody reads and every request is accepted.
- `UniqueEmailAttribute.IsValidAsync` gets `IUserRepository` from `validationContext.GetRequiredService`, passes the `CancellationToken` and returns a `ValidationResult` that names the member.
- `Order.ValidateAsync` yields one `ValidationResult` against `End` when it is before `Start`.

## Why it is shaped this way

- `AsyncValidationAttribute.IsValid` is abstract and `IAsyncValidatableObject` inherits `IValidatableObject`, so the sync method has to exist. Here both throw, and the tests pass, which shows the Minimal API calls only the async ones.
- On the same property, a failing sync attribute (`[EmailAddress]`) stops the async one (`[UniqueEmail]`) from running, so the repository is not called for a malformed email. Different properties are independent: a missing `Name` and a taken `Email` come back together.
- The object-level rule runs only after the property rules pass. With `Quantity = 0` the answer has `Quantity` and no `End`.
- The token passed to `IsValidAsync` is cancelled when the client disconnects, so the lookup does not outlive the request.
- The types are `public`. The .NET 10 trap with internal types (see the Input Validation concept) is a separate thing to check in your own project.

## Running

```bash
dotnet test
```
