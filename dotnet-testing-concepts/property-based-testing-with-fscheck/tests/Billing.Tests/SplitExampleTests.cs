using Billing;

namespace Billing.Tests;

// The tests a developer writes first. They pass, and they read well: four examples, all with positive amounts.
[TestClass]
public sealed class SplitExampleTests
{
    [TestMethod]
    [DataRow(100L, 3, new long[] { 34, 33, 33 })]
    [DataRow(100L, 4, new long[] { 25, 25, 25, 25 })]
    [DataRow(1L, 3, new long[] { 1, 0, 0 })]
    [DataRow(0L, 5, new long[] { 0, 0, 0, 0, 0 })]
    public void AnInvoiceIsSplitInShares(long cents, int parts, long[] expected)
    {
        CollectionAssert.AreEqual(expected, MoneySplitter.Split(cents, parts));
    }

    [TestMethod]
    public void ZeroPartsIsRejected()
    {
        Assert.Throws<ArgumentOutOfRangeException>(() => MoneySplitter.Split(100, 0));
    }
}
