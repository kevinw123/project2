package elec291group2.com.project2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.design.widget.NavigationView;
import android.view.Menu;

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
    }
}
