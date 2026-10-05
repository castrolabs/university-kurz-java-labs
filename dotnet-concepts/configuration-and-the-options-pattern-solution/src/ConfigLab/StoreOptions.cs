using System.ComponentModel.DataAnnotations;

namespace ConfigLab;

public sealed class StoreOptions
{
    public const string SectionName = "Store";

    [Required]
    public string? Name { get; set; }

    [Range(1, 100)]
    public int PageSize { get; set; } = 20;
}
