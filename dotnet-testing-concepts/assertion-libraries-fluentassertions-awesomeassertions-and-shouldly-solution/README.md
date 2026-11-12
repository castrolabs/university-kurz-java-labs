# Assertion Libraries: MSTest 4 Assert, AwesomeAssertions and Shouldly (Solution)

Official solution for the `assertion-libraries-fluentassertions-awesomeassertions-and-shouldly` lab.

## What changed from the starter

- **`MsTestAssertTests`** uses `Assert.AreEqual` (with a `decimal` delta for the rounded discount),
  `Assert.StartsWith`, `Assert.Contains` for both substrings and items, `Assert.HasCount`, `Assert.AreSequenceEqual`
  for the ordered SKUs and `Assert.ThrowsExactly<T>`, which returns the exception so the test can check
  `Message` and `ParamName`. There is no case-insensitive end check, so the summary test uses `Assert.IsTrue`
  with the summary as the message, otherwise a failure would only say "Assert.IsTrue failed". There is no
  equivalence assertion either, so the customer test compares `Name` and `Country` one by one.
- **`AwesomeAssertionsTests`** reads as sentences: `StartWith("INV-001").And.Contain(...)` is one chain,
  `OnlyContain` replaces the `All` check, `Invoking(...).Should().ThrowExactly<T>().WithMessage(...)` replaces
  `ThrowsExactly` plus a message assertion, and the customer test is a single
  `BeEquivalentTo(expected, o => o.Excluding(c => c.Email))`. The library finds MSTest on its own and throws
  MSTest's `AssertFailedException`.
- **`FailureMessageTests`** pins the messages down. `AssertionScope` collects both failures into one exception
  with one line each. Shouldly formats numbers with the current culture (`25,50m` on a machine with a comma
  decimal separator) and prints the source line of the failing call, so the test sets the culture and writes
  the assertion in a block-bodied lambda. Written as an expression-bodied lambda inside
  `Assert.ThrowsExactly<...>(`, the printed source line starts with `Assert.ThrowsExactly<ShouldAssertException>(`.
- **`ShouldlyTests`** (bonus) has the same six checks with Shouldly. `ShouldContain` is case insensitive by
  default in 4.3.0: `ShouldContain("ada lovelace")` passes, and `Case.Sensitive` makes it fail. Its
  `ShouldBeEquivalentTo` compares every member (it reported the e-mail difference), so the customer check
  compares members one by one.
- `Invoice.Summary()` formats the total with the invariant culture, so the summary does not change with the
  machine settings.
