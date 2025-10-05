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

/// Constants specific to a tar archive.
///
/// @author Kamran Zafar
public final class TarConstants {
    /// Size of EOF block.
    public static final int EOF_BLOCK = 1024;
    /// Size of a data block
    public static final int DATA_BLOCK = 512;
    /// Size of a header block
    public static final int HEADER_BLOCK = 512;

    /// Prevents instantiation.
    private TarConstants() {
        // empty
    }
}
