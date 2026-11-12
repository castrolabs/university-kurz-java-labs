# Evaluating AI Features with Microsoft.Extensions.AI.Evaluation

## Goal

`ReturnsAssistant` answers customer questions about a returns policy through an `IChatClient`. Eight questions
were asked once against a local Ollama (`qwen3:4b-instruct`), and the same model judged each answer with the
Relevance, Coherence and Groundedness evaluators of `Microsoft.Extensions.AI.Evaluation.Quality`. Every request
and response is saved in `eval-cache/`. Your job is to write the MSTest tests that gate this feature: thresholds
instead of equalities, a deterministic check next to the judge, known-bad answers that prove the gate can fail,
and tests that prove the cache and the report work. The tests run in milliseconds and **need no Ollama**.

## Prerequisites

- MSTest 4 Fundamentals: Lifecycle and TestContext (the concept and its lab)
- `IChatClient` from Microsoft.Extensions.AI (the `dotnet-ai-concepts` track)
- .NET 10 SDK

## Task

Read `src/Returns/ReturnsAssistant.cs`, then `tests/Returns.Tests/Support`:

- `EvalSupport` builds the `ReportingConfiguration`: results in `eval-results/` (not committed), cached model
  responses in `eval-cache/` (committed), a 10-year time to live (the default is 14 days).
- `ReplayOnlyChatClient` is the "model" when you do not record: any request that reaches it is a cache miss and
  throws, so a test can never pass by silently calling a model.
- `ExpectedFactEvaluator` is a deterministic `IEvaluator`: a regular expression, no model, no cost.
- `EvalRunner` asks the assistant through the caching client of a `ScenarioRun` and evaluates the answer.

Three traps are waiting:

- an exact score (`Assert.AreEqual(4, score)`) is a flaky test the first time the judge or the model changes;
- a correct refusal ("I don't know...") is scored 1 by the judge, so quality scores must not gate refusals;
- a gate that tolerates nothing (every score at least 4) is red on the first weak answer, and a gate that is red
  forever is deleted.

## Instructions

Complete the following TODOs in `tests/Returns.Tests`:

- TODO-00 to TODO-05: `AnswerQualityTests`: thresholds, judge failures, the fact check, the refusal, the gate on
  the whole set, and the answer the fact check cannot see.
- TODO-06 to TODO-07: `KnownBadAnswerTests`: hand-written bad answers go through the same evaluators.
- TODO-08 to TODO-11: `CacheAndReportTests`: replay never reaches the model, every turn is a cache hit, the tokens
  are kept, and the HTML report is written.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Recording the cache again with Ollama (optional)

The cache was recorded with Ollama and `qwen3:4b-instruct`. To record it again, for example with another model or
after you change the prompt:

```bash
ollama pull qwen3:4b-instruct
rm -rf eval-cache
EVAL_RECORD=1 dotnet test      # a few minutes: every answer and every judge verdict is a model call
dotnet test                    # replay again, no model
```

A different model gives different answers and different scores, so some thresholds (and the weak answer in
TODO-05) have to be adjusted to what you recorded. A request that is not in the cache, because the prompt or the
model changed, fails with `Cache miss`. Keep the package versions pinned: the evaluators' own prompts are part of
the cache key.

## Bonus (Optional)

- TODO-12 (optional): The cache key. Change `at most three sentences` in `ReturnsAssistant.SystemPrompt` to
  `two` and run the tests. Which ones fail, and with what message? Restore it afterwards.
- TODO-13 (optional): The report from the command line.
  `dotnet new tool-manifest`, `dotnet tool install Microsoft.Extensions.AI.Evaluation.Console --version 11.0.0`,
  then `dotnet aieval report -p eval-results -o eval-results/report.html`. Open the file and find the answer
  with relevance 2.
- TODO-14 (optional): Cost. Add up the input and output tokens of all the scenarios in `eval-results` and
  compare them with the size of the answers.
