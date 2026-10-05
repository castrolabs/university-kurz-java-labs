# REST Semantics: Status Codes and Idempotency (Solution)

Official solution for the `rest-semantics-status-codes-and-idempotency` lab.

## What changed from the starter

- `GET /games/{id}` returns `Results<Ok<Game>, NotFound>`: the signature now lists
  the two possible answers, and a missing game is a `404`.
- `POST /games` returns `Results.CreatedAtRoute(GetGameEndpointName, new { id }, game)`.
  The anonymous object supplies the route value, so the `Location` header stays
  correct if the path changes.
- `PUT /games/{id}` is `store.Replace(...) ? NoContent : NotFound`. Sending the same
  `PUT` twice leaves the same state, which is what makes it safe to retry.
- `DELETE /games/{id}` removes the game and always answers `204`. The guarantee is
  about the final state (the game does not exist), not about the event.

## Notes

- `POST` is the one verb that is not idempotent: sending the same body twice creates
  two games. A test documents it.
- `AddProblemDetails()` alone does not give a body to a bare `404`. The body appears
  only with `UseStatusCodePages()` (and `UseExceptionHandler()` for unhandled
  exceptions). Try `GET /games/9999` with and without them.

## Running

```bash
dotnet test
```
