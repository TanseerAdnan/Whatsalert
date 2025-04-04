package com.example.whatsalert.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.whatsalert.Model.Reminder;
import com.example.whatsalert.R;
import com.example.whatsalert.Repository.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DBOperations {

    private static final String TAG = "DBOperations";

    private Context context;
    private String secretKey = SecureUtils.getSecretKey();

    public DBOperations(Context context) {
        this.context = context;
    }

    /**
     * Saves a reminder to the database with encrypted message and contact number.
     * - Combines the day, month, and year into a date string.
     * - Encrypts the message and contact number before saving.
     * - Calculates the deletion time (1 hour after the reminder time).
     * - Inserts the reminder into the database and sets an alarm for the reminder.
     * - Displays a Snackbar on success and navigates to the HomeFragment.
     *
     * @param day The day of the reminder (e.g., "01").
     * @param month The month of the reminder (e.g., "12").
     * @param year The year of the reminder (e.g., "2024").
     * @param time The time of the reminder (e.g., "03:30 PM").
     * @param message The message for the reminder.
     * @param contactNumber The contact number associated with the reminder.
     * @param contactName The name associated with the contact number.
     */
    public void saveReminder(String day, String month, String year, String time, String message, String contactNumber, String contactName) {
        Log.i(TAG, "saveReminder: Entered Save Reminder Function");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        Log.i(TAG, "saveReminder: " + dbHelper);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.i(TAG, "saveReminder: " + db);

        try {
            // Add a log before encryption
            Log.d(TAG, "Before encryption: message = " + message + ", contactNumber = " + contactNumber);

            // Encrypt message and contact number
            String encryptedMessage = EncryptionUtil.encrypt(message, secretKey);
            String encryptedContactNumber = EncryptionUtil.encrypt(contactNumber, secretKey);

            Log.d(TAG, "After encryption: Encrypted Message: " + encryptedMessage);
            Log.d(TAG, "After encryption: Encrypted Contact Number: " + encryptedContactNumber);

            // Combine the day, month, and year into a single date string (e.g., "YYYY-MM-DD")
            String date = year + "-" + month + "-" + day;

            // Calculate the deletion_time (1 hour after the reminder time)
            String deletionTime = calculateDeletionTime(time);

            // Log input values before storing
            Log.d(TAG, "Saving reminder:");
            Log.d(TAG, "Date: " + date);
            Log.d(TAG, "Time: " + time);
            Log.d(TAG, "Message (unencrypted): " + message);
            Log.d(TAG, "Contact Number (unencrypted): " + contactNumber);
            Log.d(TAG, "Calculated Deletion Time: " + deletionTime);

            // Insert data into the reminders table
            ContentValues values = new ContentValues();
            values.put("date", date);  // Store combined date in ISO format
            values.put("time", time);  // Store time in the format "HH:MM AM/PM"
            values.put("message", encryptedMessage);
            values.put("contact_number", encryptedContactNumber);
            values.put("deletion_time", deletionTime);
            values.put("contact_name", contactName);

            // Log before inserting into the database
            Log.d(TAG, "Before inserting into the database");
            long newRowId = db.insert("reminders", null, values);
            Log.d(TAG, "New row ID: " + newRowId);
            // Once the reminder is saved to the database, set an alarm for the reminder
            // Once the reminder is saved to the database, set an alarm for the reminder and pass newRowId
            setReminderAlarm(day, month, year, time, message, (int) newRowId, contactName, contactNumber);

            if (newRowId != -1) {
                // Use runOnUiThread for Toast (if called from a background thread)
                if (context instanceof FragmentActivity) {
                    ((FragmentActivity) context).runOnUiThread(() -> {
                        // Assuming you have access to the root view
                        View rootView = ((Activity) context).findViewById(android.R.id.content); // Root view of Activity
                        // Use Snackbar
                        Snackbar.make(rootView, "Reminder saved!", Snackbar.LENGTH_LONG).show();

                        // Use NavController to navigate to HomeFragment
                        NavController navController = Navigation.findNavController((FragmentActivity) context, R.id.nav_host_fragment_content_home_screen);
                        navController.navigate(R.id.action_ReminderFragment_to_HomeFragment); // Use action ID, not view ID
                    });
                }
            }

        } catch (Exception e) {
            // Add a log for the exception message
            Log.e(TAG, "Error in saveReminder: " + e.getMessage(), e);
        } finally {
            db.close();
        }
    }


    /**
     * Retrieves all reminders from the SQLite database with decrypted message and contact number.
     * - Decrypts the message and contact number before storing them in the Reminder object.
     * - Sets the "isNotified" property based on the value from the database.
     * - Returns a list of Reminder objects with their corresponding details (id, date, time, message, contact number, contact name).
     *
     * @return A list of Reminder objects with decrypted details from the database.
     */
    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("reminders", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    // Retrieve date, time, encrypted message, and contact number
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                    @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                    @SuppressLint("Range") String encryptedMessage = cursor.getString(cursor.getColumnIndex("message"));
                    @SuppressLint("Range") String encryptedContactNumber = cursor.getString(cursor.getColumnIndex("contact_number"));
                    @SuppressLint("Range") boolean isNotified = cursor.getInt(cursor.getColumnIndex("is_notified")) == 1;
                    @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex("contact_name"));

                    // Decrypt message and contact number
                    String message = EncryptionUtil.decrypt(encryptedMessage, secretKey);
                    String contactNumber = EncryptionUtil.decrypt(encryptedContactNumber, secretKey);

                    // Create a new Reminder object
                    Reminder reminder = new Reminder(id,time, date, contactNumber, message,contactName );

                    // Set the isNotified property
                  reminder.setNotified(isNotified);

                    // Add the Reminder object to the list
                    reminders.add(reminder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return reminders;
    }


    /**
     * Sets an alarm for the reminder using WorkManager.
     * - Combines the date and time into a single string, parses it, and calculates the delay for the reminder.
     * - If the delay is valid, it schedules a OneTimeWorkRequest with the reminder details.
     * - The alarm triggers a worker (ReminderWorker) at the scheduled time to process the reminder.
     * - Handles cases where the reminder time is in the past and logs the error.
     *
     * @param day The day of the reminder.
     * @param month The month of the reminder.
     * @param year The year of the reminder.
     * @param time The time of the reminder in "HH:MM AM/PM" format.
     * @param message The message associated with the reminder.
     * @param reminderId The unique identifier for the reminder.
     * @param contactName The contact name associated with the reminder.
     * @param contactNumber The contact number associated with the reminder.
     */
    public void setReminderAlarm(String day, String month, String year, String time, String message, int reminderId, String contactName, String contactNumber) {
        try {
            // Combine date and time into a single string and parse it
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            String dateTimeString = year + "-" + month + "-" + day + " " + time;
            Date dateTime = dateTimeFormat.parse(dateTimeString);

            if (dateTime != null) {
                long triggerAtMillis = dateTime.getTime();

                // Calculate delay
                long currentTime = System.currentTimeMillis();
                long delay = triggerAtMillis - currentTime;

                if (delay <= 0) {
                    Log.e(TAG, "Invalid reminder time. Cannot schedule a past reminder.");
                    return;
                }

                // Create input data for the worker
                Data inputData = new Data.Builder()
                        .putString("message", message)
                        .putString("contact_name",contactName)
                        .putInt("reminder_id", reminderId)
                        .putString("contact_number",contactNumber)
                        .build();

                // Create a OneTimeWorkRequest
                OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(String.valueOf(reminderId))
                        .setInputData(inputData)
                        .build();

                WorkManager.getInstance(context).enqueue(reminderWork);
                Log.d(TAG, "Reminder scheduled successfully with WorkManager.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setting reminder alarm: " + e.getMessage(), e);
        }
    }


    /**
     * Deletes a reminder from the database by its unique ID.
     * - Checks if the reminder exists in the database using the provided ID.
     * - If the reminder exists, deletes it from the "reminders" table.
     * - Logs the number of rows deleted or an error message if the reminder is not found.
     *
     * @param reminderId The unique identifier for the reminder to be deleted.
     */
    public void deleteReminderById(int reminderId) {
        Log.d(TAG, "Deleting reminder with ID: " + reminderId);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("reminders", null, "id = ?", new String[]{String.valueOf(reminderId)}, null, null, null);
        if (cursor.moveToFirst()) {
            int rowsDeleted = db.delete("reminders", "id = ?", new String[]{String.valueOf(reminderId)});
            Log.d(TAG, "Rows deleted: " + rowsDeleted);
        } else {
            Log.e(TAG, "Reminder ID not found in the database: " + reminderId);
        }
        cursor.close();
        db.close();
    }

    /**
     * Updates the reminder in the database to mark it as notified.
     * - Sets the "is_notified" column to 1 (true) for the reminder with the given ID.
     * - This function assumes the "is_notified" column exists in the database schema.
     *
     * @param reminderId The unique identifier for the reminder to be updated.
     */
    public void updateReminderAsNotified(int reminderId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("is_notified", 1);  // Set 'is_notified' to true (you'll need to add this column to the DB schema)

        db.update("reminders", values, "id = ?", new String[]{String.valueOf(reminderId)});
        db.close();
    }


    /**
     * Retrieves the deletion time for a specific reminder from the database using its ID.
     *
     * @param reminderId The ID of the reminder whose deletion time is to be fetched.
     * @return The deletion time of the reminder, or null if the reminder is not found.
     */
    @SuppressLint("Range")
    public String getDeletionTimeById(int reminderId) {
        Log.d(TAG, "Fetching deletion time for reminder with ID: " + reminderId);

        String deletionTime = null;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the database for the deletion time of the reminder
        Cursor cursor = db.query("reminders", new String[]{"deletion_time"}, "id = ?",
                new String[]{String.valueOf(reminderId)}, null, null, null);

        if (cursor.moveToFirst()) {
            // Retrieve the deletion time from the database
            deletionTime = cursor.getString(cursor.getColumnIndex("deletion_time"));
            Log.d(TAG, "Deletion time retrieved: " + deletionTime);
        } else {
            Log.e(TAG, "Reminder with ID " + reminderId + " not found.");
        }

        cursor.close();
        db.close();

        return deletionTime;
    }

    /**
     * Updates an existing reminder in the database, encrypting sensitive data (message and contact number) before saving.
     *
     * @param reminderId The ID of the reminder to be updated.
     * @param values A ContentValues object containing the updated values for the reminder.
     * @return The number of rows updated in the database. Returns 0 if an error occurs.
     */
    public int updateReminder(int reminderId, ContentValues values) {
        Log.d(TAG, "Updating reminder with ID: " + reminderId);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Encrypt sensitive data if provided in values
            if (values.containsKey("message")) {
                String encryptedMessage = EncryptionUtil.encrypt(values.getAsString("message"), secretKey);
                values.put("message", encryptedMessage);
            }
            if (values.containsKey("contact_number")) {
                String encryptedContactNumber = EncryptionUtil.encrypt(values.getAsString("contact_number"), secretKey);
                values.put("contact_number", encryptedContactNumber);
            }

            // Update the database
            int rowsUpdated = db.update("reminders", values, "id = ?", new String[]{String.valueOf(reminderId)});
            Log.d(TAG, "Rows updated: " + rowsUpdated);

            if (rowsUpdated > 0) {
                Log.d(TAG, "Reminder updated successfully");
                View rootView = ((Activity) context).findViewById(android.R.id.content); // Root view of Activity
                // Use Snackbar
                Snackbar.make(rootView, "Reminder updated successfully!", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(context, "Reminder updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Reminder with ID " + reminderId + " not found.");
                View rootView = ((Activity) context).findViewById(android.R.id.content); // Root view of Activity
                Snackbar.make(rootView, "Failed to update reminder. Reminder not found.", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(context, "Failed to update reminder. Reminder not found.", Toast.LENGTH_SHORT).show();
            }
            return rowsUpdated;  // Return the number of rows updated
        } catch (Exception e) {
            Log.e(TAG, "Error updating reminder: " + e.getMessage(), e);
            return 0;  // Return 0 in case of error
        } finally {
            db.close();
        }
    }

    // =============================================== HELPER FUNCTIONS =============================================

    /**
     * Calculates the deletion time for the reminder by adding 1 hour to the given reminder time.
     *
     * @param time The time of the reminder in "hh:mm a" format (e.g., "03:30 PM").
     * @return A string representing the deletion time, which is 1 hour after the reminder time, in the same format.
     *         Returns null if there is an error during parsing or calculation.
     */
    private String calculateDeletionTime(String time) {
        try {
            // Parse the reminder time into a Date object
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date reminderTime = timeFormat.parse(time);

            if (reminderTime != null) {
                // Add 1 hour to the reminder time
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(reminderTime);
                calendar.add(Calendar.HOUR, 1); // Add 1 hour

                // Return the deletion time as a formatted string
                return timeFormat.format(calendar.getTime());
            } else {
                return null;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error calculating deletion time: " + e.getMessage(), e);
            return null;
        }
    }
}