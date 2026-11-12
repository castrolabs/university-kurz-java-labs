using CollectionExpressionsLab;

namespace CollectionExpressionsLab.Tests;

public class CollectionTests
{
    [Fact]
    public void ShouldReserveTwiceTheCapacityInAList()
    {
        List<string> names = Names.WithCapacity(["one", "two", "three"]);

        Assert.Equal(["one", "two", "three"], names);
        Assert.Equal(6, names.Capacity);
    }

    [Fact]
    public void ShouldCompareTagsIgnoringCase()
    {
        HashSet<string> tags = Tags.CaseInsensitive("Hello", "HELLO", "hello", "World");

        Assert.Equal(2, tags.Count);
        Assert.Contains("WORLD", tags);
    }

    [Fact]
    public void ShouldCombineHeadTailAndLast()
    {
        IReadOnlyList<int> all = Sequences.Combine([1, 2], Enumerable.Range(10, 2), 99);

        Assert.Equal([1, 2, 10, 11, 99], all);
    }

    [Fact]
    public void ShouldEnumerateALazyTailOnlyOnce()
    {
        int enumerations = 0;
        IEnumerable<int> Tail()
        {
            enumerations++;
            yield return 7;
            yield return 8;
        }

        IReadOnlyList<int> all = Sequences.Combine([1], Tail(), 9);

        Assert.Equal([1, 7, 8, 9], all);
        Assert.Equal(1, enumerations);
    }

    [Fact]
    public void ShouldCreateAListWithTheRequestedCapacityBehindAnInterface()
    {
        IList<int> list = Sequences.Reserve(10, Enumerable.Range(1, 3));

        var concrete = Assert.IsType<List<int>>(list);
        Assert.Equal([1, 2, 3], concrete);
        Assert.Equal(10, concrete.Capacity);
    }

    [Fact]
    public void ShouldBuildABagWithoutArguments()
    {
        Bag<int> bag = [1, 2, 3];

        Assert.Equal("", bag.Label);
        Assert.Equal([1, 2, 3], bag);
    }

    [Fact]
    public void ShouldBuildABagWithALabelFromWithArguments()
    {
        Bag<int> bag = [with("nums"), 4, 5];

        Assert.Equal("nums", bag.Label);
        Assert.Equal([4, 5], bag);
    }

    [Fact]
    public void ShouldKeepTheOldMeaningOfAMethodNamedWith()
    {
        List<int> list = Legacy.Build();

        Assert.Equal([20, 3], list);
    }

    [Fact]
    public void ShouldTreatWithAsArgumentsWhenWrittenInAnotherMethod()
    {
        // This is the new meaning, shown for contrast: no method is called.
        List<int> list = [with(10), 3];

        Assert.Equal([3], list);
        Assert.Equal(10, list.Capacity);
    }
}
