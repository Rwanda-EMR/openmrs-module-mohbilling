package org.openmrs.module.mohbilling.irembo.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SignatureHelper {
    private final String secretKey;

    public SignatureHelper(String apiKey) {
        this.secretKey = apiKey;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static boolean timingSafeEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    

    public boolean verifySignature(String payload, String signatureHeader) {
        // Step 1: Extract the timestamp and signatures from the header
        String[] elements = signatureHeader.split(",");
        String timestamp = null;
        String signatureHash = null;

        for (String element : elements) {
            String[] parts = element.trim().split("=");
            if (parts[0].equals("t")) {
                timestamp = parts[1];
            } else if (parts[0].equals("s")) {
                signatureHash = parts[1];
            }
        }

        if (timestamp == null || signatureHash == null) {
            return false;
        }

        // Step 2: Prepare the signed_payload string
        String signedPayload = timestamp + "#" + payload;
        // Step 3: Determine the expected signature
        String expectedSignature;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(this.secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            expectedSignature = bytesToHex(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }

        // Compare signatures using a timing-safe comparison
        return timingSafeEqual(hexStringToByteArray(expectedSignature), hexStringToByteArray(signatureHash));
    }
}