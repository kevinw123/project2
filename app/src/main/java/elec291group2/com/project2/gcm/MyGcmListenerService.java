package elec291group2.com.project2.gcm;

/**
 * Base service class for communicating with Google Cloud Messaging.
 * Receives and displays notifications when requested by app server.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GcmListenerService;

import elec291group2.com.project2.MainMenu;
import elec291group2.com.project2.R;

public class MyGcmListenerService extends GcmListenerService {

    SharedPreferences sharedPreferences;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        // Get string from 'message' field
        String message = data.getString("message");

        // Send notification only if enabled in shared preferences.
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sharedPreferences.getBoolean("Notifications", false))
        {
            sendNotification(message);
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_home_24dp)
                .setContentTitle("Home")
                .setContentText(message)
                .setLights(0xFFFF2506, 150, 150)
                .setColor(0xFFFF2506)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}