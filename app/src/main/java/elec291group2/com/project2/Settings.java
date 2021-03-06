package elec291group2.com.project2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import elec291group2.com.project2.gcm.RegistrationIntentService;
import elec291group2.com.project2.gcm.constants;

/**
 * Created by Kevin Qiu on 2016-03-29.
 */
public class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    String last_hash = "";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // create the settings fragment
        super.onCreate(savedInstanceState);
        // get preferences from the XML layout
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // update each preference from the menu
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i)
        {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup)
            {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j)
                {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref, singlePref.getKey());
                }
            }
            else
            {
                updatePreference(preference, preference.getKey());
            }
        }
    }

    // run this when a preference is changed from the menu
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
    {
        // update the preference
        updatePreference(findPreference(key), key);
        String un_hashed = sharedPreferences.getString("auth_key", "1234");
        if(key.equals("auth_key") && !un_hashed.equals(last_hash))
        {
            //sharedPreferences.edit().putString("auth_key","1234").apply();
            new hash().execute(un_hashed);
        }

        // update the IP and Port on the drawer
        Menu menu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        menu.findItem(R.id.ip_address).setTitle("IP Address: " + sharedPreferences.getString("IP", ""));
        menu.findItem(R.id.port).setTitle("Port: " + sharedPreferences.getString("Port", ""));

        // Register with GCM and app server if Notifications switches to 'enabled'
        if ( key.equals("Notifications")
                && sharedPreferences.getBoolean("Notifications", false)
                && checkPlayServices() )
        {
            Context context = getActivity();

            // Create broadcast receiver to update button when registration is done.
            BroadcastReceiver registrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Revert to unchecked button if registration failed.
                    if (!intent.getBooleanExtra("registrationResult", false)){
                        CheckBoxPreference notification_checkbox =
                                (CheckBoxPreference) findPreference("Notifications");
                        notification_checkbox.setChecked(false);
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter(constants.BROADCAST_REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(context).
                    registerReceiver(registrationBroadcastReceiver, intentFilter);

            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(context, RegistrationIntentService.class);
            context.startService(intent);
        }
    }

    // Update the Preference's summary on the Settings screen
    private void updatePreference(Preference preference, String key)
    {
        // do not update the summaries if the changed pref was PIN or Authenication key
        if (preference == null || key.equals("PIN") || key.equals("auth_key")) return;
        // update the summary of Preference based on it's type
        if (preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
            SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            preference.setSummary(sharedPrefs.getString(key, "Default"));
        }
        else if (preference instanceof EditTextPreference)
        {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            editTextPreference.setSummary(editTextPreference.getText());
            SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            preference.setSummary(sharedPrefs.getString(key, ""));
        }
        else if (preference instanceof CheckBoxPreference)
        {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            if (isAdded()){
                if(!sharedPrefs.getBoolean(key,false))
                    preference.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_dnd_forwardslash_24dp, null));
                else
                    preference.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_error_24dp, null));
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a toast explaining the error code.
     */
    private boolean checkPlayServices() {
        //Context context = this.getContext().getApplicationContext();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS)
        {
            // If failed, display error dialogs
            String errorString = constants.MESSAGE_PLAY_SERVICES_ERROR +
                    apiAvailability.getErrorString(resultCode);
            Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(getActivity(), resultCode,
                        constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    private class hash extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... params) {
            Log.v("UN_HASHED", params[0]);
            return EncryptionFunction.password_hash(params[0].toString());
        }

        protected void onPostExecute(String result) {
            Log.v("HASHED", result);
            last_hash = result;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putString("auth_key", result).apply();
        }
    }
}
