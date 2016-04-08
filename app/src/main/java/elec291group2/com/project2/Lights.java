package elec291group2.com.project2;

import android.app.Fragment;
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
import android.widget.SeekBar;
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
 * Created by Kevin on 2016-03-24.
 */
public class Lights extends Fragment
{
    final boolean ON = true, OFF = false;

    // instantiate the buttons and TextViews
    Button masterOnButton,
            masterOffButton,
            livingRoomButton,
            kitchenButton,
            washroomButton,
            bedroomButton,
            masterBedroomButton,
            livingRoomTimer,
            kitchenTimer,
            washroomTimer,
            bedroomTimer,
            masterBedroomTimer;
    TextView livingText,
            kitchenText,
            washroomText,
            bedroomText,
            masterBedroomText,
            timerValue;
    // create light status booleans
    boolean livingRoomStatus = false,
            kitchenStatus = false,
            washroomStatus = false,
            bedroomStatus = false,
            masterBedroomStatus = false;
    byte duration = 1;

    //Server variables
    BufferedReader in;
    PrintWriter out;
    Handler handler;
    SharedPreferences sharedPreferences;
    View view;
    private Socket socket;
    private String ipField;
    private String portField;
    private String status;
    private String auth_key;
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
        // get the IP and port for socket
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        ipField = sharedPreferences.getString("IP", "Not set");
        portField = sharedPreferences.getString("Port", "Not set");
        auth_key = sharedPreferences.getString("auth_key", "abc123");

        // inflate fragment view
        view = inflater.inflate(R.layout.lights, container, false);

        // light buttons
        masterOnButton = (Button) view.findViewById(R.id.master_on_button);
        masterOffButton = (Button) view.findViewById(R.id.master_off_button);
        livingRoomButton = (Button) view.findViewById(R.id.livingroom_button);
        kitchenButton = (Button) view.findViewById(R.id.kitchen_button);
        washroomButton = (Button) view.findViewById(R.id.washroom_button);
        bedroomButton = (Button) view.findViewById(R.id.bedroom_button);
        masterBedroomButton = (Button) view.findViewById(R.id.mbedroom_button);
        // timer buttons
        livingRoomTimer = (Button) view.findViewById(R.id.livingroom_timer);
        kitchenTimer = (Button) view.findViewById(R.id.kitchen_timer);
        washroomTimer = (Button) view.findViewById(R.id.washroom_timer);
        bedroomTimer = (Button) view.findViewById(R.id.bedroom_timer);
        masterBedroomTimer = (Button) view.findViewById(R.id.mbedroom_timer);
        // light text status
        livingText = (TextView) view.findViewById(R.id.livingroom_status);
        kitchenText = (TextView) view.findViewById(R.id.kitchen_status);
        washroomText = (TextView) view.findViewById(R.id.washroom_status);
        bedroomText = (TextView) view.findViewById(R.id.bedroom_status);
        masterBedroomText = (TextView) view.findViewById(R.id.mbedroom_status);
        // timer slider
        SeekBar timerSlider = (SeekBar) view.findViewById(R.id.timer_slider);
        timerValue = (TextView) view.findViewById(R.id.timer_text);

        /* set up button listeners on the Lights screen
           Master on/off will control all the lights features
           The toggle buttons will toggle on/off the lights
           The timer buttons will set how long the lights will stay on */
        masterOnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("51");
            }
        });

        masterOffButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("50");

            }
        });

        livingRoomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("2" + (livingRoomStatus ? "0" : "1"));
            }
        });

        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("3" + (kitchenStatus ? "0" : "1"));
            }
        });

        washroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("0" + (washroomStatus ? "0" : "1"));
            }
        });

        bedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("1" + (bedroomStatus ? "0" : "1"));
            }
        });

        masterBedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("4" + (masterBedroomStatus ? "0" : "1"));
            }
        });

        livingRoomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("8" + (char) duration);
            }
        });

        kitchenTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("9" + (char) duration);
            }
        });

        washroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("6" + (char) duration);
            }
        });

        bedroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("7" + (char) duration);
            }
        });

        masterBedroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand(":" + (char) duration);
            }
        });

        // Get value from the timer slider that will be used to set duration of lights
        timerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser)
            {
                // change text above the timer slider
                timerValue.setText("Timer: " + String.valueOf((progress * 59 / 100)+1) + " seconds");
                // get the duration (seconds) as a byte
                duration = (byte) (((progress * 59 / 100)+1) & 0xff ) ;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
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
     * Update light status text on screen with status string.
     */
    public void updateText()
    {
        // parse the status string into the system's statuses
        int livingRoomLights = Character.getNumericValue(status.charAt(7)),
                kitchenLights = Character.getNumericValue(status.charAt(8)),
                washroomLights = Character.getNumericValue(status.charAt(5)),
                bedroomLights = Character.getNumericValue(status.charAt(6)),
                masterBedroomLights = Character.getNumericValue(status.charAt(9));

        // set the text and colours on the screen based on the status bits
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

