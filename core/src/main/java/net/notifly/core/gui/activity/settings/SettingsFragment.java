package net.notifly.core.gui.activity.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import net.notifly.core.R;
import net.notifly.core.util.GeneralUtils;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static boolean isLocaleChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Set the current location by the default locale
        setCurrentLocationByDefaultLocale();
    }

    private void setCurrentLocationByDefaultLocale() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        String key = getString(R.string.curr_location_preference_key);
        if (sharedPreferences.getString(key, null) == null)
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String country = GeneralUtils.countryToLocaleMap.inverse().get(Locale.getDefault());
            editor.putString(key, country);
            editor.commit();

            // Update UI
            ((ListPreference)findPreference(key)).setValue(country);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.action_settings));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.curr_location_preference_key)))
        {
            Locale.setDefault(GeneralUtils.countryToLocaleMap.get(
                    sharedPreferences.getString(key, getString(R.string.israel))));
            isLocaleChanged = true;
        }
        else
        {
            Toast.makeText(getActivity(), "Changes in note's preferences will only take affect " +
                    "the next time the note is being notified to the user", Toast.LENGTH_LONG).show();
        }
    }
}