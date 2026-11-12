namespace SecurityHeadersLab;

// The persistence-shaped type. IsAdmin must never come from a request body.
public class User
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public bool IsAdmin { get; set; }
}

public record CreateUserRequest(string Name);
