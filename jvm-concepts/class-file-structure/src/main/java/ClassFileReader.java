import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader {

    public static int readMagicNumber(InputStream classFile) throws IOException {
        // TODO-01: Read the first 4 bytes of the class file and return them as
        // a single int. Every .class file starts with the magic number
        // 0xCAFEBABE — see DataInputStream.readInt().
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static int readMajorVersion(InputStream classFile) throws IOException {
        // TODO-02: Skip magic_number (4 bytes) and minor_version (2 bytes),
        // then read and return the 2-byte major_version. Both version fields
        // are unsigned — see DataInputStream.readUnsignedShort().
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static int readConstantPoolCount(InputStream classFile) throws IOException {
        // TODO-05 (optional): Skip magic_number, minor_version and
        // major_version (8 bytes total), then read and return the 2-byte
        // constant_pool_count.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
