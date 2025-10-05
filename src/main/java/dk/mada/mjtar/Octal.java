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

/// Utilities for handling octals.
///
/// TODO should be replaced with Long.parseUnsignedLong/toUnsignedString
///
/// @author Kamran Zafar
public final class Octal {
    /** Bit shift for each octal number. */
    private static final int OCTAL_SHIFT = 3;
    /** Bit mask for (lower) octal number. */
    private static final int OCTAL_MASK = 7;

    /// Prevents instantiation.
    private Octal() {
        // empty
    }

    /// Parse an octal string from a header buffer. This is used for the file
    /// permission mode value.
    ///
    /// @param header  the header buffer from which to parse
    /// @param offset  the offset into the buffer from which to parse
    /// @param length  the number of header bytes to parse
    ///
    /// @return the long value of the octal string
    public static long parseOctal(byte[] header, int offset, int length) {
        long result = 0;
        boolean stillPadding = true;

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            if (header[i] == 0) {
                break;
            }

            if (header[i] == (byte) ' ' || header[i] == '0') {
                if (stillPadding) {
                    continue;
                }

                if (header[i] == (byte) ' ') {
                    break;
                }
            }

            stillPadding = false;

            result = (result << OCTAL_SHIFT) + (header[i] - '0');
        }

        return result;
    }

    /// Write an octal integer to a header buffer.
    ///
    /// @param value   the value to write
    /// @param buf     the header buffer to write into
    /// @param offset  the offset to write from
    /// @param length  the number of header bytes to parse
    ///
    /// @return the integer value of the octal bytes
    public static int writeOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 1;

        buf[offset + idx] = 0;
        --idx;
        buf[offset + idx] = (byte) ' ';
        --idx;

        if (value == 0) {
            buf[offset + idx] = (byte) '0';
            --idx;
        } else {
            for (long val = value; idx >= 0 && val > 0; --idx) {
                buf[offset + idx] = (byte) ((byte) '0' + (byte) (val & OCTAL_MASK));
                val = val >> OCTAL_SHIFT;
            }
        }

        for (; idx >= 0; --idx) {
            buf[offset + idx] = (byte) '0';
        }

        return offset + length;
    }

    /// Write the checksum octal integer to a header buffer.
    ///
    /// @param value   the value to write
    /// @param buf     the header buffer to write into
    /// @param offset  the offset to write from
    /// @param length  the number of header bytes to parse
    /// @return the integer value of the entry's checksum
    public static int writeCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        writeOctalBytes(value, buf, offset, length);
        buf[offset + length - 1] = (byte) ' ';
        buf[offset + length - 2] = 0;
        return offset + length;
    }

    /// Write an octal long integer to a header buffer.
    ///
    /// @param value   the value to write
    /// @param buf     the header buffer to write into
    /// @param offset  the offset to write from
    /// @param length  the number of header bytes to parse
    /// @return the long value of the octal bytes
    public static int writeLongOctalBytes(long value, byte[] buf, int offset, int length) {
        byte[] temp = new byte[length + 1];
        writeOctalBytes(value, temp, 0, length + 1);
        System.arraycopy(temp, 0, buf, offset, length);
        return offset + length;
    }
}
