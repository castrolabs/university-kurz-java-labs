using Microsoft.Playwright;

namespace Tasks.E2E.Tests;

// TODO-00: Page Object. The only class allowed to know how src/Tasks.Web/wwwroot/index.html is built.
// Find things the way a user does (role, label, text, test id), not by CSS class or position.
// Open index.html and write each member:
//   Items          the <li> elements of the list named "Task list" (role list, then role listitem)
//   Loading        the text "Loading tasks..."
//   Error          the element with role alert
//   OpenCount      the element with data-testid="open-count"
//   DoneBox(title) the checkbox labelled "Done: <title>" (Exact = true)
//   OpenAsync()    go to "/" (the context has a BaseURL)
//   AddAsync(t)    fill the input labelled "New task", click the button named "Add task"
//   DeleteAsync(t) click the button named "Delete <title>"
public sealed class TasksPage(IPage page)
{
    public ILocator Items => throw new NotImplementedException("TODO-00");
    public ILocator Loading => throw new NotImplementedException("TODO-00");
    public ILocator Error => throw new NotImplementedException("TODO-00");
    public ILocator OpenCount => throw new NotImplementedException("TODO-00");
    public ILocator DoneBox(string title) => throw new NotImplementedException("TODO-00");

    public Task OpenAsync() => throw new NotImplementedException("TODO-00");
    public Task AddAsync(string title) => throw new NotImplementedException("TODO-00");
    public Task DeleteAsync(string title) => throw new NotImplementedException("TODO-00");
}
