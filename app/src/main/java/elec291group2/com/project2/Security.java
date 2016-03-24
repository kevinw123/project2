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
    SharedPreferences sharedPreferences;
    View view;
    TextView textStatus;
    Handler handler;
    int i = 0;
    BufferedReader in;
    PrintWriter out;
    private Socket socket;
    private String ip;
    private String port;
    private String status;
    private boolean newStatus;
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
        sharedPreferences = this.getActivity().getSharedPreferences("serverData", Context.MODE_PRIVATE);
        ip = sharedPreferences.getString("IP", "NOT ENTERED");
        port = sharedPreferences.getString("Port", "NOT ENTERED");
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
        /*
        try
        {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }*/
        Toast.makeText(this.getContext(), "Client has closed the connection.", Toast.LENGTH_SHORT).show();
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

    private void getStatus()
    {
        try
        {
            if (in.ready())  // Retrieve command from Android device, add to device queue
            {

                status = in.readLine();
                System.out.println(status);
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
                socket = new Socket(ip, Integer.parseInt(port));
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