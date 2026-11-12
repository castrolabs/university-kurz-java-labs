using Microsoft.Agents.AI.Workflows;

namespace WorkflowLab;

public sealed record Ticket(string Text, string Category);

[SendsMessage(typeof(Ticket))]
public sealed partial class Intake() : Executor("Intake")
{
    [MessageHandler]
    private ValueTask<Ticket> HandleAsync(string text, IWorkflowContext context)
        => ValueTask.FromResult(new Ticket(text, Classify(text)));

    private static string Classify(string text)
    {
        if (text.Contains("invoice", StringComparison.OrdinalIgnoreCase)) return "billing";
        if (text.Contains("error", StringComparison.OrdinalIgnoreCase)) return "technical";
        return "other";
    }
}

[YieldsOutput(typeof(string))]
public sealed partial class TicketHandler(string id) : Executor(id)
{
    [MessageHandler]
    private async ValueTask HandleAsync(Ticket ticket, IWorkflowContext context)
        => await context.YieldOutputAsync($"{this.Id}: {ticket.Text}");
}

public static class TriageWorkflow
{
    public static Workflow Build()
    {
        var intake = new Intake();
        var billing = new TicketHandler("Billing");
        var technical = new TicketHandler("Technical");
        var general = new TicketHandler("General");

        var builder = new WorkflowBuilder(intake);
        builder.AddSwitch(intake, s => s
            .AddCase<Ticket>(t => t!.Category == "billing", billing)
            .AddCase<Ticket>(t => t!.Category == "technical", technical)
            .WithDefault(general));
        return builder.WithOutputFrom(billing, technical, general).Build();
    }

    public static Workflow BuildBillingOnly()
    {
        var intake = new Intake();
        var billing = new TicketHandler("Billing");

        return new WorkflowBuilder(intake)
            .AddEdge<Ticket>(intake, billing, t => t!.Category == "billing")
            .WithOutputFrom(billing)
            .Build();
    }

    public static async Task<IReadOnlyList<string>> RunAsync(Workflow workflow, string text)
    {
        await using Run run = await InProcessExecution.RunAsync(workflow, text);
        return run.NewEvents.OfType<WorkflowOutputEvent>().Select(e => (string)e.Data!).ToList();
    }
}
