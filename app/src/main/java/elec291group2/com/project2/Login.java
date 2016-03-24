package elec291group2.com.project2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Login extends AppCompatActivity
{
    SharedPreferences sharedPreferences;
    EditText pinField;
    String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("serverData", Context.MODE_PRIVATE);

        Button loginBtn = (Button) findViewById(R.id.login_button);
        pinField = (EditText) findViewById(R.id.pin_field);

        pin = sharedPreferences.getString("PIN", "NOT ENTERED");

        if (pin.equals("NOT ENTERED"))
        {
            AlertDialog.Builder prompt = new AlertDialog.Builder(this);
            prompt.setMessage("Please set a security PIN.");
            final EditText input = new EditText(this);
            prompt.setView(input);
            input.setLayoutParams(new LinearLayout.LayoutParams(50, 30));
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            prompt.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("PIN", input.getText().toString());
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "Pin set.", Toast.LENGTH_SHORT).show();
                    pin = sharedPreferences.getString("PIN", "NOT ENTERED");
                }
            });
            prompt.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    Intent exitApp = new Intent(Intent.ACTION_MAIN);
                    exitApp.addCategory(Intent.CATEGORY_HOME);
                    exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(exitApp);
                }
            });
            AlertDialog dialog = prompt.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface dialog)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            dialog.show();
        }

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                if (pinField.getText().toString().equals(pin))
                {
                    Intent main = new Intent(getApplicationContext(), MainMenu.class);
                    startActivity(main);
                }
                else
                {
                    pinField.setText("");
                    Toast.makeText(getApplicationContext(), "Wrong PIN, try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
