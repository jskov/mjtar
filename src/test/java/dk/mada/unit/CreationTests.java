package dk.mada.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dk.mada.mjtar.TarOutputStream;

/**
 * Tests of constructor variants.
 */
class CreationTests {
    @TempDir Path dir;

    /// Tests simple file creation.
    @Test
    void canCreateEmptyArchiveInNewFile() throws IOException {
        Path archive = dir.resolve("a.tar");
        try (var _ = new TarOutputStream(archive)) {
            // empty
        }
        assertThat(archive)
            .isRegularFile()
            .hasSize(1024);
    }
}
