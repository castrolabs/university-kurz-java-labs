namespace Tenancy.Domain;

public sealed class Tenant
{
    public Guid Id { get; private set; }

    public string Subdomain { get; private set; }

    public Tenant(string subdomain)
    {
        // TODO-00: A subdomain is 1 to 63 letters, digits or hyphens, and cannot start or end
        //   with a hyphen. Trim it, lower-case it and validate it, throwing an ArgumentException
        //   for an invalid value. Keep the generated time-ordered (version 7) Id.
        Id = Guid.CreateVersion7();
        Subdomain = subdomain;
    }

    // TODO-01: Implement Rename. It applies the same rules as the constructor, keeps the Id,
    //   and leaves the current subdomain untouched when the new one is invalid.
    public void Rename(string subdomain) =>
        throw new NotImplementedException("Not implemented yet.");
}
