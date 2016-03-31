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
import android.widget.Button;
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
 * Created by Kevin on 2016-03-24.
 */
public class Lights extends Fragment
{
    final boolean ON = true, OFF = false;

    //Server stuff
    BufferedReader in;
    PrintWriter out;
    Handler handler;
    SharedPreferences sharedPreferences;
    View view;
    Button masterOnButton,
            masterOffButton,
            livingRoomButton,
            kitchenButton,
            washroomButton,
            bedroomButton,
            masterBedroomButton;
    TextView livingText,
            kitchenText,
            washroomText,
            bedroomText,
            masterBedroomText;
    boolean livingRoomStatus = false,
            kitchenStatus = false,
            washroomStatus = false,
            bedroomStatus = false,
            masterBedroomStatus = false;
    private Socket socket;
    private String ipField;
    private String portField;
    private String status = "1111111111"; //temp status placeholder
    private String auth_key;
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        ipField = sharedPreferences.getString("IP", "Not set");
        portField = sharedPreferences.getString("Port", "Not set");
        auth_key = sharedPreferences.getString("auth_key","abc123");

        view = inflater.inflate(R.layout.lights, container, false);

        masterOnButton = (Button) view.findViewById(R.id.master_on_button);
        masterOffButton = (Button) view.findViewById(R.id.master_off_button);
        livingRoomButton = (Button) view.findViewById(R.id.livingroom_button);
        kitchenButton = (Button) view.findViewById(R.id.kitchen_button);
        washroomButton = (Button) view.findViewById(R.id.washroom_button);
        bedroomButton = (Button) view.findViewById(R.id.bedroom_button);
        masterBedroomButton = (Button) view.findViewById(R.id.mbedroom_button);

        livingText = (TextView) view.findViewById(R.id.livingroom_status);
        kitchenText = (TextView) view.findViewById(R.id.kitchen_status);
        washroomText = (TextView) view.findViewById(R.id.washroom_status);
        bedroomText = (TextView) view.findViewById(R.id.bedroom_status);
        masterBedroomText = (TextView) view.findViewById(R.id.mbedroom_status);

        //updateAllButtons();

        masterOnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("MasterLights ON");
            }
        });

        masterOffButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("MasterLights OFF");

            }
        });

        livingRoomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Living " + (livingRoomStatus ? "OFF" : "ON"));

            }
        });

        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Kitchen " + (kitchenStatus ? "OFF" : "ON"));
            }
        });

        washroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Washroom " + (washroomStatus ? "OFF" : "ON"));
            }
        });

        bedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("Bedroom " + (bedroomStatus ? "OFF" : "ON"));
            }
        });

        masterBedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("MasterBedroom " + (masterBedroomStatus ? "OFF" : "ON"));
            }
        });

        new Thread(new ClientThread()).start();

        updateText();

        return view;
    }

    /*
    public void updateAllButtons()
    {
        // update status of the lights
        livingRoomStatus = Character.getNumericValue(status.charAt(5)) == 1 ? ON : OFF;
        kitchenStatus = Character.getNumericValue(status.charAt(6)) == 1 ? ON : OFF;
        washroomStatus = Character.getNumericValue(status.charAt(7)) == 1 ? ON : OFF;
        bedroomStatus = Character.getNumericValue(status.charAt(8)) == 1 ? ON : OFF;
        masterBedroomStatus = Character.getNumericValue(status.charAt(9)) == 1 ? ON : OFF;

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

    public void masterControl(boolean status)
    {
        livingRoomStatus = kitchenStatus = washroomStatus = bedroomStatus = masterBedroomStatus = status;
        updateAllButtons();
    }

    */

    public void updateText()
    {
        int livingRoomLights = Character.getNumericValue(status.charAt(5)),
                kitchenLights = Character.getNumericValue(status.charAt(6)),
                washroomLights = Character.getNumericValue(status.charAt(7)),
                bedroomLights = Character.getNumericValue(status.charAt(8)),
                masterBedroomLights = Character.getNumericValue(status.charAt(9));

        // 0 = on (R), 1 = off (G)
        livingRoomStatus = livingRoomLights == 0 ? OFF : ON;
        livingText.setText(livingRoomLights == 0 ? "OFF" : "ON");
        livingText.setTextColor(livingRoomLights == 0 ? Color.RED : Color.GREEN);

        kitchenStatus = kitchenLights == 0 ? OFF : ON;
        kitchenText.setText(kitchenLights == 0 ? "OFF" : "ON");
        kitchenText.setTextColor(kitchenLights == 0 ? Color.RED : Color.GREEN);

        washroomStatus = washroomLights == 0 ? OFF : ON;
        washroomText.setText(washroomLights == 0 ? "OFF" : "ON");
        washroomText.setTextColor(washroomLights == 0 ? Color.RED : Color.GREEN);

        bedroomStatus = bedroomLights == 0 ? OFF : ON;
        bedroomText.setText(bedroomLights == 0 ? "OFF" : "ON");
        bedroomText.setTextColor(bedroomLights == 0 ? Color.RED : Color.GREEN);

        masterBedroomStatus = masterBedroomLights == 0 ? OFF : ON;
        masterBedroomText.setText(masterBedroomLights == 0 ? "OFF" : "ON");
        masterBedroomText.setTextColor(masterBedroomLights == 0 ? Color.RED : Color.GREEN);
    }

    @Override
    public void onPause()
    {
        if(socket != null)

        {
            sendCommand("exit");
            try
            {
                handler.removeCallbacksAndMessages(getStatus);
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
                updateText();
                //updateAllButtons();
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

                if(socket.isBound()) // TODO: Find a valid condition to check
                {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    sendCommand(auth_key);
                    if(in.readLine().equals("Verified"))
                    {
                        System.out.print("Server verified the authentication key.");
                        Looper.prepare();
                        handler = new Handler();
                        handler.postDelayed(getStatus, 1000);
                        Looper.loop();
                    }
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
}
