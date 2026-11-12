using WorkflowLab;

namespace WorkflowLab.Tests;

public class TriageTests
{
    [Theory]
    [InlineData("Wrong invoice", "Billing: Wrong invoice")]
    [InlineData("Login error 500", "Technical: Login error 500")]
    [InlineData("Say hi", "General: Say hi")]
    public async Task Switch_routes_each_ticket_to_exactly_one_handler(string text, string expected)
    {
        var outputs = await TriageWorkflow.RunAsync(TriageWorkflow.Build(), text);

        Assert.Equal([expected], outputs);
    }

    [Fact]
    public async Task Conditional_edge_delivers_matching_tickets()
    {
        var outputs = await TriageWorkflow.RunAsync(TriageWorkflow.BuildBillingOnly(), "Wrong invoice");

        Assert.Equal(["Billing: Wrong invoice"], outputs);
    }

    [Fact]
    public async Task Conditional_edge_drops_a_ticket_when_no_condition_matches()
    {
        var outputs = await TriageWorkflow.RunAsync(TriageWorkflow.BuildBillingOnly(), "Say hi");

        Assert.Empty(outputs);
    }
}
