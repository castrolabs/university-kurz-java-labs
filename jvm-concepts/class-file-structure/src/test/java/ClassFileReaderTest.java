import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClassFileReader")
class ClassFileReaderTest {

    @Test
    @DisplayName("every class file starts with the 0xCAFEBABE magic number")
    void classFileStartsWithMagicNumber() throws IOException {
        try (InputStream classFile = Counter.class.getResourceAsStream("Counter.class")) {
            assertEquals(0xCAFEBABE, ClassFileReader.readMagicNumber(classFile));
        }
    }

    @Test
    @DisplayName("the major version matches this project's compiler release (21 -> 65)")
    void majorVersionMatchesCompilerRelease() throws IOException {
        try (InputStream classFile = Counter.class.getResourceAsStream("Counter.class")) {
            assertEquals(65, ClassFileReader.readMajorVersion(classFile));
        }
    }

    @Test
    @DisplayName("(optional) constant_pool_count accounts for the reserved index 0")
    void constantPoolCountAccountsForTheReservedZeroIndex() throws IOException {
        try (InputStream classFile = Counter.class.getResourceAsStream("Counter.class")) {
            assertTrue(ClassFileReader.readConstantPoolCount(classFile) > 1);
        }
    }
}
