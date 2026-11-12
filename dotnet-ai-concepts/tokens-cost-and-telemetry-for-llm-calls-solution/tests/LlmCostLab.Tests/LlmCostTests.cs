using Microsoft.Extensions.AI;
using Microsoft.Extensions.Caching.Distributed;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Options;

namespace LlmCostLab.Tests;

public sealed class LlmCostTests
{
    private static readonly ModelPrice Price = new(InputPerMillionTokens: 3m, OutputPerMillionTokens: 15m);

    private static IDistributedCache NewCache() =>
        new MemoryDistributedCache(Options.Create(new MemoryDistributedCacheOptions()));

    private static ChatMessage User(string text) => new(ChatRole.User, text);

    // --- cost ---

    [Fact]
    public void ShouldPriceInputAndOutputTokensSeparately()
    {
        var usage = new UsageDetails { InputTokenCount = 1_000, OutputTokenCount = 500 };

        Assert.Equal(0.0105m, CostCalculator.Estimate(usage, Price));
    }

    [Fact]
    public void ShouldTreatMissingUsageAndMissingCountsAsZero()
    {
        Assert.Equal(0m, CostCalculator.Estimate(null, Price));
        Assert.Equal(0.003m, CostCalculator.Estimate(new UsageDetails { InputTokenCount = 1_000 }, Price));
    }

    // --- tokens and context ---

    [Fact]
    public void ShouldCountTokensWithTheO200kVocabulary()
    {
        var counter = TokenCounter.O200k();

        Assert.Equal(0, counter.Count(""));
        Assert.Equal(9, counter.Count("Name the capital of France in one word."));
    }

    [Fact]
    public async Task ShouldDifferFromWhatTheModelReports()
    {
        const string prompt = "Name the capital of France in one word.";
        var scripted = new ScriptedChatClient(new UsageDetails { InputTokenCount = 17, OutputTokenCount = 2 });

        var reported = (await scripted.GetResponseAsync(prompt)).Usage!.InputTokenCount;

        // The tokenizer sees only the text; the model also counts its chat template.
        Assert.NotEqual(reported, TokenCounter.O200k().Count(prompt));
    }

    [Fact]
    public void ShouldSayWhetherAPromptFitsTheBudget()
    {
        var guard = new ContextGuard(TokenCounter.O200k(), contextWindow: 30, reservedForOutput: 10);

        Assert.Equal(20, guard.PromptBudget);
        Assert.True(guard.Fits([User("Name the capital of France in one word.")]));       // 9 + 4 = 13
        Assert.False(guard.Fits([User("Name the capital of France in one word."), User("And of Spain?")]));
    }

    [Fact]
    public void ShouldDropTheOldestMessagesButKeepTheSystemPromptAndTheLatest()
    {
        var guard = new ContextGuard(TokenCounter.O200k(), contextWindow: 40, reservedForOutput: 10);
        List<ChatMessage> history =
        [
            new(ChatRole.System, "Be brief."),
            User("Name the capital of France in one word."),
            new(ChatRole.Assistant, "Paris"),
            User("And of Spain?"),
        ];

        var trimmed = guard.Trim(history);

        Assert.True(guard.Fits(trimmed));
        Assert.Equal(ChatRole.System, trimmed[0].Role);
        Assert.Equal("And of Spain?", trimmed[^1].Text);
        Assert.True(trimmed.Count < history.Count);
    }

    [Fact]
    public void ShouldRefuseToTrimWhenTheLatestMessageAloneDoesNotFit()
    {
        var guard = new ContextGuard(TokenCounter.O200k(), contextWindow: 12, reservedForOutput: 10);

        Assert.Throws<InvalidOperationException>(() =>
            guard.Trim([new ChatMessage(ChatRole.System, "Be brief."), User("Name the capital of France in one word.")]));
    }

    // --- usage meter ---

    [Fact]
    public async Task ShouldSumTheUsageOfEveryCall()
    {
        var meter = new UsageMeter(new ScriptedChatClient());

        await meter.GetResponseAsync("one");
        await meter.GetResponseAsync("two");

        Assert.Equal(2, meter.Calls);
        Assert.Equal(200, meter.Total.InputTokenCount);
        Assert.Equal(40, meter.Total.OutputTokenCount);
        Assert.Equal(0.0012m, meter.Cost(Price));   // 200 * 3 + 40 * 15 = 1200 per million
    }

    [Fact]
    public async Task ShouldCountACallWhoseProviderReportedNoUsage()
    {
        var meter = new UsageMeter(new ScriptedChatClient(reportUsage: false));

        await meter.GetResponseAsync("one");

        Assert.Equal(1, meter.Calls);
        Assert.Null(meter.Total.InputTokenCount);
        Assert.Equal(0m, meter.Cost(Price));
    }

    [Fact]
    public async Task ShouldTakeTheUsageOfAStreamFromItsUsageContent()
    {
        var meter = new UsageMeter(new ScriptedChatClient());

        await foreach (var _ in meter.GetStreamingResponseAsync("one")) { }

        Assert.Equal(1, meter.Calls);
        Assert.Equal(100, meter.Total.InputTokenCount);
        Assert.Equal(20, meter.Total.OutputTokenCount);
    }

    // --- pipeline: cache, telemetry, meter ---

    [Fact]
    public async Task ShouldReachTheModelOnceForTwoIdenticalPrompts()
    {
        var model = new ScriptedChatClient();
        var meter = new UsageMeter(model);
        var client = LlmPipeline.Build(meter, NewCache(), $"lab-{Guid.NewGuid()}");

        var first = await client.GetResponseAsync("Name the capital of France in one word.");
        var second = await client.GetResponseAsync("Name the capital of France in one word.");

        Assert.Equal(1, model.Calls);
        Assert.Equal(1, meter.Calls);
        Assert.Equal(100, meter.Total.InputTokenCount);          // the hit is not billed again
        Assert.Equal(first.Usage!.InputTokenCount, second.Usage!.InputTokenCount);   // but the copy still carries Usage
    }

    [Fact]
    public async Task ShouldEmitTokenUsageMetricsOnlyForRealCalls()
    {
        string source = $"lab-{Guid.NewGuid()}";
        using var probe = new TelemetryProbe(source);
        var client = LlmPipeline.Build(new UsageMeter(new ScriptedChatClient()), NewCache(), source);

        await client.GetResponseAsync("same prompt");
        await client.GetResponseAsync("same prompt");

        var tokens = probe.Measurements.Where(m => m.Instrument == "gen_ai.client.token.usage").ToList();
        Assert.Equal(2, tokens.Count);                                     // one input, one output
        Assert.Contains((("gen_ai.client.token.usage"), 100L, "input"), tokens);
        Assert.Contains((("gen_ai.client.token.usage"), 20L, "output"), tokens);
        Assert.Single(probe.Measurements, m => m.Instrument == "gen_ai.client.operation.duration");
    }

    [Fact]
    public async Task ShouldCreateOneChatSpanWithTheUsageAsAttributes()
    {
        string source = $"lab-{Guid.NewGuid()}";
        using var probe = new TelemetryProbe(source);
        var client = LlmPipeline.Build(new UsageMeter(new ScriptedChatClient()), NewCache(), source);

        await client.GetResponseAsync("a prompt");

        var span = Assert.Single(probe.Activities);
        Assert.Equal("chat fake-model", span.DisplayName);
        Assert.Equal("chat", span.GetTagItem("gen_ai.operation.name"));
        Assert.Equal(100, span.GetTagItem("gen_ai.usage.input_tokens"));
        Assert.Equal(20, span.GetTagItem("gen_ai.usage.output_tokens"));
    }
}
