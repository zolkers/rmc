package com.riege.rmc.persistence;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class SecurityUtils {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public static byte[] encrypt(String plaintext) throws Exception {
        SecretKey key = deriveKey();
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV + ciphertext
        byte[] result = new byte[IV_SIZE + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, IV_SIZE);
        System.arraycopy(ciphertext, 0, result, IV_SIZE, ciphertext.length);

        return result;
    }

    public static String decrypt(byte[] encrypted) throws Exception {
        // Extract IV
        byte[] iv = Arrays.copyOfRange(encrypted, 0, IV_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(encrypted, IV_SIZE, encrypted.length);

        SecretKey key = deriveKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey() throws Exception {
        String machineId = getMachineIdentifier();
        byte[] salt = machineId.getBytes(StandardCharsets.UTF_8);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(machineId.toCharArray(), salt, 65536, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static String getMachineIdentifier() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String username = System.getProperty("user.name");
            return hostname + ":" + username;
        } catch (Exception e) {
            return "default:machine";
        }
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
