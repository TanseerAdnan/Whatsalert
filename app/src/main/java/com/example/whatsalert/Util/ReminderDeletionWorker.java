package com.example.whatsalert.Util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Worker class that handles the deletion of reminders in the background using WorkManager.
 * It retrieves the reminder ID from the input data, then deletes the corresponding reminder from the database.
 * The result of the operation is logged, and a success or failure status is returned based on whether the deletion was successful.
 *
 * This class is typically scheduled to run as a background task, ensuring that the reminder is deleted at the appropriate time,
 * even if the app is not actively running.
 */
public class ReminderDeletionWorker extends Worker {

    private static final String TAG = "ReminderDeletionWorker";

    public ReminderDeletionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the reminder ID from input data
        int reminderId = getInputData().getInt("reminder_id", -1);

        if (reminderId != -1) {
            DBOperations dbOperations = new DBOperations(getApplicationContext());
            dbOperations.deleteReminderById(reminderId);
            Log.i(TAG, "Reminder deleted successfully by WorkManager");
            return Result.success();
        } else {
            Log.e(TAG, "Invalid reminder ID for deletion");
            return Result.failure();
        }
    }
}
