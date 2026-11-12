# Tokens, Cost and Telemetry for LLM Calls

## Goal

Count tokens, price a call from `UsageDetails`, keep a prompt inside the context
window and wire `UseDistributedCache` and `UseOpenTelemetry` around an
`IChatClient`, then check from the outside what the cache does to the tokens
that are metered and billed.

## Prerequisites

- `IChatClient` and `ChatClientBuilder` (see the Microsoft.Extensions.AI lesson)
- Caching with `IDistributedCache`
- .NET 10 SDK

## Task

The tests use `ScriptedChatClient`, a deterministic `IChatClient` that answers
"Paris", reports 100 input and 20 output tokens and counts its calls. There is no
network and no Ollama. Metrics and spans are read with a `MeterListener` and an
`ActivityListener` (`TelemetryProbe`), so the OpenTelemetry SDK is not needed.

## Instructions

Complete the following TODOs:

- TODO-00: `CostCalculator.Estimate`: input and output tokens priced separately, per million, in `decimal`.
- TODO-01: `TokenCounter.Count` with the tokenizer (0 for empty text).
- TODO-02: `ContextGuard.CountTokens`: tokens of each message plus the fixed overhead.
- TODO-03: `ContextGuard.Fits`: compare with the prompt budget (window minus the room reserved for the answer).
- TODO-04: `ContextGuard.Trim`: drop the oldest non-system messages, keep the last one, throw when nothing more can go.
- TODO-05: `UsageMeter.GetResponseAsync`: count the call and add its usage, even when the provider reports none.
- TODO-06: `UsageMeter.GetStreamingResponseAsync`: usage comes in `UsageContent` items, count the call once at the end.
- TODO-07: `UsageMeter.Cost`: estimate the cost of the accumulated usage.
- TODO-08: `LlmPipeline.Build`: `UseDistributedCache` outermost, then `UseOpenTelemetry`, around the meter.

Read the two pipeline tests: the same two identical prompts produce one model
call, one meter call and one pair of `gen_ai.client.token.usage` measurements.

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Using Ollama instead of the fake

Nothing in `src/` depends on the provider. To try it against a local model, add
`OllamaSharp` to the test project (`dotnet add tests/LlmCostLab.Tests package OllamaSharp --version 5.5.0`)
and replace the fake where a test builds its client:

```csharp
IChatClient model = new OllamaApiClient(new Uri("http://localhost:11434"), "qwen3:4b-instruct");
var meter = new UsageMeter(model);
```

The numbers are then real (and the exact-count assertions will not hold): the
tokens the model reports are not the ones `TokenCounter` estimates.

## Bonus (Optional)

- Add a `ModelPrice` per model id and price each response by `response.ModelId`.
- Make `UsageMeter` sum `CachedInputTokenCount` and `ReasoningTokenCount` too.
