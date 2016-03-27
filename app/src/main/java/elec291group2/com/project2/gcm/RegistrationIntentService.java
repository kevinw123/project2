package elec291group2.com.project2.gcm;

/**
 * Created by Derek on 3/25/2016.
 *
 */

import elec291group2.com.project2.R;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import elec291group2.com.project2.gcm.constants;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    SharedPreferences sharedPreferences;
    Boolean registrationStatus;

    public RegistrationIntentService() {
        super(TAG);
        registrationStatus = false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = getApplicationContext().getSharedPreferences(
                             "serverData", Context.MODE_PRIVATE);

        try
        {
            // Get device token from GCM server
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            registrationStatus = true;
            showToast("Successfully registered to push notification server: " + token);
        }
        catch (Exception e)
        {
            registrationStatus = false;
            showToast("Failed to register to push notification server");
        }

        // Update shared preferences
        sharedPreferences.edit().putBoolean("Notifications", registrationStatus).apply();

        // Broadcast that registration is completed.
        Intent localIntent = new Intent(constants.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    /**
     * Persist registration to app server.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Send with sendCommand()
    }

    /**
     *  Used to display toast within intent service.
     */
    private void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}