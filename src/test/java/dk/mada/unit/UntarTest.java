package dk.mada.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import dk.mada.fixtures.TestLogging;
import dk.mada.mjtar.Tars;

/// Archive extraction tests.
@ExtendWith(TestLogging.class)
class UntarTest {
    /// The test output directory.
    @TempDir(cleanup = CleanupMode.ON_SUCCESS) Path dir;

    /// Tests simple tar file tree extraction.
    @Test
    void canExtractTree() throws IOException {
        Path archive = Paths.get("src/test/scripts/test-files.tar");
        try (InputStream is = Files.newInputStream(archive);
                BufferedInputStream bis = new BufferedInputStream(is)) {
            Tars.untar(bis)
                .destination(dir)
                .extract();
        }
        assertThat(dir.resolve("root/root-file.txt"))
            .content().isEqualTo("root\n");
        assertThat(dir.resolve("root/level1/level2/level3/file.txt"))
            .content().isEqualTo("level3\n");
    }
}
