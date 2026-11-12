# EF Core Compiled Models and Startup Time (Solution)

## Goal

Use the compiled model, prove that `OnModelCreating` does not run, measure the startup
phases and detect a stale model. See the starter project for the exercise statement.

## Solution notes

- `UseModel(LabContextModel.Instance)` is all it takes to opt in. The test counts calls to
  `OnModelCreating`: 1 without the compiled model, 0 with it.
- `EnableServiceProviderCaching(false)` is what makes that count repeatable. EF Core keeps
  the built model in its internal service provider, so a second context with the same
  options never builds it again. Do not use that setting in production, it makes every
  context pay for a new service provider.
- The probe separates `db.Model` from the first query. The compiled model removes the
  first cost (building the model from the CLR types). It does not remove the cost of
  compiling the first query, the connection or JIT, so the end-to-end gain is smaller than
  the model-only gain. Measure your own application before adopting compiled models.
- A compiled model has no `OnModelCreating`, so nothing notices when the model changes.
  `IsStale` compares entity types and property names between the generated model and a
  context that builds the model normally. `GrownContext` in the tests adds an entity and
  the check reports it. In a real project run the same comparison in a test, or regenerate
  in the build.
- `dotnet ef dbcontext optimize` also generates `LabContextAssemblyAttributes.cs`. It was
  removed here on purpose, see the starter README.

## Running the Lab

```bash
dotnet test
```
