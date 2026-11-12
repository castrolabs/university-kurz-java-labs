# Union Types in C# 15 (Solution)

Official solution for the `union-types-in-csharp-15` lab.

## What changed from the starter

- `Describe` has three arms and no discard. Because `default(PaymentResult).Value` is null, it throws `SwitchExpressionException` for the default value; `DescribeSafe` adds `null => "no result"`, which also keeps the warning alive if a fourth case type is added.
- `ToJson` and `FromJson` are one-liners over `JsonSerializer`: the .NET 11 serializer understands `union` declarations.
- `IntOrText` keeps the `int` in its own field and exposes `TryGetValue` for each case type. A switch over it calls `TryGetValue` instead of reading `Value`, so matching an `int` allocates nothing (the test asserts 0 bytes after a warm-up).

## Running

```
dotnet test
```

Requires the .NET 11 SDK.
