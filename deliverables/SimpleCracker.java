// COURSE: CS 645 - SECURITY & PRIVACY IN COMPUTER SYSTEMS
// PROJECT: 1 (PART 1)
// TEAM MEMBERS: 2
// MEMBERS: Suraj Kumar Ojha (UCID: so299), Zane Xu (UCID: zx4)



import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SimpleCracker {

    public static void main(String[] args) {
        Path shadowPath = Paths.get("shadow-simple");
        Path dictPath   = Paths.get("common-passwords.txt");

        List<String> dictionary;
        try {
            dictionary = Files.readAllLines(dictPath, StandardCharsets.UTF_8);
            // System.out.println("=== CONTENTS OF common-passwords.txt ===");
            // for (String pw : dictionary) {
            //     // Print each dictionary line exactly as read (no additional trimming).
            //     System.out.println(pw);
            // }
            // System.out.println("=== END OF common-passwords.txt ===\n");
        } catch (IOException e) {
            System.err.println("Error reading common-passwords.txt: " + e.getMessage());
            return;
        }


        try (BufferedReader br = Files.newBufferedReader(shadowPath, StandardCharsets.UTF_8)) {
            String line;
            MessageDigest md = MessageDigest.getInstance("MD5");

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // expected format: username:salt:hash
                String[] parts = line.split(":", 3);
                if (parts.length != 3) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }
                String username = parts[0];
                String salt     = parts[1];
                String storedHexHash = parts[2];

                for (String password : dictionary) {
                    byte[] inputBytes = (salt + password).getBytes(StandardCharsets.UTF_8);
                    byte[] digest = md.digest(inputBytes);
                    String hex = toHex(digest);

                    // Compare ignoring case to be robust against hex-case differences in file
                    if (hex.equalsIgnoreCase(storedHexHash)) {
                        System.out.println(username + ":" + password);
                        break; 
                    }
                    // reset MessageDigest for next digest (digest() already reset internal state)
                    // (no explicit reset needed for MessageDigest after digest())
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // MD5 should always exist in a standard JRE, but handle anyway
            System.err.println("MD5 algorithm not available: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading shadow-simple: " + e.getMessage());
        }
    }

    // already given in problem statement

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
