package com.example.whatsalert.Util;
import com.example.whatsalert.BuildConfig;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.whatsalert.R;

public class Dialog {

    /**
     * Displays a custom welcome dialog with the app version in the footer and a "Got it!" button to dismiss the dialog.
     * The dialog cannot be dismissed by touching outside, and its width is programmatically set to 85% of the screen width.
     *
     * @param context The context in which the dialog is displayed, typically an Activity or Application context.
     */
    public void showWelcomeDialog(Context context) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_welcome, null);

        // Set the app version in the footer
        TextView versionTextView = customView.findViewById(R.id.dialog_footer);
        String versionName = "Version " + BuildConfig.VERSION_NAME; // Ensure your app's `BuildConfig` has a `VERSION_NAME` field
        versionTextView.setText(versionName);

        // Set up the "Got it!" button
        Button gotItButton = customView.findViewById(R.id.dialog_button);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        gotItButton.setOnClickListener(v -> dialog.dismiss());

        // Disable dismiss on outside touch
        dialog.setCancelable(false);

        dialog.show();

        // Decrease the width of the dialog programmatically
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        dialog.getWindow().setAttributes(params);
    }


}
