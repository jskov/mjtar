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

package org.kamranzafar.jtar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dk.mada.mjtar.TarEntry;
import dk.mada.mjtar.TarInputStream;
import dk.mada.mjtar.TarOutputStream;

class JTarAppendTest {
	static final int BUFFER = 2048;

	private @TempDir Path dir;
	private @TempDir Path outDir;
	private @TempDir Path inDir;

	@Test
	void testSingleOperation() throws IOException {
		TarOutputStream tar = new TarOutputStream(Files.newOutputStream(dir.resolve("tar.tar")));
		
		Path aFile = Files.writeString(inDir.resolve("afile"), "a");
		Path bFile = Files.writeString(inDir.resolve("bfile"), "b");
		Path cFile = Files.writeString(inDir.resolve("cfile"), "c");

		tar.putNextEntry(new TarEntry(aFile, "afile"));
        Files.copy(aFile, tar);
        tar.putNextEntry(new TarEntry(bFile, "bfile"));
		Files.copy(bFile, tar);
        tar.putNextEntry(new TarEntry(cFile, "cfile"));
		Files.copy(cFile, tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	@Test
	void testAppend() throws IOException {
		Path tarFile = dir.resolve("tar.tar");
        TarOutputStream tar = new TarOutputStream(Files.newOutputStream(tarFile));
        Path aFile = Files.writeString(inDir.resolve("afile"), "a");
		tar.putNextEntry(new TarEntry(aFile, "afile"));
		Files.copy(aFile, tar);
		tar.close();

		Path bFile = Files.writeString(inDir.resolve("bfile"), "b");
		Path cFile = Files.writeString(inDir.resolve("cfile"), "c");

		tar = new TarOutputStream(tarFile, true);
		tar.putNextEntry(new TarEntry(bFile, "bfile"));
		Files.copy(bFile, tar);
        tar.putNextEntry(new TarEntry(cFile, "cfile"));
        Files.copy(cFile, tar);
		tar.close();

		untar();

		assertInEqualsOut();
	}

	/// Make sure that the contents of the input & output dirs are identical.
	private void assertInEqualsOut() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		assertEquals(inDir.toFile().list().length, outDir.toFile().list().length);
		for (File in : inDir.toFile().listFiles()) {
		    assertThat(in.toPath()).hasSameTextualContentAs(outDir.resolve(in.getName()));
		}
	}

	private void untar() throws FileNotFoundException, IOException {
		try (TarInputStream in = new TarInputStream(Files.newInputStream(dir.resolve("tar.tar")))) {
			TarEntry entry;

			while ((entry = in.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[2048];
				try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(outDir + "/" + entry.getName()))) {
					while ((count = in.read(data)) != -1) {
						dest.write(data, 0, count);
					}
				}
			}
		}
	}

}