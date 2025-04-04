package com.example.whatsalert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.whatsalert.Util.Dialog;
import com.example.whatsalert.Util.Restarter;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.whatsalert.databinding.ActivityHomeScreenBinding;

public class Home_Screen extends AppCompatActivity {

    private static final String TAG = "Home_Screen";

    // Unique request code
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 102;

    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeScreenBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar);

        // Set up NavController and AppBarConfiguration
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_screen);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Debugging: Clear SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        Log.i(TAG,"isFirstTime: "+ isFirstTime);

        if (isFirstTime) {
            // Request notification permission first
            requestNotificationPermission(true);
        } else {
            // If not the first time, no need to request permission; handle normally
            showWelcomeDialogIfNeeded(false);
        }
    }

    /**
     * Called when the activity is resumed.
     * - Requests notification permission if needed.
     * - Checks if the contacts permission is granted.
     */
    @Override
    protected void onResume() {
        super.onResume();
        requestNotificationPermission(false);
        checkContactsPermission();
    }

    /**
     * Called when the activity is stopped.
     * - Checks if the activity is finishing (i.e., the app is removed from the recent tasks list).
     * - If the app is finishing, sends a broadcast to restart the service (e.g., Restarter).
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Check if the activity is being killed (app removed from recent tasks)
        if (isFinishing()) {
            // Send broadcast to restart the service (e.g., Restarter)
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_screen);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Requests the notification permission for Android 13 (API level 33) and above.
     * - If the permission is not granted, it prompts the user to allow notifications.
     * - If the device is running below Android 13, no permission is needed,
     *   and a custom permission dialog is shown instead.
     *
     * @param isFirstTime Indicates whether this is the first time the app is requesting permission.
     */
    private void requestNotificationPermission(boolean isFirstTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG,"Requesting POST_NOTIFICATIONS permission...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                System.out.println("Notification permission already granted.");
                //showWelcomeDialogIfNeeded(isFirstTime);
            }
        } else {
            showCustomPermissionDialog();
            Log.i(TAG,"Android version below 13, no permission needed.");
            showWelcomeDialogIfNeeded(isFirstTime);
        }
    }

    /**
     * Requests the READ_CONTACTS permission to access the user's contacts.
     * - If the permission is not granted, it prompts the user to allow access.
     * - If the permission is already granted, it logs the status.
     *
     * @param isFirstTime Indicates whether this is the first time the app is requesting permission.
     */
    private void requestContactsPermission(boolean isFirstTime) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting READ_CONTACTS permission...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST_CODE);
        } else {
            Log.i(TAG, "Contacts permission already granted.");
            //showWelcomeDialogIfNeeded(isFirstTime);
        }
    }

    /**
     * Handles the result of permission requests for notifications and contacts.
     * - If the POST_NOTIFICATIONS permission is granted, it proceeds to request contacts permission.
     * - If the READ_CONTACTS permission is granted, it logs the status.
     * - If either permission is denied, it logs the denial and can show a message to the user.
     * - After handling permissions, it checks if the welcome dialog needs to be displayed for first-time users.
     *
     * @param requestCode  The request code identifying the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The results for the requested permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "POST_NOTIFICATIONS permission granted.");
                requestContactsPermission(true);
            } else {
                Log.i(TAG, "POST_NOTIFICATIONS permission denied.");
            }
        } else if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "READ_CONTACTS permission granted.");
            } else {
                Log.i(TAG, "READ_CONTACTS permission denied.");
                //Snackbar.make(binding.getRoot(), "Contacts access denied", Snackbar.LENGTH_LONG).show();
            }
            boolean isFirstTime = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    .getBoolean("isFirstTime", true);
            showWelcomeDialogIfNeeded(isFirstTime);
        }
    }

    /**
     * Checks if the READ_CONTACTS permission is still granted.
     * - If the permission is granted, it logs the status.
     * - If the permission is revoked, it requests the permission again from the user.
     */
    private void checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_CONTACTS permission is still granted.");
        } else {
            Log.i(TAG, "READ_CONTACTS permission is revoked.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Shows a welcome dialog if it's the user's first time using the app.
     * - After displaying, it updates the SharedPreferences to prevent showing it again in the future.
     */
    private void showWelcomeDialogIfNeeded(boolean isFirstTime) {
        if (isFirstTime) {
            Log.i(TAG,"Showing welcome dialog.");
            Dialog d = new Dialog();
            d.showWelcomeDialog(this);

            // Update the flag so the dialog doesn't show next time
            SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
        }
    }

    /**
     * Displays a custom permission dialog for the user to allow or deny notification permissions.
     * - If allowed, proceeds with the process by showing the welcome dialog.
     * - If denied, shows a Snackbar indicating that notifications are disabled.
     */
    private void showCustomPermissionDialog() {
        // Inflate the custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_permission, null);

        // Build the AlertDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle button clicks
        Button btnAllow = dialogView.findViewById(R.id.btn_allow);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnAllow.setOnClickListener(v -> {
            // User accepts the permission
            dialog.dismiss();
            showWelcomeDialogIfNeeded(true); // Simulate proceeding
        });

        btnCancel.setOnClickListener(v -> {
            // User rejects the permission
            dialog.dismiss();
            Snackbar.make(binding.getRoot(), "Notifications disabled", Snackbar.LENGTH_LONG).show();
        });
    }

}
