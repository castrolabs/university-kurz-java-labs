# Test Data with Builders, Bogus and AutoFixture (Solution)

Official solution for the `test-data-with-builders-bogus-and-autofixture` lab: 11 tests pass on the .NET 10 SDK.

## What changed from the starter

- **`OrderBuilder`** has fixed defaults (literal Guids, one 10.00 line), so every failure reproduces. Each rule test
  names only its fact: `Premium()`, `Worth(500m)`.
- **The boundary test uses exactly 500**, not 600: 600 would still pass if the production check were `> 500`.
- **`Fakes`** (Bogus) calls `UseSeed`, and the order faker gives its customer faker another seed (`seed + 1`).
- **`BogusTests`** proves determinism (same seed, equal lists; another seed, different lists) and asserts the data
  covers both branches before checking the four allowed rates. It does not recompute the rule: that would be a
  copy of the production code.
- **`OrderServiceTests`** uses `Freeze` so the inspected substitute is the injected one. Two tests leave the order
  anonymous because nothing depends on its values; the discount test pins the premium flag and a 100.00 line with
  `with` expressions and keeps everything else anonymous.
- **NU1608** on restore: AutoFixture.AutoNSubstitute 4.18.1 declares NSubstitute below 6.0.0; the tests pass with
  NSubstitute 6.2.0.
