import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;

/**
 * Test multiple V20 variants to find which matches -815006746 or -1909340333
 */
public class FindChecksum {
    public static void main(String[] args) throws IOException {
        String[] files = {
            "backend/src/main/resources/db/migration/V20__add_performance_indexes.sql",
            "backend/src/main/resources/db/migration/V20_test1.sql",
            "backend/src/main/resources/db/migration/V20_test2.sql",
            "backend/src/main/resources/db/migration/V20_test3.sql",
            "V20_nocomments.sql"
        };
        
        for (String filePath : files) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                CRC32 crc32 = new CRC32();
                crc32.update(bytes);
                int signedChecksum = (int) crc32.getValue();
                
                System.out.println("\n" + filePath);
                System.out.println("  Size: " + bytes.length + " bytes");
                System.out.println("  Checksum: " + signedChecksum);
                System.out.println("  Match -815006746: " + (signedChecksum == -815006746));
                System.out.println("  Match -1909340333: " + (signedChecksum == -1909340333));
            } catch (Exception e) {
                System.out.println("\n" + filePath + ": NOT FOUND");
            }
        }
    }
}
