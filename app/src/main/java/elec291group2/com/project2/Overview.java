package elec291group2.com.project2;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class Overview extends Fragment
{
    View view;
    SharedPreferences sharedPreferences;

    //Server stuff
    BufferedReader in;
    PrintWriter out;
    Handler handler;
    TextView systemText, doorText, motionText, laserText, alarmText, // security system
            livingText, kitchenText, washroomText, bedroomText, masterBedroomText; // lights
    private Socket socket;
    private String ipField;
    private String portField;
    // status: { systemStatus, doorStatus, motionStatus, laserStatus, alarmStatus
    //           livingRoomLights, kitchenLights, washroomLights, bedroomLights, masterBedroomLights }
    private String status;
    private Runnable getStatus = new Runnable()
    {
        @Override
        public void run()
        {
        /* do what you need to do */

            getStatus();
            // Call itself every 500 ms
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.overview, container, false);

        // get the IP and port for socket
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        ipField = sharedPreferences.getString("IP", "Not set");
        portField = sharedPreferences.getString("Port", "Not set");

        // security system status
        systemText = (TextView) view.findViewById(R.id.system_status);
        doorText = (TextView) view.findViewById(R.id.door_status);
        motionText = (TextView) view.findViewById(R.id.motion_status);
        laserText = (TextView) view.findViewById(R.id.laser_status);
        alarmText = (TextView) view.findViewById(R.id.alarm_status);
        // lights status
        livingText = (TextView) view.findViewById(R.id.livingroom_status);
        kitchenText = (TextView) view.findViewById(R.id.kitchen_status);
        washroomText = (TextView) view.findViewById(R.id.washroom_status);
        bedroomText = (TextView) view.findViewById(R.id.bedroom_status);
        masterBedroomText = (TextView) view.findViewById(R.id.mbedroom_status);

        new Thread(new ClientThread()).start();

        return view;
    }

    /**
     * Update security system status text on Overview with status string.
     */
    public void updateSecurity()
    {
        int systemStatus = Character.getNumericValue(status.charAt(0)),
                doorStatus = Character.getNumericValue(status.charAt(1)),
                motionStatus = Character.getNumericValue(status.charAt(2)),
                laserStatus = Character.getNumericValue(status.charAt(3)),
                alarmStatus = Character.getNumericValue(status.charAt(4));

        // systemStatus: 0 = unarmed (G), 1 = armed (B), 2 = triggered (R)
        systemText.setText(systemStatus == 0 ? "UNARMED" :
                systemStatus == 1 ? "ARMED" : "TRIGGERED");
        systemText.setTextColor(systemStatus == 0 ? Color.GREEN :
                systemStatus == 1 ? Color.BLUE : Color.RED);

        // doorStatus: 0 = closed (G), 1 = armed (B), 2 = open (M), 3 = triggered (R)
        doorText.setText(doorStatus == 0 ? "CLOSED" :
                doorStatus == 1 ? "ARMED" :
                        doorStatus == 2 ? "OPEN" : "TRIGGERED");
        doorText.setTextColor(doorStatus == 0 ? Color.GREEN :
                doorStatus == 1 ? Color.BLUE :
                        doorStatus == 2 ? Color.MAGENTA : Color.RED);

        // motionStatus: 0 = idle (G), 1 = armed (B), 2 = detected (M), 3 = triggered (R)
        motionText.setText(motionStatus == 0 ? "IDLE" :
                motionStatus == 1 ? "ARMED" :
                        motionStatus == 2 ? "DETECTED" : "TRIGGERED");
        motionText.setTextColor(motionStatus == 0 ? Color.GREEN :
                motionStatus == 1 ? Color.BLUE :
                        motionStatus == 2 ? Color.MAGENTA : Color.RED);

        // motionStatus: 0 = unarmed (G), 1 = armed (B), 2 = triggered (R)
        laserText.setText(laserStatus == 0 ? "UNARMED" :
                laserStatus == 1 ? "ARMED" : "TRIGGERED");
        laserText.setTextColor(laserStatus == 0 ? Color.GREEN :
                laserStatus == 1 ? Color.BLUE : Color.RED);

        // alarmStatus: 0 = off (R), 1 = on (G)
        alarmText.setText(alarmStatus == 0 ? "OFF" : "ON");
        alarmText.setTextColor(alarmStatus == 0 ? Color.RED : Color.GREEN);
    }

    /**
     * Update light status text on Overview with status string.
     */
    public void updateLights()
    {
        int livingRoomLights = Character.getNumericValue(status.charAt(5)),
                kitchenLights = Character.getNumericValue(status.charAt(6)),
                washroomLights = Character.getNumericValue(status.charAt(7)),
                bedroomLights = Character.getNumericValue(status.charAt(8)),
                masterBedroomLights = Character.getNumericValue(status.charAt(9));

        // 0 = on (R), 1 = off (G)
        livingText.setTextColor(livingRoomLights == 0 ? Color.RED : Color.GREEN);
        kitchenText.setTextColor(kitchenLights == 0 ? Color.RED : Color.GREEN);
        washroomText.setTextColor(washroomLights == 0 ? Color.RED : Color.GREEN);
        bedroomText.setTextColor(bedroomLights == 0 ? Color.RED : Color.GREEN);
        masterBedroomText.setTextColor(masterBedroomLights == 0 ? Color.RED : Color.GREEN);
    }

    @Override
    public void onPause()
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
        //Toast.makeText(this.getContext(), "Client has closed the connection.", Toast.LENGTH_SHORT).show();
        super.onPause();
    }

    private void sendCommand(String command)
    {
        try
        {
            out.println(command);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateStatusUI()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                updateSecurity();
                updateLights();
            }
        });
    }

    private void getStatus()
    {
        try
        {
            if (in.ready())  // Retrieve command from Android device, add to device queue
            {
                status = in.readLine();
                updateStatusUI();
                System.out.println("Recieved: " + status);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
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
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
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
            Looper.prepare();
            handler = new Handler();
            handler.postDelayed(getStatus, 1000);
            Looper.loop();
        }
    }
}
