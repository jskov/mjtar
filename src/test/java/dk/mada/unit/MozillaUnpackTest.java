package dk.mada.unit;

import dk.mada.fixture.ExternalTestData;
import dk.mada.mjtar.TarEntry;
import dk.mada.mjtar.TarInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fixture to help create test data.
 */
public class MozillaUnpackTest {

    /**
     * Runs as a java program. Select the variant you want to use first.
     *
     * @param args ignored command line arguments
     */
    public static void main(String[] args) {
        new MozillaUnpackTest().extractMozilla();
    }

    /**
     * Captured octal method input from Mozilla tar file by adding these lines at the top of Octal.parseOctal:
     * <pre>
     *  byte[] input = Arrays.copyOfRange(header, offset, offset + length);
     *  System.out.println("new byte[] { " + Arrays.toString(input).replace("[", "").replace("]", "") + "},");
     * </pre>
     *
     * Capture the output, filter for new, sort uniquely.
     */
    private void extractMozilla() {
        try {
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "extract-mozilla");
            try (InputStream is = Files.newInputStream(ExternalTestData.getMozillaTar());
                    BufferedInputStream bis = new BufferedInputStream(is);
                    TarInputStream tis = new TarInputStream(bis)) {
                untar(tis, tmp.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void untar(TarInputStream tis, String destFolder) throws IOException {
        BufferedOutputStream dest = null;
        byte data[] = new byte[16_000];

        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {
            int count;

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
}
