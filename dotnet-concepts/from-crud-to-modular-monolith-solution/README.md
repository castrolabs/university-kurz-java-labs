# From CRUD to Modular Monolith (Solution)

Official solution for the `from-crud-to-modular-monolith` lab.

## What changed from the starter

- `Catalog.Core` keeps `Game`, `GameStore` and `CatalogModuleApi` `internal`. The only public
  type is `CatalogModule`, with `AddCatalogModule` and `MapCatalogEndpoints`. The host never
  sees anything else.
- `CatalogModuleApi` implements `ICatalogModule` and is registered scoped: other modules ask
  for games through the contract and receive `GameInfoDto`, never the entity.
- `Orders.Core` references `Orders.Contracts` and `Catalog.Contracts` only. With no reference
  to `Catalog.Core`, the compiler (not a wiki page) prevents a `Join` or a call into
  Catalog's store.
- Order lines carry `UnitPrice`, copied when the order is placed. Reading an order is two
  steps: the order and its lines from Orders, then the game names from `ICatalogModule`.
  A later price change in Catalog does not rewrite history.
- The four architecture tests use reflection on the compiled assemblies: no reference from
  `Orders.Core` to `Catalog.Core`, no `*.Contracts` assembly referencing a `*.Core`, and
  only the module class exported from each `Core`.

## Bonus notes

- A project reference nobody uses is not recorded in the assembly metadata, so the
  reference test fires when code actually uses a type from the other module, which is the
  moment that matters.
- `OrderLine.GameId` is a plain `int` with no foreign key to Catalog's data. With a real
  database it stays that way across schemas (`catalog` and `orders`), so extracting a
  module later does not start with removing constraints.

## Running

```bash
dotnet test
```
