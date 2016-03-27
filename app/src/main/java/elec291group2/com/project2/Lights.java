package elec291group2.com.project2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Kevin on 2016-03-24.
 */
public class Lights extends Fragment
{
    final boolean ON = true, OFF = false;
    //Server stuff
    BufferedReader in;
    PrintWriter out;
    Handler handler;
    private Socket socket;
    private String ipField;
    private String portField;

    private String status = "111111111"; //temp status placeholder

    SharedPreferences sharedPreferences;
    View view;

    Button  masterOnButton,
            masterOffButton,
            livingRoomButton,
            kitchenButton,
            washroomButton,
            bedroomButton,
            masterBedroomButton;
    boolean livingRoomStatus = false,
            kitchenStatus = false,
            washroomStatus = false,
            bedroomStatus = false,
            masterBedroomStatus = false;

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
        sharedPreferences = this.getActivity().getSharedPreferences("serverData", Context.MODE_PRIVATE);
        ipField = sharedPreferences.getString("IP", "NOT ENTERED");
        portField = sharedPreferences.getString("Port", "NOT ENTERED");
        view = inflater.inflate(R.layout.lights, container, false);

        masterOnButton = (Button) view.findViewById(R.id.master_on_button);
        masterOffButton = (Button) view.findViewById(R.id.master_off_button);
        livingRoomButton = (Button) view.findViewById(R.id.livingroom_button);
        kitchenButton = (Button) view.findViewById(R.id.kitchen_button);
        washroomButton = (Button) view.findViewById(R.id.washroom_button);
        bedroomButton = (Button) view.findViewById(R.id.bedroom_button);
        masterBedroomButton = (Button) view.findViewById(R.id.mbedroom_button);

        //updateAllButtons();

        masterOnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Master ON");

                masterControl(ON); //to be removed once receive is functional
            }
        });

        masterOffButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Master OFF");

                masterControl(OFF); //to be removed once receive is functional

            }
        });

        livingRoomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Living " + (livingRoomStatus ? "ON" : "OFF"));

                updateButton(livingRoomButton, livingRoomStatus); //to be removed once receive is functional

            }
        });

        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Kitchen " + (kitchenStatus ? "ON" : "OFF"));

                updateButton(kitchenButton, kitchenStatus); //to be removed once receive is functional
            }
        });

        washroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Washroom " + (washroomStatus ? "ON" : "OFF"));

                updateButton(washroomButton, washroomStatus); //to be removed once receive is functional
            }
        });

        bedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Bedroom " + (bedroomStatus ? "ON" : "OFF"));

                updateButton(bedroomButton, bedroomStatus); //to be removed once receive is functional
            }
        });

        masterBedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("MasterBedroom " + (masterBedroomStatus ? "ON" : "OFF"));

                updateButton(masterBedroomButton, masterBedroomStatus); //to be removed once receive is functional
            }
        });

        new Thread(new ClientThread()).start();

        return view;
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
        // Toast.makeText(this.getContext(), "Client has closed the connection.", Toast.LENGTH_SHORT).show();
        super.onPause();
    }

    public void updateAllButtons()
    {
        // update status of the lights
        livingRoomStatus = Character.getNumericValue(status.charAt(4)) == 1 ? ON : OFF;
        kitchenStatus = Character.getNumericValue(status.charAt(5)) == 1 ? ON : OFF;
        washroomStatus = Character.getNumericValue(status.charAt(6)) == 1 ? ON : OFF;
        bedroomStatus = Character.getNumericValue(status.charAt(7)) == 1 ? ON : OFF;
        masterBedroomStatus = Character.getNumericValue(status.charAt(8)) == 1 ? ON : OFF;

        // update buttons with new statuses
        updateButton(livingRoomButton, livingRoomStatus);
        updateButton(kitchenButton, kitchenStatus);
        updateButton(washroomButton, washroomStatus);
        updateButton(bedroomButton, bedroomStatus);
        updateButton(masterBedroomButton, masterBedroomStatus);
    }

    public void updateButton(Button btn, boolean status)
    {
        btn.setText(status ? "ON" : "OFF");
        btn.getBackground().setColorFilter(status ? Color.GREEN : Color.RED, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * TO BE REMOVED
     */
    public void masterControl(boolean status)
    {
        livingRoomStatus = kitchenStatus = washroomStatus = bedroomStatus = masterBedroomStatus = status;
        updateAllButtons();
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
                updateAllButtons();
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
            //textStatus.setText(String.valueOf(i));
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

            } catch (UnknownHostException e1)
            {
                e1.printStackTrace();
            } catch (IOException e1)
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
