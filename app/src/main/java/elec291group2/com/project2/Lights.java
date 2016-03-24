package elec291group2.com.project2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Kevin on 2016-03-24.
 */
public class Lights extends Fragment
{
    final boolean ON = true, OFF = false;
    SharedPreferences sharedPreferences;
    View view;
    Button masterOnButton, masterOffButton, livingRoomButton, kitchenButton,
            washroomButton, bedroomButton, masterBedroomButton;
    boolean livingRoomStatus = false,
            kitchenStatus = false,
            washroomStatus = false,
            bedroomStatus = false,
            masterBedroomStatus = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        sharedPreferences = this.getActivity().getSharedPreferences("serverData", Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.lights, container, false);

        masterOnButton = (Button) view.findViewById(R.id.master_on_button);
        masterOffButton = (Button) view.findViewById(R.id.master_off_button);
        livingRoomButton = (Button) view.findViewById(R.id.livingroom_button);
        kitchenButton = (Button) view.findViewById(R.id.kitchen_button);
        washroomButton = (Button) view.findViewById(R.id.washroom_button);
        bedroomButton = (Button) view.findViewById(R.id.bedroom_button);
        masterBedroomButton = (Button) view.findViewById(R.id.mbedroom_button);

        updateAllButtons();

        masterOnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                masterControl(ON);
            }
        });

        masterOffButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                masterControl(OFF);
            }
        });

        livingRoomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                livingRoomStatus = !livingRoomStatus;
                updateButton(livingRoomButton, livingRoomStatus);
            }
        });

        kitchenButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                kitchenStatus = !kitchenStatus;
                updateButton(kitchenButton, kitchenStatus);
            }
        });

        washroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                washroomStatus = !washroomStatus;
                updateButton(washroomButton, washroomStatus);
            }
        });

        bedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bedroomStatus = !bedroomStatus;
                updateButton(bedroomButton, bedroomStatus);
            }
        });

        masterBedroomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                masterBedroomStatus = !masterBedroomStatus;
                updateButton(masterBedroomButton, masterBedroomStatus);
            }
        });

        return view;
    }

    public void updateButton(Button btn, boolean status)
    {
        btn.setText(status ? "ON" : "OFF");
        btn.getBackground().setColorFilter(status ? Color.GREEN : Color.RED, PorterDuff.Mode.MULTIPLY);
    }

    public void updateAllButtons()
    {
        updateButton(livingRoomButton, livingRoomStatus);
        updateButton(kitchenButton, kitchenStatus);
        updateButton(livingRoomButton, livingRoomStatus);
        updateButton(washroomButton, washroomStatus);
        updateButton(bedroomButton, bedroomStatus);
        updateButton(masterBedroomButton, masterBedroomStatus);
    }

    public void masterControl(boolean status)
    {
        livingRoomStatus = kitchenStatus = washroomStatus = bedroomStatus = masterBedroomStatus = status;
        updateAllButtons();
    }
}
