using System.IO.Compression;
using System.Text;
using Upgrade11;

namespace Upgrade11.Tests;

public class UpgradeTests
{
    // ---- Runtime: the same code, different results on .NET 11 -------------------------------

    [Theory]
    [InlineData(0.1, "0.1")]
    [InlineData(19.99, "19.99")]
    [InlineData(1.15, "1.15")]
    [InlineData(2.675, "2.675")]
    public void ShouldConvertADoubleToTheDecimalThatWasTyped(double value, string expected)
    {
        Assert.Equal(decimal.Parse(expected, System.Globalization.CultureInfo.InvariantCulture), Money.FromDouble(value));
    }

    [Theory]
    [InlineData(2.675, "2.68")]
    [InlineData(655.925, "655.93")]
    [InlineData(1.015, "1.02")]
    [InlineData(0.125, "0.13")]
    [InlineData(-2.675, "-2.68")]
    public void ShouldRoundHalfAwayFromZeroOnTheTypedValue(double value, string expected)
    {
        Assert.Equal(decimal.Parse(expected, System.Globalization.CultureInfo.InvariantCulture), Money.RoundToCents(value));
    }

    [Fact]
    public void ShouldAnswerCorruptWhenTheZipEntryFailsItsChecksum()
    {
        var zip = BuildZip("a.txt", "hello world hello world");
        var at = zip.AsSpan().IndexOf("hello"u8);
        zip[at + 3] ^= 0xFF;

        var result = Archive.ReadEntry(zip, "a.txt");

        Assert.Equal(ReadStatus.Corrupt, result.Status);
        Assert.Null(result.Text);
    }

    [Fact]
    public void ShouldStillReadAnIntactZipEntry()
    {
        var result = Archive.ReadEntry(BuildZip("a.txt", "hello"), "a.txt");

        Assert.Equal(ReadStatus.Ok, result.Status);
        Assert.Equal("hello", result.Text);
        Assert.Equal(ReadStatus.Missing, Archive.ReadEntry(BuildZip("a.txt", "hello"), "b.txt").Status);
    }

    [Fact]
    public void ShouldRecognizeAnEmptyGzipPayloadByItsContent()
    {
        Assert.True(Archive.IsEmptyPayload(Archive.GZip("")));
        Assert.True(Archive.IsEmptyPayload([]));
        Assert.False(Archive.IsEmptyPayload(Archive.GZip("x")));
    }

    [Fact]
    public void ShouldClampTheChannelInsteadOfDependingOnTheCast()
    {
        Assert.Equal(255, Money.ToChannel(300.0));
        Assert.Equal(0, Money.ToChannel(-5.0));
        Assert.Equal(128, Money.ToChannel(128.0));
    }

    // ---- Compiler: code that stops compiling with the SDK 11 compiler -----------------------

    [Fact]
    public void ShouldKeepCallingTheMethodNamedWithInsideACollectionExpression()
    {
        Assert.Equal(new object[] { 3, 3 }, Syntax.WithElements());
    }

    [Fact]
    public void ShouldDeclareTheTypeCalledClosedWithItsClrNameIntact()
    {
        var type = typeof(Money).Assembly.GetType("Upgrade11.closed");

        Assert.NotNull(type);
        var instance = Activator.CreateInstance(type);
        var reason = type.GetProperty("Reason");
        Assert.NotNull(reason);
        reason.SetValue(instance, "paid");
        Assert.Equal("paid", reason.GetValue(instance));
    }

    [Fact]
    public void ShouldReturnTheLastPairWithoutAnEscapingSpan()
    {
        Assert.Equal(new[] { 3, 6 }, Syntax.LastPair([1, 2, 3]));
        Assert.Equal(new[] { 5, 10 }, Syntax.LastPair([5]));
    }

    private static byte[] BuildZip(string name, string text)
    {
        var output = new MemoryStream();
        using (var zip = new ZipArchive(output, ZipArchiveMode.Create, leaveOpen: true))
        {
            var entry = zip.CreateEntry(name, CompressionLevel.NoCompression);
            using var stream = entry.Open();
            stream.Write(Encoding.UTF8.GetBytes(text));
        }

        return output.ToArray();
    }
}
