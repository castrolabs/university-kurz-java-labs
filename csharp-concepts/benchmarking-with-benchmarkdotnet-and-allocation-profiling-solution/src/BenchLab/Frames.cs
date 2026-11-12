using System.Buffers;

namespace BenchLab;

public static class Frames
{
    // Provided: copies the frame into a new array on every call (a LOH object for big frames).
    public static long ChecksumCopy(ReadOnlySpan<byte> frame)
    {
        var copy = frame.ToArray();
        return Sum(copy);
    }

    public static long ChecksumPooled(ReadOnlySpan<byte> frame)
    {
        var buffer = ArrayPool<byte>.Shared.Rent(frame.Length);
        try
        {
            frame.CopyTo(buffer);
            return Sum(buffer.AsSpan(0, frame.Length));
        }
        finally
        {
            ArrayPool<byte>.Shared.Return(buffer);
        }
    }

    private static long Sum(ReadOnlySpan<byte> bytes)
    {
        long total = 0;
        foreach (var b in bytes) total += b;
        return total;
    }
}
