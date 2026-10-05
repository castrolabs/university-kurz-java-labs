# From CRUD to Modular Monolith

## Goal

Turn two features that reach into each other's internals (a game catalog and an order
flow) into two modules that talk only through a contract, with the compiler and a few
architecture tests enforcing the boundary.

## Prerequisites

- C# project references and access modifiers (`public` vs `internal`)
- Minimal APIs and dependency injection
- .NET 10 SDK

## Task

The solution already has the module layout:

```text
src/
  Host/                        -> GameStore.Host, only composes the modules
  Modules/
    Catalog/Catalog.Contracts  -> ICatalogModule, GameInfoDto (the public face)
    Catalog/Catalog.Core       -> games, store, endpoints
    Orders/Orders.Contracts    -> request and response shapes
    Orders/Orders.Core         -> orders, store, endpoints
```

It works, but the boundary is fake: everything is `public`, and `Orders.Core` references
`Catalog.Core` and calls Catalog's `GameStore` directly. An order also stores `0` as the
price instead of the price that was paid. Fix the structure one step at a time and let the
compiler tell you what was reaching in.

## Instructions

Complete the following TODOs:

- TODO-00: Make every type in `Catalog.Core` `internal` except `CatalogModule`. The solution
  stops compiling: `Orders.Core` was using those types. That is the compiler enforcing the
  boundary, and the next TODOs fix it.
- TODO-01: Implement `CatalogModuleApi.GetGamesAsync` and register `ICatalogModule` in
  `AddCatalogModule`.
- TODO-02: In `Orders.Core`, use `ICatalogModule` instead of `GameStore`, and in
  `Orders.Core.csproj` replace the reference to `Catalog.Core` with `Catalog.Contracts`.
- TODO-03: Make every type in `Orders.Core` `internal` except `OrdersModule`.
- TODO-04: Copy the game's price into `OrderLine.UnitPrice` when the order is placed.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-05 (optional): Add an architecture test that fails when any `*.Contracts` assembly
  exposes a type that is not a record or an interface.
- TODO-06 (optional): Add `Orders.Core` -> `Catalog.Core` back in the csproj "temporarily" and
  see which test catches it before the code even uses the type.
- TODO-07 (optional): Replace the in-memory stores with one PostgreSQL schema per module
  (`catalog` and `orders`), each with its own `DbContext` and migrations history table.
