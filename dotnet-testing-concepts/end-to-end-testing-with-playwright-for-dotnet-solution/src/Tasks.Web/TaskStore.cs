namespace Tasks.Web;

public record TaskItem(int Id, string Title, bool Done);

// One list for the life of the host: a singleton, like the store of the WebApplicationFactory lab.
public sealed class TaskStore
{
    private readonly Lock _gate = new();
    private readonly List<TaskItem> _tasks = [];
    private int _nextId = 1;

    public IReadOnlyList<TaskItem> All()
    {
        lock (_gate) return [.. _tasks];
    }

    public TaskItem Add(string title)
    {
        lock (_gate)
        {
            var task = new TaskItem(_nextId++, title, Done: false);
            _tasks.Add(task);
            return task;
        }
    }

    public TaskItem? Toggle(int id)
    {
        lock (_gate)
        {
            var index = _tasks.FindIndex(t => t.Id == id);
            if (index < 0) return null;
            return _tasks[index] = _tasks[index] with { Done = !_tasks[index].Done };
        }
    }

    public bool Remove(int id)
    {
        lock (_gate) return _tasks.RemoveAll(t => t.Id == id) > 0;
    }

    public void Clear()
    {
        lock (_gate) _tasks.Clear();
    }
}
