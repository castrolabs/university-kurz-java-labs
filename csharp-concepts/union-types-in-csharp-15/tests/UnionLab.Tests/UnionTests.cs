using System.Runtime.CompilerServices;
using System.Text.Json;

namespace UnionLab.Tests;

public class UnionTests
{
    [Fact]
    public void ShouldDeclareAUnionThatTheCompilerMarksWithUnionAttribute()
    {
        Assert.True(typeof(PaymentResult).IsValueType);
        Assert.NotNull(typeof(PaymentResult).GetCustomAttributes(false).SingleOrDefault(a => a is UnionAttribute));
        Assert.Contains(typeof(IUnion), typeof(PaymentResult).GetInterfaces());
    }

    [Fact]
    public void ShouldKeepTheCaseValueInsideTheUnion()
    {
        PaymentResult r = new Declined("no funds");
        Assert.Equal(new Declined("no funds"), r.Value);
    }

    [Fact]
    public void ShouldDescribeEachCaseType()
    {
        Assert.Equal("approved 10 (A1)", Payments.Describe(new Approved(10, "A1")));
        Assert.Equal("declined: no funds", Payments.Describe(new Declined("no funds")));
        Assert.Equal("pending, retry in 30s", Payments.Describe(new Pending(30)));
    }

    [Fact]
    public void ShouldThrowFromDescribeWhenTheUnionIsDefault()
    {
        // default(PaymentResult).Value is null and Describe has no null arm.
        Assert.Throws<SwitchExpressionException>(() => Payments.Describe(default));
    }

    [Fact]
    public void ShouldHandleDefaultWithANullArm()
    {
        Assert.Equal("no result", Payments.DescribeSafe(default));
        Assert.Equal("pending, retry in 5s", Payments.DescribeSafe(new Pending(5)));
    }

    [Theory]
    [InlineData(42, "42")]
    [InlineData("hi", "\"hi\"")]
    public void ShouldSerializeTheCaseWithoutAnEnvelope(object raw, string expectedJson)
    {
        IntOrString value = raw is int i ? i : (string)raw;
        Assert.Equal(expectedJson, Payments.ToJson(value));
    }

    [Fact]
    public void ShouldRoundTripBothCases()
    {
        Assert.Equal(7, Payments.FromJson("7").Value);
        Assert.Equal("x", Payments.FromJson("\"x\"").Value);
    }

    [Fact]
    public void ShouldRejectATokenThatMatchesNoCaseType()
    {
        Assert.Throws<JsonException>(() => Payments.FromJson("true"));
    }

    [Fact]
    public void ShouldReadCasesFromACustomUnion()
    {
        Assert.Equal(6, Totals.Sum([new IntOrText(1), new IntOrText("ab"), new IntOrText(3), default]));
    }

    [Fact]
    public void ShouldNotBoxTheIntOfACustomUnionWhenMatching()
    {
        var items = new IntOrText[1000];
        for (int i = 0; i < items.Length; i++) items[i] = new IntOrText(i);

        Totals.Sum(items); // warm up
        long before = GC.GetAllocatedBytesForCurrentThread();
        long total = Totals.Sum(items);
        long allocated = GC.GetAllocatedBytesForCurrentThread() - before;

        Assert.Equal(499500, total);
        Assert.Equal(0, allocated);
    }

    [Fact]
    public void ShouldBoxTheIntOfAGeneratedUnion()
    {
        IntOrString value = 42;
        Assert.IsType<int>(value.Value); // lives in an `object?`: boxed
    }
}
