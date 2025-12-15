package com.riege.rmc.minecraft.protocol;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public final class EncryptionUtils {

    public static byte[] generateSharedSecret() {
        byte[] secret = new byte[16];
        new SecureRandom().nextBytes(secret);
        return secret;
    }

    public static byte[] encryptRSA(byte[] publicKeyBytes, byte[] data) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    public static String generateServerIdHash(String serverId, byte[] sharedSecret, byte[] publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            digest.update(serverId.getBytes("ISO_8859_1"));
            digest.update(sharedSecret);
            digest.update(publicKey);

            byte[] hash = digest.digest();

            return new BigInteger(hash).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate server ID hash", e);
        }
    }
}
