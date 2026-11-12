using Billing;

namespace Billing.Tests;

[TestClass]
public sealed class SplitPropertyTests
{
    // Amounts in cents, refunds included: the default generator, with its shrinker.
    private static readonly Arbitrary<long> Amounts = ArbMap.Default.ArbFor<long>();

    // A realistic number of parts. A generator alone does not shrink: the second argument does.
    private static readonly Arbitrary<int> Parts =
        Arb.From(Gen.Choose(1, 20), parts => Enumerable.Range(1, parts - 1));

    [TestMethod]
    public void SharesAddUpToTheAmount()
    {
        Prop.ForAll(Amounts, Parts, (cents, parts) =>
                MoneySplitter.Split(cents, parts).Sum() == cents)
            .QuickCheckThrowOnFailure();
    }

    [TestMethod]
    public void ThereIsOneShareForEachPart()
    {
        Prop.ForAll(Amounts, Parts, (cents, parts) =>
                MoneySplitter.Split(cents, parts).Length == parts)
            .QuickCheckThrowOnFailure();
    }

    [TestMethod]
    public void NoShareIsMoreThanOneCentAwayFromAnother()
    {
        Prop.ForAll(Amounts, Parts, (cents, parts) =>
            {
                var shares = MoneySplitter.Split(cents, parts);
                return shares.Max() - shares.Min() <= 1;
            })
            .QuickCheckThrowOnFailure();
    }

    [TestMethod]
    public void TheLargerSharesComeFirst()
    {
        Prop.ForAll(Amounts, Parts, (cents, parts) =>
            {
                var shares = MoneySplitter.Split(cents, parts);
                return shares.Zip(shares.Skip(1)).All(pair => pair.First >= pair.Second);
            })
            .QuickCheckThrowOnFailure();
    }

    [TestMethod]
    public void WithoutAtLeastOnePartItThrows()
    {
        Prop.ForAll(Amounts, Gen.Choose(-50, 0).ToArbitrary(), (cents, parts) =>
            {
                try
                {
                    MoneySplitter.Split(cents, parts);
                    return false;
                }
                catch (ArgumentOutOfRangeException)
                {
                    return true;
                }
            })
            .QuickCheckThrowOnFailure();
    }

    // The same seed and size generate the same inputs, so a failure printed by FsCheck can be replayed.
    [TestMethod]
    public void TheSameSeedGeneratesTheSameInputs()
    {
        List<(long Cents, int Parts)> Generate()
        {
            var seen = new List<(long, int)>();
            Prop.ForAll(Amounts, Parts, (cents, parts) =>
                {
                    seen.Add((cents, parts));
                    return true;
                })
                .Check(Config.QuickThrowOnFailure.WithReplay(8894821150554322134UL, 8213904093035709541UL, 8));
            return seen;
        }

        CollectionAssert.AreEqual(Generate(), Generate());
    }

    // The example that the first failure shrank to, kept as an ordinary test.
    [TestMethod]
    [DataRow(-1L, 2, new long[] { 0, -1 })]
    [DataRow(-7L, 3, new long[] { -2, -2, -3 })]
    public void ARefundIsSplitToo(long cents, int parts, long[] expected)
    {
        CollectionAssert.AreEqual(expected, MoneySplitter.Split(cents, parts));
    }
}
