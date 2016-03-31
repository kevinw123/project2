package elec291group2.com.project2.gcm;

/**
 * Created by Derek on 3/25/2016.
 *
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    SharedPreferences sharedPreferences;
    Boolean registrationResult;

    public RegistrationIntentService() {
        super(TAG);
        registrationResult = true;
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

            try
            {
                sendRegistrationToServer(token);
            }
            catch (IOException | NumberFormatException e)
            {
                e.printStackTrace();
                registrationResult = false;
                showToast(constants.MESSAGE_APP_SERVER_ERROR);
            }

            // If registration completed successful
            if (registrationResult)
                showToast(constants.MESSAGE_REGISTRATION_SUCCESS + token);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            registrationResult = false;
            showToast(constants.MESSAGE_GCM_SERVER_ERROR);
        }

        // Update shared preferences
        sharedPreferences.edit().putBoolean("Notifications", registrationResult).apply();

        // Broadcast that registration is completed.
        Intent localIntent = new Intent(constants.BROADCAST_REGISTRATION_COMPLETE);
        localIntent.putExtra("registrationResult", registrationResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Persist registration to app/home server.
     *
     * @param token The device token.
     * @throws IOException If there was an error connecting/writing to app server
     * @throws UnknownHostException If there was an error in the ip address of the app server
     * @throws NumberFormatException If the port was not set correctly in settings
     */
    private void sendRegistrationToServer(String token) throws IOException, NumberFormatException{
        String command = "register" + token;
        String ipField = sharedPreferences.getString("IP", "NOT ENTERED");
        String portField = sharedPreferences.getString("Port", "NOT ENTERED");
        String auth_key = sharedPreferences.getString("auth_key", "1234");
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        socket = new Socket();
        socket.setSoTimeout(200);
        socket.connect(new InetSocketAddress(ipField, Integer.parseInt(portField)), 200);

        if(socket != null) // TODO: Find a valid condition to check
        {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(auth_key);
            String verification_status = in.readLine();
            Log.v("System.out", verification_status);
            if(verification_status.equals("Verified"))
            {
                showToast("Connected.");
                out.println(command);
                out.close();
                socket.close();
            }
            else
            {
                showToast("Authentication key is incorrect");
            }
        }
        else
        {
            showToast("Server information is incorrect.");
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