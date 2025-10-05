// COURSE: CS 645 - SECURYITY & PRIVACY IN COMPUTER SYSTEMS
// PROJECT: 1 (PART 1)
// TEAM MEMBERS: 2
// MEMBERS: Suraj Kumar Ojha (UCID: so299), Zane Xu (UCID: zx4)




import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Cracker {

    public static void main(String[] args) {
        Path shadowPath = Paths.get("shadow");
        Path dictPath   = Paths.get("common-passwords.txt");

        List<String> dictionary;
        try {
            dictionary = Files.readAllLines(dictPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading common-passwords.txt: " + e.getMessage());
            return;
        }

        // Clean dictionary (trim & remove empties)
        for (int i = 0; i < dictionary.size(); i++) {
            dictionary.set(i, dictionary.get(i).trim());
        }
        dictionary.removeIf(String::isEmpty);

        // long attempts = 0L;

        try (BufferedReader br = Files.newBufferedReader(shadowPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Parse: username:shash:...
                String[] fields = line.split(":", 3);
                if (fields.length < 2) {
                    System.err.println("Skipping malformed shadow line: " + line);
                    continue;
                }
                String username = fields[0];
                String shash = fields[1];

                // Only handle MD5-crypt ($1$) entries here
                if (!shash.startsWith("$1$")) continue;

                // Extract salt (between $1$ and next $)
                int saltBegin = shash.indexOf("$1$") + 3;
                int saltEnd = shash.indexOf('$', saltBegin);
                if (saltEnd == -1) {
                    System.err.println("Skipping malformed shash (missing second $): " + shash);
                    continue;
                }
                String salt = shash.substring(saltBegin, saltEnd);

                // Extract the stored encoded-hash (the part after the final $)
                String storedEncodedHash = shash.substring(shash.lastIndexOf('$') + 1);

                for (String password : dictionary) {
                    // attempts++;

                    // Call provided MD5Shadow.crypt(password, salt)
                    String candidate = MD5Shadow.crypt(password, salt);
                    if (candidate == null) continue;

                    // If crypt returned full form, extract encoded part; otherwise assume it's the encoded part.
                    String candidateEncoded;
                    if (candidate.startsWith("$1$")) {
                        int lastDollar = candidate.lastIndexOf('$');
                        if (lastDollar == -1 || lastDollar == candidate.length() - 1) {
                            // malformed return; skip
                            continue;
                        }
                        candidateEncoded = candidate.substring(lastDollar + 1);
                    } else {
                        candidateEncoded = candidate;
                    }

                    // Compare encoded parts (case-sensitive; crypt uses its own base64 alphabet)
                    if (candidateEncoded.equals(storedEncodedHash)) {
                        System.out.println(username + ":" + password);
                        break;
                    }

                }

            }

        } catch (IOException e) {
            System.err.println("Error reading shadow file: " + e.getMessage());
        }
    }
}
