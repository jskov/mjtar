package dk.mada.mjtar;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// Helps dealing with file permissions.
///
/// TODO: Rewrite to Files/Path
public final class PermissionUtils {
    /// Flag for default filesystem being posix capable
    /// FIXME: should get state from filesystem of first processed file - and specify this clearly in the docs
    private static final boolean IS_POSIX =
            FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    /// When using non-posix permissions, 'owner' and 'group' are treated as the same. No permissions are given to
    // 'others'.
    private enum StandardFilePermission {
        /// Execute permissions (for owner+group)
        EXECUTE(0110),
        /// Write permissions (for owner+group)
        WRITE(0220),
        /// Read permissions (for owner+group)
        READ(0440);

        /// The mode values for the selected permission
        private int mode;

        StandardFilePermission(int mode) {
            this.mode = mode;
        }
    }

    /// Mapping posix permissions to mode values.
    /// TODO: probably use enumMap?
    private static Map<PosixFilePermission, Integer> posixPermissionToInteger = Map.of(
            PosixFilePermission.OWNER_EXECUTE, 0100,
            PosixFilePermission.OWNER_WRITE, 0200,
            PosixFilePermission.OWNER_READ, 0400,
            PosixFilePermission.GROUP_EXECUTE, 0010,
            PosixFilePermission.GROUP_WRITE, 0020,
            PosixFilePermission.GROUP_READ, 0040,
            PosixFilePermission.OTHERS_EXECUTE, 0001,
            PosixFilePermission.OTHERS_WRITE, 0002,
            PosixFilePermission.OTHERS_READ, 0004);

    /// Prevents instantiation.
    private PermissionUtils() {
        // empty
    }

    /// Get file permissions in octal mode, e.g. 0755.
    ///
    /// Note: it uses `java.nio.file.attribute.PosixFilePermission` if OS supports this, otherwise reverts to
    /// using standard Java file operations, e.g. `java.io.File#canExecute()`. In the first case permissions will
    /// be precisely as reported by the OS, in the second case 'owner' and 'group' will have equal permissions and
    /// 'others' will have no permissions, e.g. if file on Windows OS is `read-only` permissions will be `0550`.
    ///
    /// @throws NullPointerException if file is null.
    /// @throws IllegalArgumentException if file does not exist.
    public static int permissions(File f) {
        if (f == null) {
            throw new NullPointerException("File is null.");
        }
        if (!f.exists()) {
            throw new IllegalArgumentException("File " + f + " does not exist.");
        }

        return IS_POSIX ? posixPermissions(f) : standardPermissions(f);
    }

    private static int posixPermissions(File f) {
        int number = 0;
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(f.toPath());
            for (Map.Entry<PosixFilePermission, Integer> entry : posixPermissionToInteger.entrySet()) {
                if (permissions.contains(entry.getKey())) {
                    number += entry.getValue();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return number;
    }

    private static Set<StandardFilePermission> readStandardPermissions(File f) {
        Set<StandardFilePermission> permissions = new HashSet<>();
        if (f.canExecute()) {
            permissions.add(StandardFilePermission.EXECUTE);
        }
        if (f.canWrite()) {
            permissions.add(StandardFilePermission.WRITE);
        }
        if (f.canRead()) {
            permissions.add(StandardFilePermission.READ);
        }
        return permissions;
    }

    private static Integer standardPermissions(File f) {
        int number = 0;
        Set<StandardFilePermission> permissions = readStandardPermissions(f);
        for (StandardFilePermission permission : permissions) {
            number += permission.mode;
        }
        return number;
    }
}
