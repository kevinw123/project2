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

    boolean livingRoomStatus = false,
            kitchenStatus = false,
            washroomStatus = false,
            bedroomStatus = false,
            masterBedroomStatus = false;
    int duration = 0;

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

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        ipField = sharedPreferences.getString("IP", "Not set");
        portField = sharedPreferences.getString("Port", "Not set");
        auth_key = sharedPreferences.getString("auth_key", "abc123");

        view = inflater.inflate(R.layout.lights, container, false);

        masterOnButton = (Button) view.findViewById(R.id.master_on_button);
        masterOffButton = (Button) view.findViewById(R.id.master_off_button);
        livingRoomButton = (Button) view.findViewById(R.id.livingroom_button);
        kitchenButton = (Button) view.findViewById(R.id.kitchen_button);
        washroomButton = (Button) view.findViewById(R.id.washroom_button);
        bedroomButton = (Button) view.findViewById(R.id.bedroom_button);
        masterBedroomButton = (Button) view.findViewById(R.id.mbedroom_button);

        livingRoomTimer = (Button) view.findViewById(R.id.livingroom_timer);
        kitchenTimer = (Button) view.findViewById(R.id.kitchen_timer);
        washroomTimer = (Button) view.findViewById(R.id.washroom_timer);
        bedroomTimer = (Button) view.findViewById(R.id.bedroom_timer);
        masterBedroomTimer = (Button) view.findViewById(R.id.mbedroom_timer);

        livingText = (TextView) view.findViewById(R.id.livingroom_status);
        kitchenText = (TextView) view.findViewById(R.id.kitchen_status);
        washroomText = (TextView) view.findViewById(R.id.washroom_status);
        bedroomText = (TextView) view.findViewById(R.id.bedroom_status);
        masterBedroomText = (TextView) view.findViewById(R.id.mbedroom_status);

        SeekBar timerSlider = (SeekBar) view.findViewById(R.id.timer_slider);
        timerValue = (TextView) view.findViewById(R.id.timer_text);

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
                sendCommand("0" + (livingRoomStatus ? "0" : "1"));

            }
        });

        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("1" + (kitchenStatus ? "0" : "1"));
            }
        });

        washroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("2" + (washroomStatus ? "0" : "1"));
            }
        });

        bedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("3" + (bedroomStatus ? "0" : "1"));
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

        livingRoomTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("LivingRoomTimed " + duration);
            }
        });

        kitchenTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("KitchenTimed " + duration);
            }
        });
        
        washroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("WashroomTimed " + duration);
            }
        });
        
        return view;
    }

        bedroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("BedroomTimed " + duration);
            }
        });

        masterBedroomTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("MasterBedroomTimed " + duration);
            }
        });


        timerSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser)
            {
                timerValue.setText("Timer: " + String.valueOf(progress * 60 / 100) + " seconds");
                duration = progress * 60 / 100;
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


        new Thread(new ClientThread()).start();

        updateText();

        return view;
    }

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
        if (socket != null)

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
        if (out != null)
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
            String status = in.readLine();
            if (status != null)  // Retrieve command from Android device, add to device queue
            {
                Log.v("System.out", status);
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

    private void showToast(String message)
    {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    class ClientThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                socket = new Socket(ipField, Integer.parseInt(portField));

                if (socket != null) // TODO: Find a valid condition to check
                {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    sendCommand(auth_key);
                    String verification_status = in.readLine();
                    Log.v("System.out", verification_status);
                    if (verification_status.equals("Verified"))
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
            } catch (UnknownHostException e1)
            {
                e1.printStackTrace();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            } catch (NumberFormatException e1)
            {
                e1.printStackTrace();
            }

        }

    }
}

