using static Microsoft.Playwright.Assertions;

namespace Tasks.E2E.Tests;

[TestClass]
[TestCategory("E2E")]
public sealed class TasksPageTests : E2ETest
{
    private TasksPage Tasks => new(Page);

    [TestMethod]
    public async Task ShouldShowLoadingAndThenAnEmptyList()
    {
        await Tasks.OpenAsync();

        // The API answers after 300 ms. The web-first assertions retry until the page gets there.
        await Expect(Tasks.Loading).ToBeVisibleAsync();
        await Expect(Tasks.Loading).ToBeHiddenAsync();
        await Expect(Tasks.OpenCount).ToHaveTextAsync("0 open");
    }

    [TestMethod]
    public async Task ShouldAddATask()
    {
        await Tasks.OpenAsync();
        await Tasks.AddAsync("Write the report");

        await Expect(Tasks.Items).ToHaveCountAsync(1);
        await Expect(Tasks.Items.First).ToContainTextAsync("Write the report");
        await Expect(Tasks.OpenCount).ToHaveTextAsync("1 open");
    }

    [TestMethod]
    public async Task ShouldExplainAnEmptyTitleAndKeepTheListEmpty()
    {
        await Tasks.OpenAsync();
        await Expect(Tasks.OpenCount).ToHaveTextAsync("0 open"); // the list finished loading
        await Tasks.AddAsync("   ");

        await Expect(Tasks.Error).ToHaveTextAsync("Title is required.");
        await Expect(Tasks.Items).ToHaveCountAsync(0);
    }

    [TestMethod]
    public async Task ShouldMarkATaskAsDone()
    {
        await Tasks.OpenAsync();
        await Tasks.AddAsync("Pay the invoice");
        await Expect(Tasks.OpenCount).ToHaveTextAsync("1 open");

        await Tasks.DoneBox("Pay the invoice").CheckAsync();

        await Expect(Tasks.DoneBox("Pay the invoice")).ToBeCheckedAsync();
        await Expect(Tasks.OpenCount).ToHaveTextAsync("0 open");
    }

    [TestMethod]
    public async Task ShouldDeleteATask()
    {
        await Tasks.OpenAsync();
        await Tasks.AddAsync("One");
        // Wait for the first task to show up before typing the second: the page clears the input when the
        // first POST returns, which would erase a title typed too early (a real race this test once hit).
        await Expect(Tasks.Items).ToHaveCountAsync(1);
        await Tasks.AddAsync("Two");
        await Expect(Tasks.Items).ToHaveCountAsync(2);

        await Tasks.DeleteAsync("One");

        await Expect(Tasks.Items).ToHaveCountAsync(1);
        await Expect(Tasks.Items.First).ToContainTextAsync("Two");
    }

    [TestMethod]
    public async Task ShouldKeepTheTasksAfterAReload()
    {
        await Tasks.OpenAsync();
        await Tasks.AddAsync("Survive a reload");
        await Expect(Tasks.Items).ToHaveCountAsync(1);

        await Page.ReloadAsync();

        await Expect(Tasks.Items).ToHaveCountAsync(1);
    }
}
