# Blazor QuickGrid and Virtualize

## Goal

Build a paginated, sortable data grid with `QuickGrid` (first with an `IQueryable`, then
with an `ItemsProvider` that only asks a service for the slice on screen) and a huge
list with `Virtualize`. In .NET 11 the grid keeps its page and sort in the URL, so it
works in static server-side rendering without interactivity.

## Prerequisites

- Blazor components and static SSR basics
- xUnit, bUnit and `WebApplicationFactory`
- **.NET 11 SDK** (11.0.100 or later). The projects target `net11.0`; the tests use bUnit
  2.15.2 and `Microsoft.AspNetCore.Mvc.Testing` 11.0.0, and the web project uses
  `Microsoft.AspNetCore.Components.QuickGrid` 11.0.0, all from nuget.org.

## Task

The `BlazorGridLab` project has three pages: `/products` (grid over an in-memory
`IQueryable` of 23 products), `/remote` (grid with an `ItemsProvider` backed by
`ProductService`) and `/feed` (a list of 10,000 items). Today the grid has no paging or
sorting, the provider loads everything and the feed renders 10,000 rows. The tests mix
`WebApplicationFactory` (HTML of the static page, including links) and bUnit (the
components with a fake JS runtime). No browser is involved.

## Instructions

- TODO-00: in `Products.razor`, add `Pagination` (5 per page) and a `Paginator`, make Name
  and Price sortable, Name the default sort column. `/products` shows Name01..Name05.
- TODO-01: nothing to add beyond TODO-00; `?page=2&sort=Name&direction=desc` must show
  Name18..Name14. Note that the page number in the URL is 1-based.
- TODO-02: nothing to add either; the headers and the pager must be plain links
  (`<a href=...>`), so the grid works without a circuit.
- TODO-03: in `Remote.razor`, paginate (5 per page) and make the provider use
  `request.StartIndex`, `request.Count` and `request.GetSortByProperties()`.
- TODO-04: in `Feed.razor`, replace the loop with `Virtualize` (`ItemsProvider`,
  `ItemSize` 20, `OverscanCount` 3, `ItemComparer`).
- TODO-05: the bUnit test for the grid passes once TODO-00 is done; read it to see why it
  needs `WaitForAssertion`.

## Verification

Run `dotnet test`. The tests never run the browser scroll logic of `Virtualize`: to see
the window move, run `dotnet run --project src/BlazorGridLab`, open `/feed` and watch the DOM.
