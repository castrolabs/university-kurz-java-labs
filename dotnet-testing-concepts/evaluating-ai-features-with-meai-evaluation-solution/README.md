# Evaluating AI Features with Microsoft.Extensions.AI.Evaluation (solution)

Official solution of `evaluating-ai-features-with-meai-evaluation`. From this directory:

```bash
dotnet test
```

The 24 tests (12 test methods, two of them with 7 data rows each) pass in about 180 ms with no Ollama, from the
recorded `eval-cache/`. Versions: MSTest 4.5.3, `Microsoft.Extensions.AI.Evaluation`, `.Quality` and `.Reporting`
11.0.0, `Microsoft.Extensions.AI.Abstractions` 11.0.0, OllamaSharp 5.5.0, on the .NET 10 SDK (`net10.0`).

## Recording

`EVAL_RECORD=1 dotnet test` talks to `http://localhost:11434` with `qwen3:4b-instruct`, for the assistant and for
the judge. Recording the 8 scenarios takes about 90 s and the 2 known-bad answers about 25 s on the author's Mac.
Two tests that are about replay (`ReplayingTheCacheNeverReachesTheModel`, `EveryModelTurnOfAReplayedRunIsACacheHit`)
report `Inconclusive` while recording.

## Notes on the recorded run

| Scenario | Relevance | Groundedness | Coherence | Fact check |
| --- | --- | --- | --- | --- |
| refund-window | 4 | 5 | 4 | true |
| refund-time | 4 | 5 | 4 | true |
| shipping-defective | 2 | 3 | 3 | true |
| shipping-regular | 4 | 5 | 5 | true |
| sale-items | 4 | 5 | 4 | true |
| pitched-tent | 4 | 5 | 4 | true |
| gift-card | 4 | 5 | 3 | true |
| unknown (a correct refusal) | 1 | 1 | 3 | true |

- `shipping-defective`: the model answered "The customer pays for return shipping ... Wait, correction: ...
  Brightside Outdoor pays". The regular expression finds `free`, so the deterministic check is green, and only the
  judge (relevance 2) sees the answer is a mess. That is the point of TODO-05.
- `unknown`: the right answer, and the judge scores it 1 for relevance and 1 for groundedness. Never gate a refusal
  on a quality score; compare the text.
- Known-bad answers: "60 days ... always free" gets groundedness 3 and relevance 4 from the 4B judge (the fact check
  is what turns it red); the off-topic answer gets 1 and 1.
- Repeating the same judge request 5 times gave the same score 5 of 5 times, for each of four answers: with
  Temperature 0 the verdict does not vary for an identical request. The variation is in the answers (at
  Temperature 0.8 the broken-item question gave 2 different answers in 6, with relevance 3 or 4) and between models.
- Cost of the 8 scenarios, from `eval-results`: 29,489 input and 6,977 output tokens, in 32 model turns (1 answer
  and 3 judge calls per scenario). The judge reads the policy and thinks aloud, so most tokens are the evaluation,
  not the answer. A replay costs none and `eval-results` keeps the tokens of the recording.
- The prompt text is part of the cache key: changing `three sentences` to `two` in `SystemPrompt` turns 10 of the 12
  discovered tests red with `Cache miss` (the class initializer of `AnswerQualityTests` throws), and restoring it is green again.
- The model id reported by the client is part of the key too: `ReplayOnlyChatClient` reports `ollama` and
  `qwen3:4b-instruct`, and reporting another id misses every entry.
