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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
            String token = instanceID.getToken(constants.GCM_SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

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
        String command = "register" + token;
        String ipField = sharedPreferences.getString("IP", "NOT ENTERED");
        String portField = sharedPreferences.getString("Port", "NOT ENTERED");
        Socket socket;
        PrintWriter out;
        try {
            socket = new Socket(ipField, Integer.parseInt(portField));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(command);
            out.close();
            socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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