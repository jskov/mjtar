/// Copyright 2012 Kamran Zafar
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/// Tar computation utilities.
///
/// @author Kamran
public final class TarUtils {
    /// Prevents instantiation.
    private TarUtils() {
        // empty
    }

    /// Determines the tar file size of the given folder/file path.
    ///
    /// @param path the path to inspect
    /// @return the expected size of a tar for the path
    public static long calculateTarSize(Path path) {
        return tarSize(path) + TarConstants.EOF_BLOCK;
    }

    private static long tarSize(Path dir) {
        if (Files.isRegularFile(dir)) {
            return entrySize(dir.toFile().length());
        }

        List<Path> subFiles;
        try (Stream<Path> files = Files.list(dir)) {
            subFiles = files.toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list files in " + dir, e);
        }
        if (subFiles.isEmpty()) {
            // Empty folder header
            return TarConstants.HEADER_BLOCK;
        } else {
            return subFiles.stream().mapToLong(TarUtils::tarSize).sum();
        }
    }

    private static long entrySize(long fileSize) {
        long size = 0;
        size += TarConstants.HEADER_BLOCK; // Header
        size += fileSize; // File size

        long extra = size % TarConstants.DATA_BLOCK;

        if (extra > 0) {
            size += (TarConstants.DATA_BLOCK - extra); // pad
        }

        return size;
    }

    /// Trim character from both ends of string
    ///
    /// @param s the string to trim
    /// @param c the character to trim away
    /// @return the resulting string
    public static String trim(String s, char c) {
        StringBuffer tmp = new StringBuffer(s);
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        return tmp.toString();
    }
}
