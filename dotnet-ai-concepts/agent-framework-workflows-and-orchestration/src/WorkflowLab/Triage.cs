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
        // TODO-00: Return "billing" when the text contains "invoice", "technical" when it contains
        //   "error" (ignore case, billing wins when both appear), and "other" otherwise.
        throw new NotImplementedException("Not implemented yet.");
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

        // TODO-01: Start the workflow at intake and route every Ticket to exactly one handler with
        //   AddSwitch: category "billing" to billing, "technical" to technical, anything else to
        //   general (WithDefault). All three handlers are output sources (WithOutputFrom).
        throw new NotImplementedException("Not implemented yet.");
    }

    public static Workflow BuildBillingOnly()
    {
        var intake = new Intake();
        var billing = new TicketHandler("Billing");

        // TODO-02: Connect intake to billing with ONE conditional edge (AddEdge<Ticket> with a
        //   condition): only "billing" tickets pass. There is no default: other tickets must simply
        //   produce no output.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static async Task<IReadOnlyList<string>> RunAsync(Workflow workflow, string text)
    {
        await using Run run = await InProcessExecution.RunAsync(workflow, text);
        return run.NewEvents.OfType<WorkflowOutputEvent>().Select(e => (string)e.Data!).ToList();
    }
}
