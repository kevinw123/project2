package elec291group2.com.project2;

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
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

/**
 * Created by Kevin Qiu on 2016-03-29.
 */
public class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        updatePreference(findPreference(key), key);

        Menu menu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        menu.findItem(R.id.ip_address).setTitle("IP Address: " + sharedPreferences.getString("IP", "not entered"));
        menu.findItem(R.id.port).setTitle("Port: " + sharedPreferences.getString("Port", "not entered"));
    }

    private void updatePreference(Preference preference, String key)
    {

        if(key.equals("auth_key"))
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String un_hashed = sharedPreferences.getString("auth_key", "1234");
            //sharedPreferences.edit().putString("auth_key","1234").apply();
            new hash().execute(un_hashed);
            return;
        }
        if (preference == null || key.equals("PIN")) return;
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
            checkBoxPreference.setSummary(checkBoxPreference.isChecked() ? "Enabled" : "Disabled");
            SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            preference.setSummary(sharedPrefs.getBoolean(key, false) ? "Enabled" : "Disabled");
            if(!sharedPrefs.getBoolean(key,false))
                preference.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_dnd_forwardslash_24dp, null));
            else
                preference.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_error_24dp, null));
        }
    }

    private class hash extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... params) {
            Log.v("UN_HASHED", params[0]);
            return encrytionFunction.password_hash(params[0].toString());
        }

        protected void onPostExecute(String result) {
            //Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            Log.v("HASHED", result);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putString("auth_key", result).apply();
        }
    }
}
