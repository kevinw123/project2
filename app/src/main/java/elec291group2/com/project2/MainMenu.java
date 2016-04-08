package elec291group2.com.project2;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Overview");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // create the drawer view
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // set the drawer as a listener and get the menu
        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nv.getMenu();
        nv.setNavigationItemSelectedListener(this);

        // retrieve ip address, port, notifications
        String ip = sharedPreferences.getString("IP", "Not set");
        String port = sharedPreferences.getString("Port", "Not set");
        boolean notifStatus = sharedPreferences.getBoolean("Notifications", false);

        // set the menu items to the values
        menu.findItem(R.id.ip_address).setTitle("IP Address: " + ip);
        menu.findItem(R.id.port).setTitle("Port: " + port);
        menu.findItem(R.id.notifications).setTitle("Notifications: " + (notifStatus ? "On" : "Off"));

        // send the user to the settings menu if either IP or Port have not been set
        if (ip.equals("Not set") || port.equals("Not set"))
        {
            toolbar.setTitle("Settings");
            AlertDialog.Builder prompt = new AlertDialog.Builder(this);
            prompt.setMessage("Please enter your IP address and port.");
            prompt.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });
            prompt.show();
            getFragmentManager().beginTransaction().replace(R.id.relativeLayout, new Settings()).commit();
        }
        // if they have been set, open the overview fragment
        else
        {
            getFragmentManager().beginTransaction().replace(R.id.relativeLayout, new Overview()).commit();
        }
    }

    // if drawer is open when back is pressed, close drawer, otherwise go back to overview
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            ((Toolbar) findViewById(R.id.toolbar)).setTitle("Overview");
            getFragmentManager().beginTransaction().replace(R.id.relativeLayout, new Overview()).commit();
        }
    }

    // change to a fragment based on the selected item in the drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // instantiate a fragment
        Fragment fragment = null;
        // determine which item was selected
        switch(item.getItemId())
        {
            case R.id.nav_overview:
                toolbar.setTitle("Overview");
                fragment = new Overview();
                break;
            case R.id.nav_security:
                toolbar.setTitle("Security");
                fragment = new Security();
                break;
            case R.id.nav_camera:
                Intent camera = new Intent(getApplicationContext(), Camera.class);
                startActivity(camera);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case R.id.nav_lights:
                toolbar.setTitle("Lights");
                fragment = new Lights();
                break;
            case R.id.nav_settings:
                toolbar.setTitle("Settings");
                fragment = new Settings();
                break;
            default:
                break;
        }
        // go to the new fragment and inflate it on screen
        if(fragment != null)
        {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.relativeLayout, fragment).commit();
            DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
            dl.closeDrawers();
        }
        return true;
    }
}

