package net.notifly.core.gui.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.R;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.note.NewNoteActivity;
import net.notifly.core.service.BackgroundService;

public class MainActivity extends ActionBarActivity
  implements NavigationDrawerFragment.NavigationDrawerCallbacks
{
  private static final int NEW_NOTE_CODE = 1;

  public static final int NAVIGATION_SECTION_NOTES = 1;
  public static final String EXTRA_NOTE = "net.notifly.core.note";

  /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init joda time
        ResourceZoneInfoProvider.init(this);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

      if (!BackgroundService.ALIVE)
      {
        Log.d("MainActivity", "started background service");
        this.startService(new Intent(this, BackgroundService.class));
      }
    }

  @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
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
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_settings)
    {
      return true;
    } else if (id == R.id.action_add_note)
    {
      openNewNoteActivity();
    }

    return super.onOptionsItemSelected(item);
  }

  private void openNewNoteActivity()
  {
    SwipeListView list = (SwipeListView) findViewById(R.id.notes_list_view);
    list.closeOpenedItems();

    Intent intent = new Intent(this, NewNoteActivity.class);
    startActivityForResult(intent, NEW_NOTE_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);

    switch (requestCode)
    {
      case NEW_NOTE_CODE:
        if (resultCode == RESULT_OK)
        {
          afterNewNote(intent);
        }
    }
  }

  private void afterNewNote(Intent intent)
  {
    Note note = intent.getParcelableExtra(MainActivity.EXTRA_NOTE);
    ListView list = (ListView)findViewById(R.id.notes_list_view);
    NotesAdapter adapter = (NotesAdapter) list.getAdapter();
    adapter.insert(note, 0);
    adapter.notifyDataSetChanged();
  }
}
