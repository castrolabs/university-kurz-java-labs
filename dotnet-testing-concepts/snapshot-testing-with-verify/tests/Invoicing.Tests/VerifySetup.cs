using System.Runtime.CompilerServices;

namespace Invoicing.Tests;

public static class VerifySetup
{
    // TODO-01: Verify replaces Guid and DateTimeOffset values by itself, but the invoice Number is a string such as
    // INV-20261112-1a2b3c4d, built from the date and the first 8 characters of a Guid. If you ran
    // TODO-00 twice you saw the .received file change every run.
    // Add one scrubber here, for the whole assembly: VerifierSettings.ScrubLinesWithReplace takes a
    // Func<string, string?> that rewrites each line (Regex.Replace is enough). Replace the number with a fixed
    // placeholder, then run TODO-00 until the snapshot stops changing. Delete the old .verified.txt first.
    [ModuleInitializer]
    public static void Initialize()
    {
    }
}
