# Benchmarking with BenchmarkDotNet and Allocation Profiling

## Goal

Measure allocations instead of guessing: write a BenchmarkDotNet benchmark with a baseline, build a small allocation meter with `GC.GetAllocatedBytesForCurrentThread`, and fix methods that allocate too much with a `StringBuilder`, `ArrayPool<T>` and `ObjectPool<T>`.

## Prerequisites

- Allocations, the managed heap and `ArrayPool<T>`
- Basic reflection and attributes
- .NET 10 SDK

## Task

`Measure`, `ReportRenderer`, `Frames` and `RenderBenchmarks` are stubbed. The tests check allocations in bytes with ranges, never time, so they pass the same way on any machine. They also read the attributes of `RenderBenchmarks` by reflection, so the benchmark you write is checked without running BenchmarkDotNet inside the test run. All allocation tests live in one class with parallelization disabled.

## Instructions

Complete the following TODOs:

- TODO-00: `Measure.BytesPerCall`: average bytes allocated per call, with a warm-up call and a sink so the work is not dead code.
- TODO-01: `Measure.AllocRatio`: the same idea as the `Alloc Ratio` column of BenchmarkDotNet.
- TODO-02: `ReportRenderer.RenderFast`: replace the `+=` loop with a `StringBuilder`, so the cost stops growing with the square of the input.
- TODO-03: `Frames.ChecksumPooled`: rent from `ArrayPool<byte>.Shared` instead of copying a 100,000 byte frame into a new array on every call.
- TODO-04: `RenderBenchmarks`: write the benchmark class with `[MemoryDiagnoser]`, `[Params]`, `[GlobalSetup]` and a `[Benchmark(Baseline = true)]`.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
dotnet test -c Release
```

When TODO-04 is done, run your benchmark for real (Release only, outside a debugger, it takes a couple of minutes):

```bash
dotnet run -c Release --project src/BenchLab.Runner -- --filter "*" --job short
```

Read the `Ratio` and `Alloc Ratio` columns, not only `Mean`. Numbers depend on the machine: compare the two methods against each other, not against anyone else's table.

## Bonus (Optional)

- TODO-05 (optional): `ReportRenderer.RenderPooled`: take the builder from an `ObjectPool<StringBuilder>` and allocate only the result string. Try it with 1000 values and see why the default pool policy stops helping.
