using System.Collections.Concurrent;

namespace Dispatch;

// Messages from the depot arrive on another thread. The desk reads them from here.
public sealed class Inbox
{
    private readonly BlockingCollection<string> _messages = new();

    public void Post(string message) => _messages.Add(message);

    // Waits for the next message for as long as it takes.
    public string Receive() => _messages.Take();

    // Waits at most `timeout`. Returns false when nothing arrived in time.
    public bool TryReceive(TimeSpan timeout, out string? message) => _messages.TryTake(out message, timeout);
}
