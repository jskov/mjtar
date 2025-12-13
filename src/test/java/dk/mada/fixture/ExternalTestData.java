package dk.mada.fixture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/// Test data to be sourced from outside the repository because of size.
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

    /// Make remote file available locally.
    ///
    /// If the file is already present and matches the sha256sum return it directly.
    /// Otherwise download the file as an inputStream.
    /// The decompressor is used to decompress the remote inputStream before the data is written to disk.
    ///
    /// @param uri  the remote URI to download the file from
    /// @param file the local location of the file
    /// @param expectedSha256sum the expected sha256sum of the file
    /// @param decompressor used for (optionally) decompressing the remote input stream 
    private static Path cacheFile(URI uri, Path file, String expectedSha256sum, Decompressor decompressor) {
        try {
            if (!Files.isRegularFile(file)) {
                Files.createDirectories(EXTERNAL_TEST_DATA_DIR);

                try (InputStream rawStream = uri.toURL().openStream();
                        InputStream decompressedStream = decompressor.apply(rawStream)) {
                    Files.copy(decompressedStream, file);
                }
            }
            byte[] buffer = new byte[32 * 1024];
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file);
                    DigestInputStream dis = new DigestInputStream(is, md)) {
                while (dis.read(buffer) != -1) {
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

    /// Stream decompressors. 
    private static class Decompressors {
        /// Pass through (No operation). No changes made to the stream.
        ///
        /// @param is the stream
        /// @result the input stream
        @SuppressWarnings("unused")
        public static InputStream passThrough(InputStream is) {
            return new BufferedInputStream(is);
        }

        /// Decompress bz2 stream.
        ///
        /// @param is the stream to decompress
        /// @result the decompressed stream
        public static InputStream bz2unzip(InputStream is) throws IOException {
            return new BZip2CompressorInputStream(new BufferedInputStream(is));
        }
    }
}
