public interface IUserRepository
{
    Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken);
}

public sealed class InMemoryUserRepository : IUserRepository
{
    private static readonly HashSet<string> Taken = ["taken@example.com"];

    public async Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken)
    {
        await Task.Delay(5, cancellationToken); // stands in for a database round trip
        return Taken.Contains(email);
    }
}
