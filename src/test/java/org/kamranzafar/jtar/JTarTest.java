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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dk.mada.mjtar.TarConstants;
import dk.mada.mjtar.TarEntry;
import dk.mada.mjtar.TarHeader;
import dk.mada.mjtar.TarInputStream;
import dk.mada.mjtar.TarOutputStream;
import dk.mada.mjtar.TarUtils;

class JTarTest {
	static final int BUFFER = 2048;

	private @TempDir Path dir;

	/// Tar the given folder.
	///
	/// @throws IOException if there is an IO error
	@Test
	void tar() throws IOException {
		OutputStream dest = Files.newOutputStream(dir.toAbsolutePath().resolve("tartest.tar"));
		TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

		Path tartest = dir.toAbsolutePath().resolve("tartest");
		Files.createDirectories(tartest);

		Files.writeString(tartest.resolve("one"), "HPeX2kD5kSTc7pzCDX");
		Files.writeString(tartest.resolve("two"), "gTzyuQjfhrnyX9cTBSy");
		Files.writeString(tartest.resolve("three"), "KG889vdgjPHQXUEXCqrr");
		Files.writeString(tartest.resolve("four"), "CNBDGjEJNYfms7rwxfkAJ");
		Files.writeString(tartest.resolve("five"), "tT6mFKuLRjPmUDjcVTnjBL");
		Files.writeString(tartest.resolve("six"), "jrPYpzLfWB5vZTRsSKqFvVj");

		tarFolder(null, dir.toAbsolutePath().toString() + "/tartest/", out);

		out.close();

		assertEquals(TarUtils.calculateTarSize(dir.toAbsolutePath().resolve("tartest")), Files.size(dir.toAbsolutePath().resolve("tartest.tar")));
	}

	/// Untar the tar file.
	///
    /// @throws IOException if there is an IO error
	@Test
	void untarTarFile() throws IOException {
		Path destFolder = dir.resolve("untartest");
		Files.createDirectories(destFolder);

		Path zf = Paths.get("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(Files.newInputStream(zf)));
		untar(tis, destFolder.toAbsolutePath().toString());

		tis.close();

		assertFileContents(destFolder);
	}

	/// Untar the tar file.
	///
    /// @throws IOException if there is an IO error
	@Test
	void untarTarFileDefaultSkip() throws IOException {
		Path destFolder = dir.resolve("untartest/skip");
		Files.createDirectories(destFolder);

		Path zf = Paths.get("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(Files.newInputStream(zf)));
		tis.setDefaultSkip(true);
		untar(tis, destFolder.toAbsolutePath().toString());

		tis.close();

		assertFileContents(destFolder);
	}

	/// Untar the gzipped-tar file
	///
    /// @throws IOException if there is an IO error
	@Test
	void untarTGzFile() throws IOException {
		Path destFolder = dir.resolve("untargztest");
		Path zf = Paths.get("src/test/resources/tartest.tar.gz");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(zf))));

		untar(tis, destFolder.toAbsolutePath().toString());

		tis.close();

		assertFileContents(destFolder);
	}


	@Test
	void testOffset() throws IOException {
		Path destFolder = dir.resolve("untartest");
		Files.createDirectories(destFolder);

		Path zf = Paths.get("src/test/resources/tartest.tar");

		TarInputStream tis = new TarInputStream(new BufferedInputStream(Files.newInputStream(zf)));
		tis.getNextEntry();
		assertEquals(TarConstants.HEADER_BLOCK, tis.getCurrentOffset());
		tis.getNextEntry();
		TarEntry entry = tis.getNextEntry(); 
		// All of the files in the tartest.tar file are smaller than DATA_BLOCK
		assertEquals(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2, tis.getCurrentOffset());
		tis.close();
		
		RandomAccessFile rif = new RandomAccessFile(zf.toFile(), "r");
		rif.seek(TarConstants.HEADER_BLOCK * 3 + TarConstants.DATA_BLOCK * 2);
		byte[] data = new byte[(int)entry.getSize()];
		rif.read(data);
		assertEquals("gTzyuQjfhrnyX9cTBSy", new String(data, "UTF-8"));
		rif.close();
	}
	
	private void untar(TarInputStream tis, String destFolder) throws IOException {
		BufferedOutputStream dest = null;

		TarEntry entry;
		while ((entry = tis.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry.getName());
			int count;
			byte data[] = new byte[BUFFER];

			if (entry.isDirectory()) {
				new File(destFolder + "/" + entry.getName()).mkdirs();
				continue;
			} else {
				int di = entry.getName().lastIndexOf('/');
				if (di != -1) {
					new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
			dest = new BufferedOutputStream(fos);

			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}

	void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
		BufferedInputStream origin = null;
		File f = new File(path);
		String files[] = f.list();

		// is file
		if (files == null) {
			files = new String[1];
			files[0] = f.getName();
		}

		parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName() + "/");

		for (int i = 0; i < files.length; i++) {
			System.out.println("Adding: " + files[i]);
			File fe = f;
			byte data[] = new byte[BUFFER];

			if (f.isDirectory()) {
				fe = new File(f, files[i]);
			}

			if (fe.isDirectory()) {
				String[] fl = fe.list();
				if (fl != null && fl.length != 0) {
					tarFolder(parent, fe.getPath(), out);
				} else {
					TarEntry entry = new TarEntry(fe.toPath(), parent + files[i] + "/");
					out.putNextEntry(entry);
				}
				continue;
			}

			FileInputStream fi = new FileInputStream(fe);
			origin = new BufferedInputStream(fi);
			TarEntry entry = new TarEntry(fe.toPath(), parent + files[i]);
			out.putNextEntry(entry);

			int count;

			while ((count = origin.read(data)) != -1) {
				out.write(data, 0, count);
			}

			out.flush();

			origin.close();
		}
	}

	@Test
	void fileEntry() throws IOException {
		String fileName = "file.txt";
		long fileSize = 14523;
		long modTime = System.currentTimeMillis() / 1000;
		int permissions = 0755;

		// Create a header object and check the fields
		TarHeader fileHeader = TarHeader.createHeader(fileName, fileSize, modTime, false, permissions);
		assertEquals(fileName, fileHeader.name.toString());
		assertEquals(TarHeader.LF_NORMAL, fileHeader.linkFlag);
		assertEquals(fileSize, fileHeader.size);
		assertEquals(modTime, fileHeader.modTime);
		assertEquals(permissions, fileHeader.mode);

		// Create an entry from the header
		TarEntry fileEntry = new TarEntry(fileHeader);
		assertEquals(fileName, fileEntry.getName());

		// Write the header into a buffer, create it back and compare them
		byte[] headerBuf = new byte[TarConstants.HEADER_BLOCK];
		fileEntry.writeEntryHeader(headerBuf);
		TarEntry createdEntry = new TarEntry(headerBuf);
		assertTrue(fileEntry.equals(createdEntry));
	}

	private void assertFileContents(Path destFolder) throws IOException {
		assertEquals("HPeX2kD5kSTc7pzCDX", Files.readString(destFolder.resolve("tartest/one")));
		assertEquals("gTzyuQjfhrnyX9cTBSy", Files.readString(destFolder.resolve("tartest/two")));
		assertEquals("KG889vdgjPHQXUEXCqrr", Files.readString(destFolder.resolve("tartest/three")));
		assertEquals("CNBDGjEJNYfms7rwxfkAJ", Files.readString(destFolder.resolve("tartest/four")));
		assertEquals("tT6mFKuLRjPmUDjcVTnjBL", Files.readString(destFolder.resolve("tartest/five")));
		assertEquals("jrPYpzLfWB5vZTRsSKqFvVj", Files.readString(destFolder.resolve("tartest/six")));
	}
}