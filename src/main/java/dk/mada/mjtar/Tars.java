/// Copyright 2026 Jesper Skov
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///      http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
package dk.mada.mjtar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

/// This class consists exclusively of static methods that provide help
/// interacting with tar archives.
public final class Tars {
    private static Logger logger = Logger.getLogger(Tars.class.getName());

    /// Prevents instantiation.
    private Tars() {
        // empty
    }

    /// Creates an untar configuration builder that can be used extract a tar archive.
    ///
    /// @param tarInputStream the input stream for the tar archive
    /// @return the untar configuration builder
    public static UntarConfigBuilder untar(InputStream tarInputStream) {
        return new UntarConfigBuilder(tarInputStream);
    }

    /// Configuration builder for untar operation.
    public static final class UntarConfigBuilder {
        /// The tar input stream.
        private final TarInputStream tarStream;
        /// The destination directory.
        @Nullable private Path destination;

        /// Creates a new builder instance.
        ///
        /// The input stream will be wrapped in a BufferedInputStream if it is not already one.
        ///
        /// @param tarInputStream the input stream for the tar archive
        private UntarConfigBuilder(InputStream tarInputStream) {
            if (!(tarInputStream instanceof BufferedInputStream)) {
                tarInputStream = new BufferedInputStream(tarInputStream);
            }
            tarStream = new TarInputStream(tarInputStream);
        }

        /// Sets the destination directory. This will be created if it does not already exist.
        ///
        /// @param dir the destination directory
        /// @return the builder object
        public UntarConfigBuilder destination(Path dir) {
            this.destination = dir;
            return this;
        }

        /// Extracts the archive
        /// @throws IOException if there is an IO failure
        public void extract() throws IOException {
            doExtract(new UntarConfig(tarStream, Objects.requireNonNull(destination, "Destination must be specified")));
        }
    }

    /// Untar configuration.
    ///
    /// @param tarStream the tar input stream
    /// @param destination the destination directory
    record UntarConfig(TarInputStream tarStream, Path destination) {}

    /// Extract tar archive.
    ///
    /// @param config the configuration specifying how to extract the archive
    static void doExtract(UntarConfig config) throws IOException {
        Path destination = config.destination();
        if (!Files.isDirectory(destination)) {
            Files.createDirectories(destination);
        }
        try (TarInputStream tarStream = config.tarStream()) {
            TarEntry te;
            while ((te = tarStream.getNextEntry()) != null) {
                Path f = destination.resolve(te.getName());
                logger.fine(" " + te.getName());
                TarHeader header = te.getHeader();
                if (header.isDirectory()) {
                    Files.createDirectories(f);
                } else if (header.isSymbolicLink()) {
                    logger.info("TODO: softlink");
                } else {
                    Path dir = f.getParent();
                    Files.createDirectories(dir);
                    Files.copy(tarStream, f);
                }
            }
        }
    }
}
