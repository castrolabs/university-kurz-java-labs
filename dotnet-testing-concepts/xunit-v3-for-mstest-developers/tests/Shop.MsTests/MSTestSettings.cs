// Classes run in parallel; tests inside one class run one after the other.
[assembly: Parallelize(Workers = 0, Scope = ExecutionScope.ClassLevel)]
