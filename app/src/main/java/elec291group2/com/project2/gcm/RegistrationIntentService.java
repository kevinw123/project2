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
import android.preference.PreferenceManager;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        try
        {
            // Get device token from GCM server
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(constants.GCM_SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            registrationResult = sendRegistrationToServer(token);

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
     */
    private boolean sendRegistrationToServer(final String token){
        class RegistrationClientThread implements Callable<Boolean>
        {
            Socket socket = new Socket();
            PrintWriter out;
            BufferedReader in;

            @Override
            public Boolean call()
            {
                Boolean registrationStatus = false;
                try
                {
                    String command = "register " + token;
                    String ipField = sharedPreferences.getString("IP", "Not set");
                    String portField = sharedPreferences.getString("Port", "Not setsadjaiosjods");
                    String auth_key = sharedPreferences.getString("auth_key", "1234");
                    socket.connect(new InetSocketAddress(ipField, Integer.parseInt(portField)), 250);

                    if (socket != null)
                    {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        out.println(auth_key);
                        String verification_status = in.readLine();
                        if (verification_status.equals("Verified")) {
                            out.println(command);
                            registrationStatus = true;
                        } else {
                            showToast(constants.MESSAGE_APP_SERVER_ERROR + " (Authentication key is incorrect)");
                        }
                    } else {
                        showToast(constants.MESSAGE_APP_SERVER_ERROR + " (Server information is incorrect)");
                    }
                }
                catch (IOException | NumberFormatException e)
                {
                    e.printStackTrace();
                    showToast(constants.MESSAGE_APP_SERVER_ERROR + " (Server information is incorrect)");
                }
                finally
                {
                    try{
                        if (out != null) { out.close(); }
                        if (in != null) { in.close(); }
                        if (socket != null) { socket.close(); }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Log.d("System.out", "Error closing in/out streams or socket");
                    }
                }

                return registrationStatus;
            }
        }

        Callable<Boolean> callable = new RegistrationClientThread();
        Future<Boolean> f = Executors.newSingleThreadExecutor().submit(callable);

        try{
            return f.get();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
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