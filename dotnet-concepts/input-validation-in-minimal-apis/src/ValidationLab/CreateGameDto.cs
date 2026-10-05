namespace ValidationLab;

// TODO-01: Declare the validation rules on the positional parameters:
//   - Name: required, at most 50 characters
//   - GenreId: between 1 and 50
//   - Price: between 1 and 100
// TODO-02: Run the tests after TODO-00 and TODO-01. Some POST tests should pass now
//   but they do not. Find out why this type is never validated and fix it.
internal record CreateGameDto(string Name, int GenreId, decimal Price, DateOnly ReleaseDate);
