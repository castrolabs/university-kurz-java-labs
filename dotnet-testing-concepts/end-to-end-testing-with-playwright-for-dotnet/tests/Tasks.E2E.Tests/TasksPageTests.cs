using static Microsoft.Playwright.Assertions;

namespace Tasks.E2E.Tests;

[TestClass]
[TestCategory("E2E")]
public sealed class TasksPageTests : E2ETest
{
    private TasksPage Tasks => new(Page);

    // Use web-first assertions (Expect(locator).ToHave...Async), never Assert on a value you read once:
    // they retry until the page catches up. Page.WaitForTimeoutAsync is not allowed.

    // TODO-01: The API answers after 300 ms, so right after OpenAsync the page says "Loading tasks...".
    [TestMethod]
    public async Task ShouldShowLoadingAndThenAnEmptyList()
    {
        // Expect the loading text visible, then hidden, then OpenCount "0 open".
        await Task.CompletedTask;
        Assert.Fail("TODO-01: write this test.");
    }

    // TODO-02
    [TestMethod]
    public async Task ShouldAddATask()
    {
        // Add "Write the report": one item containing that text, OpenCount "1 open".
        await Task.CompletedTask;
        Assert.Fail("TODO-02: write this test.");
    }

    // TODO-03
    [TestMethod]
    public async Task ShouldExplainAnEmptyTitleAndKeepTheListEmpty()
    {
        // Submit "   ". The alert says "Title is required.". Careful: the list is hidden until the first load
        // finishes, so "zero items" is true for the wrong reason unless you waited for "0 open" first.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    // TODO-04
    [TestMethod]
    public async Task ShouldMarkATaskAsDone()
    {
        // Add a task, check its DoneBox, then expect it checked and OpenCount "0 open".
        await Task.CompletedTask;
        Assert.Fail("TODO-04: write this test.");
    }

    // TODO-05
    [TestMethod]
    public async Task ShouldDeleteATask()
    {
        // Add "One" and "Two", delete "One", expect only "Two". Between the two adds, wait for the first
        // item to appear: the page clears the input when the first POST returns, and a title typed before
        // that is erased. Run it without the wait and read the failure and the trace.
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    // TODO-06
    [TestMethod]
    public async Task ShouldKeepTheTasksAfterAReload()
    {
        // Add a task, Page.ReloadAsync(), expect it still there (the store lives in the server).
        await Task.CompletedTask;
        Assert.Fail("TODO-06: write this test.");
    }
}
