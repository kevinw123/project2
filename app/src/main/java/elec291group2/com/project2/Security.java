package elec291group2.com.project2;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Created by Kevin on 2016-03-23.
 */
public class Security extends Fragment
{
    final boolean ON = true, OFF = false;

    SharedPreferences sharedPreferences;
    View view;

    // instantiate the buttons and TextViews
    Button masterArmButton,
            masterDisarmButton,
            doorButton,
            motionButton,
            laserButton,
            alarmButton;
    TextView systemText,
            doorText,
            motionText,
            laserText,
            alarmText;
    // create system status booleans
    boolean systemStatus = false,
            doorStatus = false,
            motionStatus = false,
            laserStatus = false,
            alarmStatus = false;

    // instantiate socket variables
    private Socket socket;
    private String ipField;
    private String portField;
    private String status;
    private String auth_key = "";
    BufferedReader in;
    PrintWriter out;
    Handler handler;
    private Runnable getStatus = new Runnable()
    {
        @Override
        public void run()
        {
            getStatus();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // inflate fragment view
        view = inflater.inflate(R.layout.security, container, false);

        // get the IP and port for socket
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        ipField = sharedPreferences.getString("IP", "Not set");
        portField = sharedPreferences.getString("Port", "Not set");
        auth_key = sharedPreferences.getString("auth_key", "1234");

        // system arming buttons
        masterArmButton = (Button) view.findViewById(R.id.master_arm_button);
        masterDisarmButton = (Button) view.findViewById(R.id.master_disarm_button);
        doorButton = (Button) view.findViewById(R.id.door_button);
        motionButton = (Button) view.findViewById(R.id.motion_button);
        laserButton = (Button) view.findViewById(R.id.laser_button);
        alarmButton = (Button) view.findViewById(R.id.alarm_button);
        // security system status
        systemText = (TextView) view.findViewById(R.id.system_status);
        doorText = (TextView) view.findViewById(R.id.door_status);
        motionText = (TextView) view.findViewById(R.id.motion_status);
        laserText = (TextView) view.findViewById(R.id.laser_status);
        alarmText = (TextView) view.findViewById(R.id.alarm_status);

        /* set up button listeners on the Security screen
           Master arm/disarm will control all the security features
           The other buttons will toggle the status of the system */
        masterArmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("2");
            }
        });

        masterDisarmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("3");
            }
        });

        doorButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand(doorStatus ? "7" : "4");
            }
        });

        motionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand(motionStatus ? "8" : "5");
            }
        });

        laserButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand(laserStatus ? "9" : "6");
            }
        });

        alarmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand(alarmStatus ? "0" : "1");
            }
        });

        return view;
    }

    // when fragment is resumed, create the socket thread
    @Override
    public void onResume()
    {
        new Thread(new ClientThread()).start();
        super.onResume();
    }

    // when fragment is exited, close connection with socket
    @Override
    public void onPause()
    {
        if(socket != null)
        {
            sendCommand("exit");
            try
            {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    /**
     * Update security status text on screen with status string.
     */
    public void updateText()
    {
        // parse the status string into the system's statuses
        int systemValue = Character.getNumericValue(status.charAt(0)),
                doorValue = Character.getNumericValue(status.charAt(1)),
                motionValue = Character.getNumericValue(status.charAt(2)),
                laserValue = Character.getNumericValue(status.charAt(3)),
                alarmValue = Character.getNumericValue(status.charAt(4));

        // set the text and colours on the screen based on the status bits
        // systemStatus: 0 = unarmed (G), 1 = armed (B), 2 = triggered (R), 3 = password trigger (R)
        systemStatus = systemValue == 0 ? OFF : ON;
        systemText.setText(systemValue == 0 ? "UNARMED" :
                systemValue == 1 ? "ARMED" :
                        systemValue == 2 ? "TRIGGERED" : "FAILED ENTRY");
        systemText.setTextColor(systemValue == 0 ? Color.GREEN :
                systemValue == 1 ? Color.BLUE : Color.RED);

        // doorValue: 0 = closed (G), 1 = armed (B), 2 = open (M), 3 = triggered (R)
        doorStatus = doorValue == 0 || doorValue == 2 ? OFF : ON;
        doorText.setText(doorValue == 0 ? "CLOSED" :
                doorValue == 1 ? "ARMED" :
                        doorValue == 2 ? "OPEN" : "TRIGGERED");
        doorText.setTextColor(doorValue == 0 ? Color.GREEN :
                doorValue == 1 ? Color.BLUE :
                        doorValue == 2 ? Color.MAGENTA : Color.RED);

        // motionValue: 0 = idle (G), 1 = armed (B), 2 = detected (M), 3 = triggered (R)
        motionStatus = motionValue == 0 || motionValue == 2 ? OFF : ON;
        motionText.setText(motionValue == 0 ? "IDLE" :
                motionValue == 1 ? "ARMED" :
                        motionValue == 2 ? "DETECTED" : "TRIGGERED");
        motionText.setTextColor(motionValue == 0 ? Color.GREEN :
                motionValue == 1 ? Color.BLUE :
                        motionValue == 2 ? Color.MAGENTA : Color.RED);

        // laserValue: 0 = unarmed (G), 1 = armed (B), 2 = triggered (R)
        laserStatus = laserValue == 0 ? OFF : ON;
        laserText.setText(laserValue == 0 ? "UNARMED" :
                laserValue == 1 ? "ARMED" : "TRIGGERED");
        laserText.setTextColor(laserValue == 0 ? Color.GREEN :
                laserValue == 1 ? Color.BLUE : Color.RED);

        // alarmValue: 0 = off (R), 1 = on (G)
        alarmStatus = alarmValue == 0 ? OFF : ON;
        alarmText.setText(alarmValue == 0 ? "OFF" : "ON");
        alarmText.setTextColor(alarmValue == 0 ? Color.RED : Color.GREEN);

        Menu menu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        menu.findItem(R.id.status).setTitle("System Status: " +
                (systemValue == 0 ? "UNARMED" :
                        systemValue == 1 ? "ARMED" : "TRIGGERED"));
    }

    // send command to be received by the RPi
    private void sendCommand(String command)
    {
        if(out != null)
        {
            try
            {
                // send a command through the writer stream
                out.println(command);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // update the UI of the fragment
    public void updateStatusUI()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                updateText();
            }
        });
    }

    // retrieve a status from the input stream
    private void getStatus()
    {
        try
        {
            String temp_status = in.readLine();
            if (temp_status != null)  // Retrieve command from Android device, add to device queue
            {
                // check if the status string is of right length
                if (temp_status.length() == 10)
                {
                    // update the status string and UI elements
                    status = temp_status;
                    updateStatusUI();
                }
                // pause for 1 second
                handler.postDelayed(getStatus, 1000);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            handler.removeCallbacksAndMessages(getStatus);
        }
    }

    // Thread class to communicate between the Android app and RPi
    class ClientThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                // create a socket using the stored IP and Port
                socket = new Socket(ipField, Integer.parseInt(portField));

                // if socket is properly created, set up the stream communication streams
                if(socket != null)
                {
                    // create the bufferedReader stream and printWriter stream
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    // check the authentication key with the server's key
                    Log.v("System.out", auth_key);
                    sendCommand(auth_key);
                    String verification_status = in.readLine();
                    Log.v("System.out", verification_status);
                    // establish connection if they match
                    if(verification_status.equals("Verified"))
                    {
                        showToast("Connected.");

                        Looper.prepare();
                        handler = new Handler();
                        handler.postDelayed(getStatus, 1000);
                        Looper.loop();
                    }
                    // else, alert user
                    else
                    {
                        showToast("Authentication key is incorrect");
                    }
                }
                // if socket is null, alert user
                else
                {
                    showToast("Server information is incorrect.");
                }
            }
            catch (UnknownHostException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            catch (NumberFormatException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    // helper function to show toasts
    private void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}