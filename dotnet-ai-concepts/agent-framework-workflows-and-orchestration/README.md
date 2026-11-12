# Agent Framework Workflows and Orchestration

## Goal

Build and test a Microsoft Agent Framework workflow graph without any LLM: conditional edges and switches, fan-out with a barrier fan-in, resettable executors, and checkpoints that let a new workflow instance resume after a crash.

## Prerequisites

- C# records, primary constructors and `partial` classes
- `Microsoft.Agents.AI.Workflows` 1.28.0 (stable on NuGet), `Executor`, `[MessageHandler]` and `IWorkflowContext`
- .NET 10 SDK

## Task

The executors are deterministic, so the tests need no model and no network. You will classify tickets, route them with a switch and with a single conditional edge, join two parallel branches with a barrier, make a shared executor reusable, and save and restore an executor's state in a checkpoint.

The `Microsoft.Agents.AI.Workflows.Generators` package is already referenced: without it, an `Executor` subclass does not compile (`ConfigureProtocol` is never generated).

## Instructions

Complete the following TODOs:

- TODO-00: `Intake.Classify`: `billing` for "invoice", `technical` for "error", `other` otherwise.
- TODO-01: `TriageWorkflow.Build`: `AddSwitch` with two cases and a default.
- TODO-02: `TriageWorkflow.BuildBillingOnly`: one conditional edge, no default. What happens to a ticket that matches nothing?
- TODO-03: `Total` handler: accumulate the barrier's messages and yield the sum after the last one.
- TODO-04: `Total.ResetAsync`: reset the state so the same `Workflow` can run twice.
- TODO-05: `FanWorkflow.Build`: `AddFanOutEdge` and `AddFanInBarrierEdge`.
- TODO-06: `Accumulator`: save the total in `OnCheckpointingAsync` and read it back in `OnCheckpointRestoredAsync`.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-07 (optional): Build a sequential `Writer` then `Editor` pipeline with `AgentWorkflowBuilder.BuildSequential`, over any `IChatClient`. Test it with a fake `IChatClient` that echoes its last message and counts calls: how many model calls does it make? Why does each agent need a stable `ChatClientAgentOptions.Id` if you want to resume from a checkpoint?
- TODO-08 (optional): Run the same pipeline against a local Ollama (`ollama pull qwen3:4b-instruct`, `http://localhost:11434`) in a test that is skipped unless an environment variable is set.
