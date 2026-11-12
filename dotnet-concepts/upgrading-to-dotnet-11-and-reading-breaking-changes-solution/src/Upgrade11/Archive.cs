using System.IO.Compression;
using System.Text;

namespace Upgrade11;

public enum ReadStatus { Ok, Missing, Corrupt }

public sealed record ReadResult(ReadStatus Status, string? Text);

public static class Archive
{
    /// <summary>Reads one text entry from a ZIP that was uploaded by a user.</summary>
    public static ReadResult ReadEntry(byte[] zip, string name)
    {
        using var archive = new ZipArchive(new MemoryStream(zip), ZipArchiveMode.Read);
        var entry = archive.GetEntry(name);
        if (entry is null)
        {
            return new ReadResult(ReadStatus.Missing, null);
        }

        try
        {
            using var reader = new StreamReader(entry.Open(), Encoding.UTF8);
            return new ReadResult(ReadStatus.Ok, reader.ReadToEnd());
        }
        catch (InvalidDataException)
        {
            // .NET 11 validates the CRC32 of the extracted data.
            return new ReadResult(ReadStatus.Corrupt, null);
        }
    }

    public static byte[] GZip(string text)
    {
        var output = new MemoryStream();
        using (var gzip = new GZipStream(output, CompressionMode.Compress, leaveOpen: true))
        {
            gzip.Write(Encoding.UTF8.GetBytes(text));
        }

        return output.ToArray();
    }

    /// <summary>True when the compressed payload holds no text at all.</summary>
    public static bool IsEmptyPayload(byte[] gzipped)
    {
        if (gzipped.Length == 0)
        {
            return true;
        }

        using var gzip = new GZipStream(new MemoryStream(gzipped), CompressionMode.Decompress);
        return gzip.ReadByte() == -1;
    }
}
