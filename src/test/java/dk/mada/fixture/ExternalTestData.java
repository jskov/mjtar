package dk.mada.fixture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * Test data to be sourced from outside the repository because of size.
 */
public class ExternalTestData {
    private static final Path EXTERNAL_TEST_DATA_DIR =
            Paths.get(System.getProperty("java.io.tmpdir")).resolve("_mjtar");

    /// Reference archive of the mozilla source code
    private static final URI MOZILLA_ARCHIVE_URI =
            URI.create("https://github.com/yewq/Silesia-compression-corpus/raw/refs/heads/main/mozilla.bz2");
    /// Local location of mozilla archive
    private static final Path MOZILLA_ARCHIVE_FILE = EXTERNAL_TEST_DATA_DIR.resolve("mozilla.tar");

    /// Private constructor.
    private ExternalTestData() {
        // prevents creation
    }

    public static Path getMozillaTar() {
        return cacheFile(
                MOZILLA_ARCHIVE_URI,
                MOZILLA_ARCHIVE_FILE,
                "657fc3764b0c75ac9de9623125705831ebbfbe08fed248df73bc2dc66e2a963b",
                Decompressors::bz2unzip);
    }

    private static Path cacheFile(URI uri, Path file, String expectedSha256sum, Decompressor decompressor) {
        try {
            if (!Files.isRegularFile(file)) {
                Files.createDirectories(EXTERNAL_TEST_DATA_DIR);

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofMinutes(2))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
                InputStream rawStream = decompressor.apply(response.body());
                Files.copy(rawStream, file);
                int statusCode = response.statusCode();
                if (statusCode != 200) {
                    throw new IllegalStateException(
                            "Failed to download file from " + uri + ", got response " + statusCode);
                }
            }

            byte[] buffer = new byte[32 * 1024];
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                int read;
                while ((read = is.read(buffer)) != -1) {
                    md.update(buffer, 0, read);
                }
            }
            String actualSha256sum = HexFormat.of().formatHex(md.digest());
            if (!actualSha256sum.equals(expectedSha256sum)) {
                throw new IllegalStateException(
                        "File " + file + " has sha256sum " + actualSha256sum + ", expected " + expectedSha256sum);
            }
            return file;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to cache URI " + uri + " to " + file, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Was intererruped downloading " + uri);
        }
    }

    @FunctionalInterface
    public interface Decompressor {
        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        InputStream apply(InputStream t) throws IOException;
    }

    private static class Decompressors {
        private static InputStream raw(InputStream is) {
            return new BufferedInputStream(is);
        }

        private static InputStream bz2unzip(InputStream is) throws IOException {
            return new BZip2CompressorInputStream(new BufferedInputStream(is));
        }
    }
}
