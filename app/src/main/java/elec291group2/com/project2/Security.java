package elec291group2.com.project2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
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

    BufferedReader in;
    PrintWriter out;
    Handler handler;
    Button masterArmButton,
            masterDisarmButton,
            doorButton,
            motionButton,
            laserButton,
            alarmButton;
    boolean systemStatus = false,
            doorStatus = false,
            motionStatus = false,
            laserStatus = false,
            alarmStatus = false;
    TextView systemText, doorText, motionText, laserText, alarmText; // security system
    private Socket socket;
    private String ipField;
    private String portField;
    private String status = "1111111111";
    private String auth_key = "";
    private Runnable getStatus = new Runnable()
    {
        @Override
        public void run()
        {
      /* do what you need to do */

            getStatus();

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
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

        masterArmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("System ON");
            }
        });

        masterDisarmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("System OFF");
            }
        });

        doorButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Door " + (doorStatus ? "OFF" : "ON"));
            }
        });

        motionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Motion " + (motionStatus ? "OFF" : "ON"));
            }
        });

        laserButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Laser " + (laserStatus ? "OFF" : "ON"));
            }
        });

        alarmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Alarm " + (alarmStatus ? "OFF" : "ON"));
            }
        });

        new Thread(new ClientThread()).start();

        //updateAllButtons();
        //updateText();

        return view;
    }

    public void updateText()
    {
        int systemValue = Character.getNumericValue(status.charAt(0)),
                doorValue = Character.getNumericValue(status.charAt(1)),
                motionValue = Character.getNumericValue(status.charAt(2)),
                laserValue = Character.getNumericValue(status.charAt(3)),
                alarmValue = Character.getNumericValue(status.charAt(4));

        // systemValue: 0 = unarmed (G), 1 = armed (B), 2 = triggered (R)
        systemStatus = systemValue == 0 ? OFF : ON;
        systemText.setText(systemValue == 0 ? "UNARMED" :
                systemValue == 1 ? "ARMED" : "TRIGGERED");
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
        alarmStatus = laserValue == 0 ? OFF : ON;
        alarmText.setText(alarmValue == 0 ? "OFF" : "ON");
        alarmText.setTextColor(alarmValue == 0 ? Color.RED : Color.GREEN);
    }

    /*
    public void updateAllButtons()
    {
        // update Value of the lights
        systemStatus = Character.getNumericValue(status.charAt(0)) == 1 ? ON : OFF;
        doorStatus = Character.getNumericValue(status.charAt(1)) == 1 ? ON : OFF;
        motionStatus = Character.getNumericValue(status.charAt(2)) == 1 ? ON : OFF;
        laserStatus = Character.getNumericValue(status.charAt(3)) == 1 ? ON : OFF;
        alarmStatus = Character.getNumericValue(status.charAt(4)) == 1 ? ON : OFF;

        // update buttons with new statuses
        updateButton(doorButton, doorStatus);
        updateButton(motionButton, motionStatus);
        updateButton(laserButton, laserStatus);
        updateButton(alarmButton, alarmStatus);
    }

    public void updateButton(Button btn, boolean status)
    {
        btn.setText(status ? "ARMED" : "DISARMED");
        btn.getBackground().setColorFilter(status ? Color.GREEN : Color.RED, PorterDuff.Mode.MULTIPLY);
    }
    */

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
            // Toast.makeText(this.getContext(), "Client has closed the connection.", Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

    private void sendCommand(String command)
    {
        if(out != null)
        {
            try
            {
                out.println(command);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void updateStatusUI()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //updateAllButtons();
                updateText();
            }
        });
    }



    private void getStatus()
    {
        try
        {
            String status = in.readLine();
            if (status != null)  // Retrieve command from Android device, add to device queue
            {
                if (status.length() == 10)
                {
                    updateStatusUI();
                }
                handler.postDelayed(getStatus, 1000);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            handler.removeCallbacksAndMessages(getStatus);
        }

    }



    class ClientThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {

                socket = new Socket(ipField, Integer.parseInt(portField));

                if(socket != null) // TODO: Find a valid condition to check
                {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    sendCommand(auth_key);
                    String verification_status = in.readLine();
                    Log.v("System.out", verification_status);
                    if(verification_status.equals("Verified"))
                    {
                        showToast("Connected.");

                        Looper.prepare();
                        handler = new Handler();
                        handler.postDelayed(getStatus, 1000);
                        Looper.loop();
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