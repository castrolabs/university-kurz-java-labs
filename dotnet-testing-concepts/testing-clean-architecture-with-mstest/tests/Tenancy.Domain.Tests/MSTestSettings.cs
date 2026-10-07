// MSTest runs tests one at a time unless the assembly opts in. This opts in to running test methods in
// parallel. Each test gets its own instance of the test class, so instance fields are safe; static
// fields are shared by every test running at the same time and are not.
[assembly: Parallelize(Scope = ExecutionScope.MethodLevel)]
