using Microsoft.Extensions.DependencyInjection;

namespace DiLab;

public static class StartupTasks
{
    // Runs outside any request, so there is no ambient scope to borrow a FakeDb from.
    public static FakeDb SeedDatabase(IServiceProvider root)
    {
        // TODO-04: Create a scope, resolve FakeDb from the scope's provider, call Seed(),
        //   and return the instance. The scope must be disposed when the method ends.
        throw new NotImplementedException("Not implemented yet.");
    }
}
