using Billing;

namespace Billing.Tests;

// Properties of MoneySplitter.Split, written with the FsCheck API inside ordinary [TestMethod]s:
//
//     Prop.ForAll(arbitraryA, arbitraryB, (a, b) => <a bool that must hold>).QuickCheckThrowOnFailure();
//
// QuickCheckThrowOnFailure throws when a property is falsified, and the message has the shrunk counterexample
// and the seed. MSTest reports that exception as the failure of the test.
[TestClass]
public sealed class SplitPropertyTests
{
    // TODO-02: Amounts is the default generator for long (with its shrinker). Parts only needs 1 to 20.
    // Replace the Parts line with Arb.From(Gen.Choose(1, 20), shrinker): the second argument turns a value
    // into smaller candidates (try Enumerable.Range(1, parts - 1)). Run a failing property with and without the
    // shrinker and compare the counterexamples.
    private static readonly Arbitrary<long> Amounts = ArbMap.Default.ArbFor<long>();

    private static readonly Arbitrary<int> Parts = ArbMap.Default.ArbFor<int>().Filter(parts => parts is >= 1 and <= 20);

    // TODO-00: The shares add up to the amount. Use Amounts and Parts. It must FAIL on the starting code:
    // write down the Original and the Shrunk values and the seed, then fix MoneySplitter.Split (src/Billing)
    // and run it again until it passes.
    [TestMethod]
    public void SharesAddUpToTheAmount()
    {
        Assert.Fail("TODO-00: write this property, watch it fail, then fix Split.");
    }

    // TODO-01: Three more properties of the same call: one share for each part; no two shares more than one
    // cent apart (max - min <= 1); the larger shares come first (each share is >= the next one).
    [TestMethod]
    public void ThereIsOneShareForEachPart()
    {
        Assert.Fail("TODO-01: write this property.");
    }

    [TestMethod]
    public void NoShareIsMoreThanOneCentAwayFromAnother()
    {
        Assert.Fail("TODO-01: write this property.");
    }

    [TestMethod]
    public void TheLargerSharesComeFirst()
    {
        Assert.Fail("TODO-01: write this property.");
    }

    // TODO-03: Fewer than one part throws ArgumentOutOfRangeException, for any amount. Use
    // Gen.Choose(-50, 0).ToArbitrary() for the parts. There is no Prop.Throws in the C# API: catch it yourself
    // and return true or false.
    [TestMethod]
    public void WithoutAtLeastOnePartItThrows()
    {
        Assert.Fail("TODO-03: write this property.");
    }

    // TODO-04: The same seed and size generate the same inputs. Collect every (cents, parts) pair a property
    // receives while it runs with Config.QuickThrowOnFailure.WithReplay(seed1, seed2, size), do it twice, and
    // assert the two lists are equal. Any seed works; use the one printed by your failing run if you saved it.
    [TestMethod]
    public void TheSameSeedGeneratesTheSameInputs()
    {
        Assert.Fail("TODO-04: write this test.");
    }

    // TODO-05: Keep what the property found as an ordinary example: the Shrunk values of TODO-00, plus a second
    // refund of your choice, as [DataRow] rows with the expected shares (larger shares first).
    [TestMethod]
    public void ARefundIsSplitToo()
    {
        Assert.Fail("TODO-05: add parameters and [DataRow] rows.");
    }
}
