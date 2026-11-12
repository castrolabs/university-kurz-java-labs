using Microsoft.Agents.AI;
using Microsoft.Agents.AI.Workflows;
using Microsoft.Extensions.AI;

namespace WorkflowLab;

public static class AgentPipelines
{
    private static AIAgent Create(IChatClient client, string name, string instructions) =>
        client.AsAIAgent(new ChatClientAgentOptions
        {
            Id = name.ToLowerInvariant(),
            Name = name,
            ChatOptions = new ChatOptions { Instructions = instructions, Temperature = 0, Seed = 42 },
        });

    public static Workflow BuildWriterEditor(IChatClient client) =>
        AgentWorkflowBuilder.BuildSequential(
            Create(client, "Writer", "Write one short sentence about the topic."),
            Create(client, "Editor", "Rewrite the user's last sentence in at most 6 words. Output only the rewrite."));

    public static async Task<IReadOnlyList<ChatMessage>> RunAsync(Workflow workflow, string topic)
    {
        await using StreamingRun run = await InProcessExecution.RunStreamingAsync(
            workflow, new List<ChatMessage> { new(ChatRole.User, topic) });
        await run.TrySendMessageAsync(new TurnToken(emitEvents: true));

        IReadOnlyList<ChatMessage> result = [];
        await foreach (WorkflowEvent e in run.WatchStreamAsync())
        {
            if (e is WorkflowOutputEvent { Data: List<ChatMessage> messages }) result = messages;
        }
        return result;
    }
}
