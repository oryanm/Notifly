package net.notifly.core.gui.activity.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

import net.notifly.core.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static boolean isLocaleChanged = false;

    private Map<String, Locale> countryToLocaleMap = new HashMap<String, Locale>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        initCountryToLocaleMap();
    }

    private void initCountryToLocaleMap() {
        countryToLocaleMap.put(getString(R.string.israel), new Locale("he", "IL"));
        countryToLocaleMap.put(getString(R.string.usa), new Locale("en", "US"));
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
            Locale.setDefault(countryToLocaleMap.get(
                    sharedPreferences.getString(key, getString(R.string.israel))));
            isLocaleChanged = true;
        }
    }
}