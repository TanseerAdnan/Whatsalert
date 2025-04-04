package com.example.whatsalert.Util;

import android.net.Uri;
import android.util.Base64;

/**
 * Utility class to handle secure encoding and decoding operations.
 * This class provides methods to generate a WhatsApp URL with an encoded phone number and message,
 * as well as retrieving a secret key by decoding it from Base64 at runtime.
 */
public class SecureUtils {

    private static final String ENCODED_URL = "aHR0cHM6Ly93YS5tZS8="; // Base64 encoded "https://wa.me/"
    private static final String ENCODED_SECRET_KEY = "MTIzNDU2Nzg5MDEyMzQ1Ng==";

    /**
     * Decodes the Base64 encoded URL and generates a WhatsApp URL with the provided phone number and message.
     *
     * @param phoneNumber The phone number to send the message to.
     * @param message The message to be sent.
     * @return The generated WhatsApp URL with the encoded message.
     */
    public static String getWhatsAppUrl(String phoneNumber, String message) {
        String baseUrl = new String(Base64.decode(ENCODED_URL, Base64.DEFAULT)); // Decode at runtime
        return baseUrl + phoneNumber + "?text=" + Uri.encode(message);
    }

    /**
     * Decodes the Base64 encoded secret key and returns the decoded value.
     *
     * @return The decoded secret key.
     */
    public static String getSecretKey() {
        return new String(Base64.decode(ENCODED_SECRET_KEY, Base64.DEFAULT)); // Decode secret key at runtime
    }
}
