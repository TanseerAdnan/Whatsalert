package com.example.whatsalert.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;

public class WhatsappUtil {

    /**
     * Retrieves a sorted list of all WhatsApp contact names from the device's contacts.
     * It queries the ContactsContract to get contacts that have a WhatsApp profile.
     *
     * @param context The application context.
     * @return A sorted list of WhatsApp contact names.
     */
    public static ArrayList<String> getWhatsappContactNames(Context context) {
        ArrayList<String> contactNames = new ArrayList<>();

        // Query to get contacts with WhatsApp profiles
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DISPLAY_NAME},
                ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{"vnd.android.cursor.item/vnd.com.whatsapp.profile"},
                null
        );

        if (cursor != null) {
            // Loop through the cursor to retrieve contact names
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                contactNames.add(name);
            }
            cursor.close();
        }

        // Sort the list alphabetically (A to Z) in a case-insensitive manner
        Collections.sort(contactNames, String.CASE_INSENSITIVE_ORDER);

        return contactNames;
    }

    /**
     * Retrieves the phone number of a WhatsApp contact given their name.
     * It queries the ContactsContract to get the phone number associated with the specified WhatsApp contact name.
     *
     * @param context The application context.
     * @param name The name of the contact.
     * @return The phone number of the specified WhatsApp contact, or null if not found.
     */
    @SuppressLint("Range")
    public static String getContactNumberFromName(Context context, String name) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.Data.DISPLAY_NAME + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{name, "vnd.android.cursor.item/vnd.com.whatsapp.profile"},
                null
        );

        String number = null;
        if (cursor != null) {
            // Check if a result is found and retrieve the number
            if (cursor.moveToFirst()) {
                number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            cursor.close();
        }

        return number;
    }

}
