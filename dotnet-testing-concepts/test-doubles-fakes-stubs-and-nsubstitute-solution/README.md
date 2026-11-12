# Test Doubles in .NET: Fakes, Stubs and NSubstitute (Solution)

Official solution for the `test-doubles-fakes-stubs-and-nsubstitute` lab.

## What changed from the starter

- **`InMemoryNotificationLog`** is a hand-written fake: a `HashSet<string>` behind the port. The
  idempotency and retry tests read `Keys` after the act step, so they check what was recorded, not
  which methods were called.
- **`Known(contact)`** configures the directory for `Shipment.CustomerId` only. With
  `Arg.Any<Guid>()` the stub would answer for any id, and a service that looks up the order id
  instead of the customer id would pass every test (verified below).
- **Every service test asserts the `NotificationOutcome` first.** `Received` and `DidNotReceive` come
  second, to check the message went to the right place. A `DidNotReceive` on its own passes for any
  reason that sends nothing: an unknown customer, a wrong id, an exception swallowed somewhere.
- **The SMS test configures `.Returns(true)`.** An unconfigured `Task<bool>` member of a substitute
  returns a completed task with `false`. Without the setup the service falls back to email, and a test
  that only checks `_sms.Received(1)` still passes.
- **The failure test uses a sequence**, `Returns(Task.FromException(...), Task.CompletedTask)`: the
  first call gives a faulted task, the second succeeds, so one test proves "nothing recorded" and
  "the retry sends it".
- **`HttpSmsSenderTests`** (bonus) test the sealed adapter with a hand-written `HttpMessageHandler`
  stub. The request body is compared after deserializing it: `System.Text.Json` escapes `+` as
  `+` by default, so comparing raw JSON strings fails for a correct body. The learning test pins
  down that `Substitute.For<HttpSmsSender>(...)` throws `TypeLoadException` (Castle cannot derive
  from a sealed class).
- **`NSubstitute.Analyzers.CSharp`** is referenced with `PrivateAssets="all"`. It warns at build time
  when a test configures or verifies a member NSubstitute cannot intercept (NS1000, NS1001) and when
  `.Throws` is used on an async member (NS5003).

## Bonus notes

Mutation check, run against this solution (MSTest 4.5.2, NSubstitute 6.2.0). Each change to
`src/Notifications` was applied on its own and then reverted:

| Change to the production code | Tests that fail |
| --- | --- |
| The directory is queried with `shipment.OrderId` | 7 service tests (all that use `Known`) |
| Every SMS counts as delivered (result ignored) | `AnSmsCustomerFallsBackToEmailWhenTheGatewayRefuses` |
| The opt-out check is removed | `ACustomerWhoOptedOutGetsNothing` |
| The log entry is written before delivery | `AFailedEmailIsNotRecordedSoTheRetrySendsIt`, `AnSmsCustomerWithoutPhoneOrEmailCannotBeReached` |
| The log is never checked | `TheSameShipmentNotifiesTheCustomerOnlyOnce` |
| `HttpSmsSender` returns `true` whatever the status | `TheGatewayRefusingTheNumberMeansNotDelivered` |

And the traps, verified the same way (all three leave the suite green, which is the problem):

| Weaker test | What it hides |
| --- | --- |
| `Known` with `Arg.Any<Guid>()` | the lookup by `OrderId` above: 11 of 11 pass |
| SMS test with no `.Returns(true)`, only `_sms.Received(1)` | the outcome is `FellBackToEmail`, the customer got an email |
| Opt-out stub set up for `Guid.NewGuid()`, only `DidNotReceive` asserted | removing the opt-out check: the test still passes, because the customer is "unknown" |
