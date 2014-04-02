package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.service.BackgroundService;
import net.notifly.core.sql.NotesDAO;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.NonConfigurationInstance;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        LocationFragment.OnFragmentInteractionListener {
    public static final int NAVIGATION_SECTION_NOTES = 0;

    @FragmentById(R.id.navigation_drawer)
    NavigationDrawerFragment navigationDrawerFragment;

    @AfterViews
    void setUp() {
        // init joda time
        ResourceZoneInfoProvider.init(this);

        if (!BackgroundService.ALIVE) {
            Log.d("MainActivity", "started background service");
            this.startService(new Intent(this, BackgroundService.class));
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;
        String tag;

        switch (position) {
            case NAVIGATION_SECTION_NOTES:
                tag = "notes";
                fragment = NotesMainFragment.newInstance();
                break;
            default:
                tag = "favorite_locations";
                fragment = getFragmentManager().findFragmentByTag(tag);

                if (fragment == null) {
                    fragment = LocationFragment.newInstance();
                }
        }

        // update the main content by replacing fragments
        getFragmentManager().beginTransaction().replace(R.id.container, fragment, tag).commit();
    }
}