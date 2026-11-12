using ControlFlowLab;

namespace ControlFlowLab.Tests;

public class FlowTests
{
    [Theory]
    [InlineData(3, "gold silver bronze")]
    [InlineData(2, "silver bronze")]
    [InlineData(1, "bronze")]
    [InlineData(0, "none")]
    [InlineData(-1, "none")]
    [InlineData(9, "?")]
    public void ShouldAccumulateTiersWithGotoCase(int level, string expected)
    {
        Assert.Equal(expected, Flow.Tier(level));
    }

    [Fact]
    public void ShouldStopRetryingOnTheFirstSuccess()
    {
        int calls = 0;

        int last = Flow.Retry(n => { calls++; return n == 2; }, 5);

        Assert.Equal(2, last);
        Assert.Equal(2, calls);
    }

    [Fact]
    public void ShouldStopRetryingAtTheMaximum()
    {
        int last = Flow.Retry(_ => false, 3);

        Assert.Equal(3, last);
    }

    [Fact]
    public void ShouldFindTheFirstCellAndStopScanning()
    {
        int[][] grid = [[1, 0, 1], [2, 3, 9], [4, 5, 6]];

        var result = Flow.FindFirst(grid, 3);

        Assert.Equal(new SearchResult(1, 1, 5), result);
    }

    [Fact]
    public void ShouldReportAMissingGoalAfterVisitingEveryCell()
    {
        int[][] grid = [[1, 2], [3, 4]];

        var result = Flow.FindFirst(grid, 42);

        Assert.Equal(new SearchResult(-1, -1, 4), result);
    }

    [Fact]
    public void ShouldSkipTheRestOfARowAfterAZero()
    {
        int[][] grid = [[1, 0, 1], [2, 3, 9], [4, 5, 6]];

        Assert.Equal(7, Flow.CountOpenCells(grid));
    }

    [Fact]
    public void ShouldCountNothingForARowThatStartsWithZero()
    {
        int[][] grid = [[0, 1, 1], [1]];

        Assert.Equal(1, Flow.CountOpenCells(grid));
    }

    [Fact]
    public void ShouldSumCommandsSkippingZerosAndStoppingAtMinusOne()
    {
        Assert.Equal(12, Flow.RunCommands([5, 0, 7, -1, 100]));
    }

    [Fact]
    public void ShouldSumAllCommandsWhenThereIsNoStop()
    {
        Assert.Equal(6, Flow.RunCommands([1, 2, 0, 3]));
    }
}
