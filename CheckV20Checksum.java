import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;

/**
 * Calculates CRC32 checksum of V20 migration to verify it matches database record.
 * Expected: -815006746
 */
public class CheckV20Checksum {
    public static void main(String[] args) throws IOException {
        String filePath = "backend/src/main/resources/db/migration/V20__add_performance_indexes.sql";
        
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        
        long checksumValue = crc32.getValue();
        // Flyway stores as signed int, not unsigned
        int signedChecksum = (int) checksumValue;
        
        System.out.println("File: " + filePath);
        System.out.println("File size: " + bytes.length + " bytes");
        System.out.println("CRC32 (unsigned): " + checksumValue);
        System.out.println("CRC32 (signed): " + signedChecksum);
        System.out.println("Expected (from DB): -815006746");
        System.out.println("Match: " + (signedChecksum == -815006746));
        
        if (signedChecksum == -815006746) {
            System.out.println("\n✓ CHECKSUM MATCHES - V20 is correct!");
            System.exit(0);
        } else {
            System.out.println("\n✗ CHECKSUM MISMATCH");
            System.out.println("Difference: " + (signedChecksum - (-815006746)));
            System.exit(1);
        }
    }
}
