# Blazor SSR State: TempData, Session and CacheView

## Goal

Keep state between requests in a Blazor Web App that uses only static server-side
rendering (no circuit): a flash message across a Post/Redirect/Get with TempData, a
cart in the session, and a cached block with `CacheView`. This lab targets .NET 11,
where `[SupplyParameterFromTempData]`, `[SupplyParameterFromSession]`, `CacheView`
and `BasePath` exist.

## Prerequisites

- Static SSR basics (`EditForm`, `[SupplyParameterFromForm]`, Post/Redirect/Get)
- `WebApplicationFactory` and xUnit
- **.NET 11 SDK** (11.0.100 or later). The projects target `net11.0` and the tests use
  `Microsoft.AspNetCore.Mvc.Testing` 11.0.0 from nuget.org.

## Task

The `BlazorSsrStateLab` project has three pages (`Flash`, `Cart`, `News`). Today the
flash message is lost on redirect, the cart forgets its items, the news block is
rendered on every request and the `<base>` element is missing. The tests call the app
in memory and read the HTML and the `Set-Cookie` headers. Each `HttpClient` keeps its
own cookies, so one client is one browser. No browser is involved.

## Instructions

- TODO-00: in `Flash.razor`, mark `Message` so its value is supplied from TempData.
  After the POST and the redirect, `GET /flash` must show `saved hello` once.
- TODO-01: a second browser must not see another browser's message (this follows from
  TempData being a cookie; make sure TODO-00 passes first).
- TODO-02: in `Cart.razor`, supply `Items` from the session and register the session in
  `Program.cs` (`AddDistributedMemoryCache`, `AddSession`, `UseSession`).
- TODO-03: in `News.razor`, wrap the block in `CacheView` varying by the `category`
  query string, expiring after 5 minutes.
- TODO-04: in `App.razor`, render the `<base href="/" />` with the `BasePath` component.
- Bonus: `ShouldStoreTempDataInSessionWhenConfigured` needs no code. Read the test and
  find the option that moves TempData from a cookie to the session.

## Verification

Run `dotnet test`. To run only the required work use `dotnet test --filter "Category!=Bonus"`.
