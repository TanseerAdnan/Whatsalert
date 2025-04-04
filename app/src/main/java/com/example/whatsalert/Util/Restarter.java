package com.example.whatsalert.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A BroadcastReceiver that listens for a broadcast to restart the `RestartBroadcastService`.
 * When the broadcast is received, it starts the `RestartBroadcastService` to ensure that
 * the service is running even after being stopped or killed by the system.
 *
 * Key Functions:
 * 1. onReceive: This method is triggered when the broadcast is received. It starts the service again.
 */
public class Restarter extends BroadcastReceiver {

    private static final String TAG = "Restarter";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast received to restart the service");

        // Restart the service
        Intent serviceIntent = new Intent(context, RestartBroadcastService.class);  // Use your actual service here
        context.startService(serviceIntent);
    }
}
