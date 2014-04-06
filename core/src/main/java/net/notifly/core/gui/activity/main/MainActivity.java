package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import net.notifly.core.R;
import net.notifly.core.service.BackgroundService;
import net.notifly.core.service.BackgroundService_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final int NAVIGATION_SECTION_NOTES = 0;

    @FragmentById(R.id.navigation_drawer)
    NavigationDrawerFragment navigationDrawerFragment;

    @AfterViews
    void setUp() {
        if (!BackgroundService.ALIVE) {
            Log.d("MainActivity", "started background service");
            BackgroundService_.intent(getApplication()).start();
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