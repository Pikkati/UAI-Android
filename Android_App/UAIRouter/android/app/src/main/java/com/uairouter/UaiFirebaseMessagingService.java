package com.uairouter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class UaiFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");

            showNotification(title != null ? title : "UAI Alert", body != null ? body : "New update available", type);
        }

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title != null ? title : "UAI Alert", body != null ? body : "New update available", "general");
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Send token to server for targeted notifications
        sendTokenToServer(token);
    }

    private void showNotification(String title, String message, String type) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "uai_notifications",
                    "UAI Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications from UAI Copilot services");
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent to open the app
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "uai_notifications")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use default icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Add type-specific styling
        if ("alert".equals(type)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else if ("update".equals(type)) {
            builder.setSmallIcon(android.R.drawable.ic_menu_upload);
        }

        // Show notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void sendTokenToServer(String token) {
        // TODO: Send token to Docker backend for registration
        // This would typically be done via HTTP POST to your API
        // For now, just log it
        android.util.Log.d("FCM", "New token: " + token);
    }
}
