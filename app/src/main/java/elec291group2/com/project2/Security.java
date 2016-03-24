package elec291group2.com.project2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    Handler handler;
    private Socket socket;
    private String ip;
    private String port;
    private boolean newStatus;
    private Runnable getStatus = new Runnable()
    {
        @Override
        public void run()
        {
      /* do what you need to do */

            getStatus();
            // Call itself every 500 ms
            handler.postDelayed(this, 2000);
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

        new Thread(new ClientThread()).start();
        Toast.makeText(this.getContext(), "Client has connected", Toast.LENGTH_SHORT).show();


        kevin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendCommand("kevin");
            }
        });

        handler = new Handler();
        handler.postDelayed(getStatus, 100);

        return view;
    }

    @Override
    public void onPause()
    {
        sendCommand("exit");
        Toast.makeText(this.getContext(), "Client has closed the connection.", Toast.LENGTH_SHORT).show();
        super.onPause();
    }

    private void sendCommand(String command)
    {
        try
        {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(command);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getStatus()
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String newStatus = in.readLine();
            if (!(newStatus == null))  // Retrieve command from Android device, add to device queue
            {
                Toast.makeText(this.getContext(), newStatus, Toast.LENGTH_SHORT).show();
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
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

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }


}