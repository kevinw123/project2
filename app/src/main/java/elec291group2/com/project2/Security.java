package elec291group2.com.project2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
 * Created by Kevin on 2016-03-23.
 */
public class Security extends Fragment
{
    SharedPreferences sharedPreferences;
    View view;
    TextView textStatus;

    BufferedReader in;
    PrintWriter out;
    Handler handler;
    private Socket socket;
    private String ipField;
    private String portField;
    private String status;
    private Runnable getStatus = new Runnable()
    {
        @Override
        public void run()
        {
      /* do what you need to do */

            getStatus();
            // Call itself every 500 ms
            handler.postDelayed(this, 500);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.security, container, false);

        // get the IP and port for socket
        sharedPreferences = this.getActivity().getSharedPreferences("serverData", Context.MODE_PRIVATE);
        ipField = sharedPreferences.getString("IP", "NOT ENTERED");
        portField = sharedPreferences.getString("Port", "NOT ENTERED");

        Button kevin = (Button) view.findViewById(R.id.kevin);
        textStatus = (TextView) view.findViewById(R.id.status);

        new Thread(new ClientThread()).start();

        kevin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("kevin");
            }
        });

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
                textStatus.setText(status);
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


    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                socket = new Socket(ipField, Integer.parseInt(portField));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Looper.prepare();
            handler = new Handler();
            handler.postDelayed(getStatus, 1000);
            Looper.loop();
        }
    }


}