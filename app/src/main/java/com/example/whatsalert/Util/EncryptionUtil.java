package com.example.whatsalert.Util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import java.security.SecureRandom;

public class EncryptionUtil {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // Recommended IV length for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

    /**
     * Encrypts the given data using AES encryption in GCM mode with a randomly generated IV.
     * The IV is concatenated with the encrypted data and the result is encoded in Base64.
     *
     * @param data The plaintext data to be encrypted.
     * @param secretKey The secret key used for AES encryption.
     * @return The encrypted data as a Base64 encoded string.
     * @throws Exception If any error occurs during encryption.
     */
    public static String encrypt(String data, String secretKey) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generate a random IV

        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(AES_GCM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes());

        // Concatenate IV and ciphertext, then encode in Base64
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    /**
     * Decrypts the given Base64 encoded encrypted data using AES decryption in GCM mode.
     * The IV is extracted from the beginning of the data and used in the decryption process.
     *
     * @param encryptedData The Base64 encoded encrypted data to be decrypted.
     * @param secretKey The secret key used for AES decryption.
     * @return The decrypted plaintext data as a string.
     * @throws Exception If any error occurs during decryption.
     */
    public static String decrypt(String encryptedData, String secretKey) throws Exception {
        byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);

        // Extract IV from the beginning of the decoded data
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedBytes, 0, iv, 0, GCM_IV_LENGTH);

        byte[] encryptedBytes = new byte[decodedBytes.length - GCM_IV_LENGTH];
        System.arraycopy(decodedBytes, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(AES_GCM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

}
