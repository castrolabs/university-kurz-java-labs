# Agent Framework Workflows and Orchestration

## Goal

Build and test a Microsoft Agent Framework workflow graph without any LLM: conditional edges and switches, fan-out with a barrier fan-in, resettable executors, and checkpoints that let a new workflow instance resume after a crash.

## Prerequisites

- C# records, primary constructors and `partial` classes
- `Microsoft.Agents.AI.Workflows` 1.28.0 (stable on NuGet), `Executor`, `[MessageHandler]` and `IWorkflowContext`
- .NET 10 SDK

## Task

The executors are deterministic, so the tests need no model and no network. This is the official solution: classify tickets, route them with a switch and with a single conditional edge, join two parallel branches with a barrier, make a shared executor reusable, and save and restore an executor's state in a checkpoint.

## Instructions

What each TODO does in this solution:

- TODO-00: `Intake.Classify` checks "invoice" before "error", so billing wins when both appear.
- TODO-01: `AddSwitch` sends a ticket to exactly one target: the first case that matches, else `WithDefault`.
- TODO-02: with a plain conditional edge a non-matching ticket is dropped silently: no output, no error. Use a switch with a default (or a second edge) when "no match" must be handled.
- TODO-03: the barrier waits until both branches finished, then delivers each result as its own message in the same superstep. A handler for `List<int>` is never called (messages are dropped without an error), so `Total` counts messages itself.
- TODO-04: once a workflow has one `IResettableExecutor`, every shared executor instance in it must implement the interface, or the second run throws `Cannot reuse Workflow with shared Executor instances that do not implement IResettableExecutor.` With none resettable, the second run starts from the leftover state.
- TODO-05: `AddFanOutEdge(source, [square, cube])` and `AddFanInBarrierEdge([square, cube], total)`.
- TODO-06: a checkpoint holds the queued messages and the state registered with `QueueStateUpdateAsync`. Without TODO-06 the resumed run sums only steps 5 to 8 (26 instead of 36).

`FileSystemJsonCheckpointStore` locks its directory: dispose the first store before opening a second one on the same folder (the test that simulates another process does this).

## Running the Lab

From the lab directory:

```bash
dotnet test
```

The solution has 13 tests; 12 run by default. To also run the one that calls a local model:

```bash
ollama pull qwen3:4b-instruct
RUN_OLLAMA=1 dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): `AgentPipelines.BuildWriterEditor` is `AgentWorkflowBuilder.BuildSequential` over two agents, each with a stable `Id` (executor identity includes it, so a rebuilt workflow still matches an old checkpoint). The fake `IChatClient` in `BonusTests` shows two model calls, one per agent.
- TODO-08 (optional): `FactIfOllama` skips the real-model test unless `RUN_OLLAMA=1`.
