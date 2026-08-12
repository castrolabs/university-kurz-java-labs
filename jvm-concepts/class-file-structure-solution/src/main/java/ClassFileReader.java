import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader {

    public static int readMagicNumber(InputStream classFile) throws IOException {
        var in = new DataInputStream(classFile);
        return in.readInt();
    }

    public static int readMajorVersion(InputStream classFile) throws IOException {
        var in = new DataInputStream(classFile);
        in.readInt();               // magic_number
        in.readUnsignedShort();     // minor_version
        return in.readUnsignedShort(); // major_version
    }

    public static int readConstantPoolCount(InputStream classFile) throws IOException {
        var in = new DataInputStream(classFile);
        in.readInt();               // magic_number
        in.readUnsignedShort();     // minor_version
        in.readUnsignedShort();     // major_version
        return in.readUnsignedShort(); // constant_pool_count
    }
}
