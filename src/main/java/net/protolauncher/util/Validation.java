package net.protolauncher.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides various utilities regarding file validation.
 */
public class Validation {

    // An array of hex characters.
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Validates a file against the given sha1.
     *
     * @param path The file to check.
     * @param sha1 The sha1 to compare against.
     * @return <code>true</code> if the file exists and matches the given sha1, otherwise <code>false</code>.
     * @throws IOException Thrown if validating the file fails, usually due to sha1 creation.
     */
    public static boolean validate(@NotNull Path path, @NotNull String sha1) throws IOException {
        // Check file
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }

        // Generate sha1
        String filesha1 = Validation.createSha1(path);
        return filesha1.equalsIgnoreCase(sha1);
    }

    /**
     * Creates a SHA1 code from the given path.
     *
     * @param path The file to generate a SHA1 from.
     * @return The SHA1 as a string.
     * @throws IOException Thrown if any part of the SHA1 process goes wrong.
     */
    public static String createSha1(Path path) throws IOException  {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        InputStream fis = Files.newInputStream(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return bytesToHex(digest.digest());
    }

    /**
     * Converts the given byte array to a hexadecimal string.
     *
     * @param bytes The bytes to convert.
     * @return A hexadecimal string representing the given bytes.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

}
