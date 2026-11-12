using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;

namespace Invoicing.Tests;

public static class VerifySetup
{
    // Verify already replaces Guid and DateTimeOffset values with Guid_1 and DateTimeOffset_1. It cannot know that
    // the invoice number is a string built from the date and the first 8 characters of a Guid, so this scrubber
    // runs on every snapshot in the assembly and rewrites any text that looks like one.
    [ModuleInitializer]
    public static void Initialize() =>
        VerifierSettings.ScrubLinesWithReplace(line =>
            Regex.Replace(line, @"INV-\d{8}-[0-9a-f]{8}", "INV-{date}-{id}"));
}
