# Snapshot Testing with Verify

## Goal

An `InvoiceService` (an order in, an `Invoice` with a Guid, a timestamp, an invoice number and the tax out) and an
`InvoiceHtml` renderer are already fully implemented. Your job is to test them with snapshots, using
`Verify.MSTest`, and on the way learn the approval workflow (`.received` and `.verified` files), how to scrub the
values that change on every run, and which rules a snapshot cannot be trusted with.

## Prerequisites

- C# basics and MSTest basics (`[TestClass]`, `[TestMethod]`, `Assert`)
- .NET 10 SDK
- `mstest-fundamentals-lifecycle-and-testcontext` helps, but is not required

## About the Verify version

This lab pins `Verify.MSTest` to **32.0.0** (published 2026-08-26). Verify 33 and later fail the build with
SponsorCheck error `SC021: No license specified` until the project declares its Open Source Maintenance Fee
status (a sponsorship, a time-bounded exemption, or an explicit opt-out). The fee applies to versions released
after 2026-09-01 and is paid by organizations with a yearly revenue of US$10,000 or more. A public lab cannot
make that declaration for the people who copy it, and an exemption expires within 12 months. 32.0.0 is the last
stable release before the fee and has no check, so the lab keeps building. Everything you practice here works the
same on Verify 33; the concept page explains how to declare the status in a real project. Official page:
https://github.com/VerifyTests/Verify/blob/main/docs/maintenance-fee.md

## Task

Read `src/Invoicing` first (`InvoiceService.cs`, `InvoiceHtml.cs`). Then open `tests/Invoicing.Tests`. Every test
method is already declared with a hint and fails with a `TODO-NN` message until you write it.

The workflow for every snapshot test:

1. Write the test with `await Verify(...)` (or `return Verify(...)`) and run it. The first run **fails** and
   writes `<Class>.<Method>.received.txt` (or `.html`) next to the test source file.
2. Open the `.received` file and read it. This is the step the whole technique depends on.
3. If it is right, approve it: rename `.received` to `.verified`. Run again: green.
4. From then on, any difference fails the test and rewrites the `.received` file for you to compare.

`*.received.*` is ignored by Git, `*.verified.*` is committed. If a diff tool of yours opens on every failure, set
`DiffEngine_Disabled=true`.

## Instructions

Complete the following TODOs in `tests/Invoicing.Tests`:

- TODO-00: `InvoiceSnapshotTests`: snapshot a whole invoice, read the file, approve it, and notice which values
  Verify already scrubbed and which one it did not.
- TODO-01: `VerifySetup`: one scrubber for the invoice number, for the whole assembly.
- TODO-02: `HtmlSnapshotTests`: snapshot the rendered HTML (a `.verified.html` file) and scrub the date in the text.
- TODO-03: `FailureSnapshotTests`: snapshot an exception and drop the stack trace.
- TODO-04: `InvariantTests`: the rules that hold for every input stay plain assertions.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-05 (optional): Mutation check. Change `src/Invoicing` on purpose, one change at a time, and see which tests
  notice: `TaxRate` to `0.21m`, `MidpointRounding.AwayFromZero` to `MidpointRounding.ToEven`, remove the
  `WebUtility.HtmlEncode` around the customer in `InvoiceHtml`, return `subtotal` instead of
  `subtotal + tax` as the total. For each one, read the diff in the `.received` file.
- TODO-06 (optional): Approve without reading. Set `TaxRate` to `0.21m`, run, rename every `.received` file to
  `.verified`, and run again. Which tests are red now? What does that say about a snapshot suite on its own?
  Undo the change and restore the `.verified` files before you go on.
