package elec291group2.com.project2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
        sharedPreferences = getSharedPreferences("serverData", Context.MODE_PRIVATE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nv.getMenu();
        nv.setNavigationItemSelectedListener(this);

        String ip = sharedPreferences.getString("IP", "NOT ENTERED");
        String port = sharedPreferences.getString("Port", "NOT ENTERED");

        menu.findItem(R.id.ip_address).setTitle("IP Address: " + ip);
        menu.findItem(R.id.port).setTitle("Port: " + port);

        if (ip.equals("NOT ENTERED") || port.equals("NOT ENTERED"))
        {
            AlertDialog.Builder prompt = new AlertDialog.Builder(this);
            prompt.setMessage("Please enter your IP address and port.");
            prompt.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });
            prompt.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    onBackPressed();
                }
            });
            prompt.show();
            getFragmentManager().beginTransaction().replace(R.id.relativeLayout, new Settings()).commit();
        }
        else
            getFragmentManager().beginTransaction().replace(R.id.relativeLayout, new Overview()).commit();
    }

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
            Intent exitApp = new Intent(Intent.ACTION_MAIN);
            exitApp.addCategory(Intent.CATEGORY_HOME);
            exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exitApp);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Fragment fragment = null;
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
                toolbar.setTitle("Camera");
                fragment = new Camera();
                break;
            case R.id.nav_settings:
                toolbar.setTitle("Settings");
                fragment = new Settings();
                break;
            default:
                break;
        }
        if(fragment != null)
        {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.relativeLayout, fragment).commit();
            DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
            dl.closeDrawers();
        }
        return true;
    }

    public void onDrawerOpened(View drawerView)
    {
        Menu menu = ((NavigationView) drawerView).getMenu();
        ((NavigationView) drawerView).setNavigationItemSelectedListener(this);

        menu.findItem(R.id.ip_address).setTitle("IP Address: " + sharedPreferences.getString("IP", "not entered"));
        menu.findItem(R.id.port).setTitle("Port: " + sharedPreferences.getString("Port", "not entered"));
    }
}
