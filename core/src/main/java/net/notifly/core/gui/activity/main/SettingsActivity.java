package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.notifly.core.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

}