namespace DtoLab;

public record GameSummaryDto(int Id, string Name, string Genre, decimal Price, DateOnly ReleaseDate);

public record GameDetailsDto(int Id, string Name, int GenreId, decimal Price, DateOnly ReleaseDate);

public record CreateGameDto(string Name, int GenreId, decimal Price, DateOnly ReleaseDate);

public record UpdateGameDto(string Name, int GenreId, decimal Price, DateOnly ReleaseDate);
