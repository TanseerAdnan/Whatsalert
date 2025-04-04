package com.example.whatsalert.Util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * A service that listens for tasks being removed and automatically restarts itself using a broadcast.
 * This service ensures that it remains running by sending a broadcast intent to a custom `Restarter` class
 * whenever it is destroyed or removed from the task list.
 *
 * Key Functions:
 * 1. onCreate: Called when the service is created.
 * 2. onStartCommand: Starts the service and ensures it is restarted if the system kills it.
 * 3. onTaskRemoved: Sent when the service's task is removed. It triggers a broadcast to restart the service.
 * 4. onDestroy: Called when the service is destroyed. Optionally, it broadcasts to restart the service.
 * 5. onBind: Not used in this case, as this is a started service (no binding).
 */
public class RestartBroadcastService extends Service {

    private static final String TAG = "RestartBroadcastService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    /**
     * Called when the service is started via `startService()`.
     * The service is set to restart automatically when killed by the system using START_STICKY.
     * @param intent The Intent that started the service.
     * @param flags Additional flags about how the service should be started.
     * @param startId A unique integer identifying this specific request to start the service.
     * @return Returns the start mode for the service, which in this case is `START_STICKY`.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    /**
     * Called when the service's task is removed. This method is used to restart the service
     * by sending a broadcast to a custom receiver.
     * @param rootIntent The Intent that was used to start the service.
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Restart the broadcast receiver
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        sendBroadcast(broadcastIntent);
        Log.d(TAG, "Broadcast sent to restart receiver");
    }

    /**
     * Called when the service is destroyed. This can happen if the system stops the service or
     * the service is explicitly destroyed. It broadcasts to restart the service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Optionally re-register the broadcast here too
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        sendBroadcast(broadcastIntent);
        Log.d(TAG, "Service destroyed and broadcast sent");
    }

    /**
     * Called when a client (e.g., activity or another service) binds to the service.
     * Since this service is not designed for binding, it returns `null`.
     * @param intent The Intent that was used to bind to the service.
     * @return Returns null as this service does not support binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}