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

        // TODO-02: On .NET 10 a damaged entry was read silently, with wrong bytes in the text.
        //          On .NET 11 reading validates the CRC32 and throws InvalidDataException when the
        //          bytes do not match. The upload endpoint must answer "corrupt", not crash.
        using var reader = new StreamReader(entry.Open(), Encoding.UTF8);
        return new ReadResult(ReadStatus.Ok, reader.ReadToEnd());
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
        // TODO-03: A GZipStream that never received data wrote 0 bytes on .NET 10, so this length
        //          check was enough. On .NET 11 it writes a valid 20 byte empty GZip file.
        //          Decide from the content, not from the size of the compressed bytes.
        return gzipped.Length == 0;
    }
}
