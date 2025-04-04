package com.example.whatsalert.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.whatsalert.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Worker class that handles the reminder notification and scheduled deletion in the background using WorkManager.
 * This class performs the following tasks:
 * 1. Sends a notification with the reminder message to the user.
 * 2. Opens a WhatsApp chat pre-filled with the reminder message upon notification click.
 * 3. Schedules reminder deletion based on the specified deletion time, if provided, by using another Worker (ReminderDeletionWorker).
 * 4. Updates the reminder status to indicate that it has been notified.
 *
 * The work is handled asynchronously, ensuring the app can notify the user and perform deletion operations even when the app is in the background.
 */
public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve data passed to the Worker
        Context context = getApplicationContext();
        String message = getInputData().getString("message");
        String contactName = getInputData().getString("contact_name");
        String contactNumber = getInputData().getString("contact_number");
        int reminderId = getInputData().getInt("reminder_id", -1);

        // Ensure valid input data is received
        if (message == null || reminderId == -1) {
            Log.e(TAG, "Invalid input data. Aborting.");
            return Result.failure();
        }

        Log.i(TAG, "Handling reminder: Sending notification");
        // Get the URI for the custom sound from the raw folder
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.reminder_sound);

        // Create and display the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a NotificationChannel for devices running Android Oreo and above
                NotificationChannel channel = new NotificationChannel(
                        "REMINDER_CHANNEL",
                        "Reminder Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setSound(soundUri, null); // Set the custom sound for the notification
                notificationManager.createNotificationChannel(channel);
            }

            // Build the WhatsApp URL with the pre-filled message
            String phoneNumber = contactNumber;
            String Sendmessage = message;
            String url = SecureUtils.getWhatsAppUrl(phoneNumber, Sendmessage);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url)); // Intent to open WhatsApp with the URL

            // Create PendingIntent to trigger when the notification is clicked
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "REMINDER_CHANNEL")
                    .setSmallIcon(R.drawable.whatsalert)
                    .setContentTitle("Reminder for " + contactName)  // Use the formatted title
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)  // Set the PendingIntent for the notification click action
                    .setAutoCancel(true)  // Automatically dismiss the notification when clicked
                    .setSound(soundUri);// Dismiss notification when clicked

            notificationManager.notify(reminderId, notificationBuilder.build());
            Log.i(TAG, "Notification sent successfully.");
        }

        // Handle reminder deletion logic
        DBOperations dbOperations = new DBOperations(context);
        String deletionTime = dbOperations.getDeletionTimeById(reminderId);

        if (deletionTime != null) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date deletionDate = timeFormat.parse(deletionTime);

                if (deletionDate != null) {
                    Calendar currentCalendar = Calendar.getInstance();
                    Calendar deletionCalendar = Calendar.getInstance();
                    deletionCalendar.setTime(deletionDate);

                    // Adjust the deletion calendar to the current day (same year, month, and day)
                    deletionCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
                    deletionCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
                    deletionCalendar.set(Calendar.DAY_OF_MONTH, currentCalendar.get(Calendar.DAY_OF_MONTH));

                    // Check if the current time has passed the deletion time
                    if (currentCalendar.after(deletionCalendar)) {
                        // Deletion time already passed
                        dbOperations.deleteReminderById(reminderId);
                    } else {
                        // Calculate delay in minutes
                        long delay = (deletionCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis()) / 1000;

                        // Schedule deletion with WorkManager
                        Data inputData = new Data.Builder()
                                .putInt("reminder_id", reminderId)
                                .build();

                        OneTimeWorkRequest deleteReminderWork = new OneTimeWorkRequest.Builder(ReminderDeletionWorker.class)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .setInputData(inputData)
                                .addTag(String.valueOf(reminderId))
                                .build();

                        // Enqueue the deletion work
                        WorkManager.getInstance(context).enqueue(deleteReminderWork);
                        Log.d(TAG, "Reminder deletion scheduled after delay: " + delay + " seconds");
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing deletion time: " + e.getMessage(), e);
            }
        } else {
            // If no deletion time is found for the reminder, log an error
            Log.e(TAG, "Deletion time not found for reminder ID: " + reminderId);
        }

        // Update the reminder status as notified
        dbOperations.updateReminderAsNotified(reminderId);

        return Result.success(); // Indicate that the work was successful
    }

}
