package util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SimpleCrypto {

    // keep this secret key in code or another secure place
    private static final String KEY = "My$Super#Key123";

    public static String encrypt(String plainText) {
        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[textBytes.length];

        for (int i = 0; i < textBytes.length; i++) {
            result[i] = (byte) (textBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        return Base64.getEncoder().encodeToString(result);
    }

    public static String decrypt(String encoded) {
        byte[] encBytes = Base64.getDecoder().decode(encoded);
        byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[encBytes.length];

        for (int i = 0; i < encBytes.length; i++) {
            result[i] = (byte) (encBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        return new String(result, StandardCharsets.UTF_8);
    }
}
