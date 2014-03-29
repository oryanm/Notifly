package net.notifly.core.gui.activity.main;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.note.NewNoteActivity_;
import net.notifly.core.service.BackgroundService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final int NEW_NOTE_CODE = 1;
    public static final int NAVIGATION_SECTION_NOTES = 1;
    public static final String EXTRA_NOTE = "net.notifly.core.note";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @FragmentById(R.id.navigation_drawer)
    NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @AfterViews
    void setUp() {
        mTitle = getTitle();
        // init joda time
        ResourceZoneInfoProvider.init(this);

        if (!BackgroundService.ALIVE) {
            Log.d("MainActivity", "started background service");
            this.startService(new Intent(this, BackgroundService.class));
        }
    }

    @OptionsItem(R.id.action_add_note)
    void openNewNoteActivity() {
        SwipeListView list = (SwipeListView) findViewById(R.id.notes_list_view);
        list.closeOpenedItems();

        Intent intent = new Intent(this, NewNoteActivity_.class);
        startActivityForResult(intent, NEW_NOTE_CODE);
    }

    @OnActivityResult(NEW_NOTE_CODE)
    void afterNewNote(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Note note = intent.getParcelableExtra(MainActivity.EXTRA_NOTE);
            ListView list = (ListView) findViewById(R.id.notes_list_view);
            NotesAdapter adapter = (NotesAdapter) list.getAdapter();
            adapter.insert(note, 0);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case NAVIGATION_SECTION_NOTES:
                mTitle = getString(R.string.title_section_notes);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
}
