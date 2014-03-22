package net.notifly.core;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.sql.NotesDAO;

import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity
  implements NavigationDrawerFragment.NavigationDrawerCallbacks
{
  private static final int NEW_NOTE_CODE = 1;

  public static final int NAVIGATION_SECTION_NOTES = 1;

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

      sample();
    }

  private void sample()
  {
    try
    {
      Location currentLocation = LocationUtils.getSingleton().getCurrentLocation();
      String origin = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
      String distanceAndDuration = new RetreiveResultTask().execute(origin, "Tel-Aviv", "walking").get();
      Toast.makeText(this, distanceAndDuration, Toast.LENGTH_LONG).show();
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    } catch (ExecutionException e)
    {
      e.printStackTrace();
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
          reloadNotes();
        }
    }
  }

  private void reloadNotes()
  {
    ListView list = (ListView)findViewById(R.id.notes_list_view);
    NotesAdapter adapter = (NotesAdapter) list.getAdapter();

    adapter.clear();

    NotesDAO notesDAO = new NotesDAO(this);
    // todo: maybe use addAll (requires minSDKLevel 11)
    for (Note note : notesDAO.getAllNotes())
    {
      adapter.add(note);
    }

    adapter.notifyDataSetChanged();
    notesDAO.close();
  }

  /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      int section = getArguments().getInt(ARG_SECTION_NUMBER);

      switch (section)
      {
        case NAVIGATION_SECTION_NOTES:
        {
          createNotesListView(rootView);
        }
      }
      return rootView;
    }

    private void createNotesListView(View rootView)
    {
      NotesDAO notesDAO = new NotesDAO(getActivity());
      final NotesAdapter adapter = new NotesAdapter(getActivity(), notesDAO.getAllNotes());
      notesDAO.close();

      SwipeListView list = (SwipeListView) rootView.findViewById(R.id.notes_list_view);
      list.setAdapter(adapter);
    }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
