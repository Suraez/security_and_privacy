// COURSE: CS 645 - SECURITY & PRIVACY IN COMPUTER SYSTEMS
// PROJECT: 1 (PART 1)
// TEAM MEMBERS: 2
// MEMBERS: Suraj Kumar Ojha (UCID: so299), Zane Xu (UCID: zx4)

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class Test2 {

    // Hard-code the directory containing sub-directories with .txt files
    private static final String DICT_DIR = "/home/suraj/courses/CS_645/project/project_1/code/test_dir"; // <-- change this
    // Hard-code shadow file path (relative or absolute)
    private static final String SHADOW_PATH = "shadow"; // <-- change if needed
    // Hard-code log file path
    private static final String LOG_PATH = "cracker.log"; // <-- change if needed

    public static void main(String[] args) {
        // Redirect stdout & stderr to a log file (append mode), UTF-8
        PrintStream logStream = null;
        try {
            logStream = new PrintStream(new FileOutputStream(LOG_PATH, true), true, "UTF-8");
            System.setOut(logStream);
            System.setErr(logStream);
        } catch (IOException e) {
            System.err.println("Failed to open log file '" + LOG_PATH + "': " + e.getMessage());
            // If we can't open log file, continue using console streams (or exit if you prefer)
            logStream = null;
        }

        // Ensure logStream is closed on JVM shutdown (including System.exit)
        final PrintStream finalLogStream = logStream;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalLogStream != null) {
                finalLogStream.flush();
                finalLogStream.close();
            }
        }));

        Path dictDir = Paths.get(DICT_DIR);
        Path shadowPath = Paths.get(SHADOW_PATH);

        if (!Files.exists(dictDir) || !Files.isDirectory(dictDir)) {
            System.err.println("Dictionary path is not a directory or does not exist: " + dictDir);
            System.exit(2);
        }
        if (!Files.exists(shadowPath) || !Files.isRegularFile(shadowPath)) {
            System.err.println("Shadow file not found: " + shadowPath);
            System.exit(2);
        }

        // Read and iterate shadow entries one-by-one
        try (BufferedReader shadowBr = Files.newBufferedReader(shadowPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = shadowBr.readLine()) != null) {
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

                // Walk dictDir recursively and test each .txt file
                try (Stream<Path> files = Files.walk(dictDir)) {
                    files
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                        .sorted(Comparator.naturalOrder()) // stable order; optional
                        .forEach(p -> {
                            // For each file, read and test lines
                            try (BufferedReader dictBr = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                                String passLine;
                                while ((passLine = dictBr.readLine()) != null) {
                                    passLine = passLine.trim();
                                    if (passLine.isEmpty()) continue;

                                    String candidate = MD5Shadow.crypt(passLine, salt);
                                    if (candidate == null) continue;

                                    String candidateEncoded;
                                    if (candidate.startsWith("$1$")) {
                                        int lastDollar = candidate.lastIndexOf('$');
                                        if (lastDollar == -1 || lastDollar == candidate.length() - 1) {
                                            continue; // malformed
                                        }
                                        candidateEncoded = candidate.substring(lastDollar + 1);
                                    } else {
                                        candidateEncoded = candidate;
                                    }

                                    if (candidateEncoded.equals(storedEncodedHash)) {
                                        // Found match — print and exit entire program immediately
                                        System.out.println(username + ":" + passLine);
                                        // flush log and exit (shutdown hook will close PrintStream)
                                        System.exit(0);
                                    }
                                }
                            } catch (IOException e) {
                                System.err.println("Warning: couldn't read dictionary file " + p + ": " + e.getMessage());
                            }
                        });
                } catch (IOException e) {
                    System.err.println("Error walking dictionary directory: " + e.getMessage());
                    System.exit(1);
                }
                // If we reach here, no match for this shadow entry; continue to next shadow line
            }

            // After scanning all shadow entries and dictionary files
            System.out.println("No password from the dictionary directory matched any shadow entry.");
        } catch (IOException e) {
            System.err.println("Error reading shadow file: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
