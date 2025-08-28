package za.co.backspace.whatsappintegration.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;

public class AuthUtil {
    private static Map<String, String> userTokenCache = new HashMap<>();

    private static final String ALGORITHM = "AES";
    private final SecretKey secretKey;

    public AuthUtil(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting token", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting token", e);
        }
    }

    public String checkUserToken(String token, VTigerApiClient vTigerApiClient) {
        var user = userTokenCache.get(token);
        if (user != null) {
            return user;
        }
        String decrypted = decrypt(token);
        String[] parts = decrypted.split(":");
        String username = parts[0];
        String accessKey = parts[1];
        var validUser = vTigerApiClient.getMyUser(username, accessKey);
        userTokenCache.put(token, username);
        return username;
    }
}
