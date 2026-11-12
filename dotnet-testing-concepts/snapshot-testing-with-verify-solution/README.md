# Snapshot Testing with Verify (Solution)

Official solution for the `snapshot-testing-with-verify` lab. 10 tests, all green on SDK 10.0.100 and on SDK
11.0.100 (running the `net10.0` project on the .NET 11 runtime), with MSTest 4.5.3 and Verify.MSTest 32.0.0.

## What changed from the starter

- **`InvoiceSnapshotTests`** returns `Verify(invoice)`. The approved file is
  `InvoiceSnapshotTests.TheInvoiceObjectIsTheSnapshot.verified.txt`. `Id` is `Guid_1` and `IssuedAt` is
  `DateTimeOffset_1` with no code from you: Verify scrubs `Guid` and `DateTimeOffset` values by default. `Number`
  is a string, so without a scrubber the file changes on every run.
- **`VerifySetup`** has a `[ModuleInitializer]` with `VerifierSettings.ScrubLinesWithReplace` and one regex,
  `INV-\d{8}-[0-9a-f]{8}` to `INV-{date}-{id}`. Being global, it also cleans the HTML snapshots.
- **`HtmlSnapshotTests`** uses `Verify(html, "html")` and `.ScrubInlineDateTimeOffsets("yyyy-MM-dd HH:mm:ss")`: the
  `Issued ...` line is text, and Verify only scrubs inline dates when told the format.
- **`FailureSnapshotTests`** uses `Throws(...)` with `.IgnoreStackTrace()`. Without it the file contains the
  compiler-generated lambda name, which changes when the code around it changes.
- **`InvariantTests`** has no Verify at all: the total is the subtotal plus the tax for four sizes of order, the
  subtotal is the sum of the lines, and the tax of the coffee order is `1.05` (5.50 times 0.19 is 1.045, rounded
  away from zero).
- **`.gitattributes`** marks `*.verified.txt` and `*.verified.html` as LF with UTF-8, as the Verify docs
  recommend, so the approved files do not change between operating systems. `.gitignore` ignores `*.received.*`.

## Two things that are easy to miss

- **The culture.** The first `.received.html` had `9,90` instead of `9.90`: the machine that wrote this lab uses
  a culture with a decimal comma, and `{value:0.00}` follows the current culture. A snapshot approved on that
  machine fails on a CI agent with another culture. `InvoiceHtml` and the invoice number use
  `CultureInfo.InvariantCulture`. The same culture made `decimal.Parse("0.50")` throw in a first version of the
  invariant tests, which now take the price in cents.
- **The stack trace.** `Throws` snapshots include it by default.

## The Verify version and the license

Verify 33.3.4 (the latest when this was written) with no declaration:

```text
Verify.targets(172,5): SponsorCheck error SC021: No license specified. Package 'Verify' requires a SponsorCheck license property.
```

Measured on SDK 10.0.100 and 11.0.100: the build fails with that error, which lists the options (sponsor,
`SmallRevenue`, `OpenSource`, `MaintainerConsulting`, `LicensedUntil`, `LicenseIgnored`). Then, with 33.3.4:

| Declaration | Result |
| --- | --- |
| `Verify_SponsorshipExemption=OpenSource` and `Verify_SponsorshipExemptionUntil=2027-10` | build passes, 10 tests pass |
| the same with `Verify_SponsorshipExemptionUntil=2028-10` | `SC046: Exemption end date too far in the future ... (maximum 2027-11)` |
| `Verify_SponsorshipLicenseIgnored=true` | build passes with warning `SC023: ... Build is allowed but is in breach of the package license` |

The exemption has to be renewed at least every 12 months, and claiming it is a statement about who uses the
code, which a public lab cannot make for its readers. That is why this lab pins 32.0.0: released 2026-08-26,
before the fee date of 2026-09-01, no SponsorCheck. Source:
https://github.com/VerifyTests/Verify/blob/main/docs/maintenance-fee.md

## Mutation check

Each change below to `src/Invoicing` was tried on its own against this solution (`dotnet test`, then reverted):

| Change to the production code | Tests that fail |
| --- | --- |
| `TaxRate` 0.19 to 0.21 | the three snapshots with a tax, `TheTaxIsRoundedAwayFromZeroToTheCent` |
| `MidpointRounding.ToEven` | `TheInvoiceObjectIsTheSnapshot`, `TheRenderedInvoiceIsAnHtmlFile`, `TheTaxIsRoundedAwayFromZeroToTheCent` |
| `.Skip(1)` on the lines | the three snapshots with a tax, `TheTaxIsRoundedAwayFromZeroToTheCent` |
| no `HtmlEncode` on the customer | `MarkupInNamesIsEscaped` only (a snapshot catches what no assertion was written for) |
| the total without the tax | the three snapshots with a total, `TheTotalIsTheSubtotalPlusTheTax` |
| the number built from 6 characters of the Guid instead of 8 | the three snapshots (the scrubber regex no longer matches: a scrubber is code too) |

## Approving without reading

With `TaxRate` at `0.21m`, 4 tests fail. Renaming every `.received` file to `.verified` (what someone in a hurry
does) turns the three snapshot tests green: 1 failure left, `TheTaxIsRoundedAwayFromZeroToTheCent`, the plain
assertion with a literal worked out by hand. Without it, the wrong tax rate would have shipped with a green suite.
