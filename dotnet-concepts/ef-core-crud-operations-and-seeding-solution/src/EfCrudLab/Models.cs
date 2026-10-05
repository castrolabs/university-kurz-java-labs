namespace EfCrudLab;

public class Genre
{
    public int Id { get; set; }
    public required string Name { get; set; }
}

public class Game
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public int GenreId { get; set; }
    public Genre? Genre { get; set; }
    public decimal Price { get; set; }
    public DateOnly ReleaseDate { get; set; }
}

public record CreateGameDto(string Name, int GenreId, decimal Price, DateOnly ReleaseDate);

public record GameDetailsDto(int Id, string Name, int GenreId, decimal Price, DateOnly ReleaseDate);

public record GameSummaryDto(int Id, string Name, string Genre, decimal Price, DateOnly ReleaseDate);
