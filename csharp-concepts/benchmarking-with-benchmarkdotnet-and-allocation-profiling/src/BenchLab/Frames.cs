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
        // TODO-03: Same result as ChecksumCopy, without allocating the copy. Rent a buffer of at
        //   least frame.Length bytes from ArrayPool<byte>.Shared, copy the frame into it, sum only
        //   the first frame.Length bytes (the rented buffer may be longer and may hold old data),
        //   and Return the buffer in a finally block.
        throw new NotImplementedException("Not implemented yet.");
    }

    private static long Sum(ReadOnlySpan<byte> bytes)
    {
        long total = 0;
        foreach (var b in bytes) total += b;
        return total;
    }
}
