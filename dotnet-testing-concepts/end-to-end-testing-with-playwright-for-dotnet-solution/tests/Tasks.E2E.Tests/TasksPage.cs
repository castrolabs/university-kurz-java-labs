using Microsoft.Playwright;

namespace Tasks.E2E.Tests;

// Page Object: the only place that knows how the page is built. Locators are by role, label and test id,
// the way a user finds things, not by CSS class or position.
public sealed class TasksPage(IPage page)
{
    public ILocator Items => page.GetByRole(AriaRole.List, new() { Name = "Task list" }).GetByRole(AriaRole.Listitem);
    public ILocator Loading => page.GetByText("Loading tasks...");
    public ILocator Error => page.GetByRole(AriaRole.Alert);
    public ILocator OpenCount => page.GetByTestId("open-count");
    public ILocator DoneBox(string title) => page.GetByLabel($"Done: {title}", new() { Exact = true });

    public async Task OpenAsync() => await page.GotoAsync("/");

    public async Task AddAsync(string title)
    {
        await page.GetByLabel("New task", new() { Exact = true }).FillAsync(title);
        await page.GetByRole(AriaRole.Button, new() { Name = "Add task" }).ClickAsync();
    }

    public async Task DeleteAsync(string title) =>
        await page.GetByRole(AriaRole.Button, new() { Name = $"Delete {title}" }).ClickAsync();
}
