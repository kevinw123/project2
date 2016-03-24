package elec291group2.com.project2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import elec291group2.com.project2.R;

public class Login extends AppCompatActivity
{
    EditText pin;
    String pw;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button loginBtn = (Button) findViewById(R.id.login_button);
        pin = (EditText) findViewById(R.id.pin_field);

        // TO BE CHANGED
        // ALLOW CHANGING PIN IN SETTINGS AND PROMPT USER ON FIRST OPEN TO SET THEIR PIN
        pw = "1234";

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(pin.getText().toString().equals(pw))
                {
                    Intent main = new Intent(getApplicationContext(), MainMenu.class);
                    startActivity(main);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Wrong PIN, try again." , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
