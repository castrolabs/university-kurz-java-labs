namespace UpgradeLab;

public class Customer
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public Address Address { get; set; } = new("", "");
    public List<string> Tags { get; set; } = [];
    public List<Note> Notes { get; set; } = [];
}

public record Address(string Street, string City);

public record Note(string Text);
