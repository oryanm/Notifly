package net.notifly.core.gui.activity.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.note.NewNoteActivity;
import net.notifly.core.gui.activity.note.NewNoteActivity_;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.util.AddressLoader;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_location)
@OptionsMenu(R.menu.fav_locations)
public class LocationFragment extends Fragment implements
        AddressLoader.Callbacks,
        ActionMode.Callback,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        DragSortListView.DropListener {
    public static final String FRAGMENT_TAG = "favorite_locations";

    @App
    Notifly notifly;
    @ViewById(android.R.id.list)
    DragSortListView locationsListView;
    @Bean
    LocationHandler locationHandler;
    @Bean
    LocationAdapter adapter;

    List<Location> locations;

    ActionMode actionMode;

    int selectedLocationPosition = -1;

    public static LocationFragment newInstance() {
        LocationFragment_ fragment = new LocationFragment_();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @AfterViews
    void loadLocations() {
        if (locations == null) {
            LocationDAO locationDAO = new LocationDAO(getActivity());
            locations = locationDAO.getFavoriteLocations();
            locationDAO.close();
            loadAddresses();
            adapter.addAll(locations);
        }

        locationsListView.setAdapter(adapter);
        locationsListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        locationsListView.setOnItemClickListener(this);
        locationsListView.setOnItemLongClickListener(this);
        locationsListView.setDropListener(this);
    }

    private void loadAddresses() {
        for (Location location : locations) {
            if (!LocationHandler.isValid(location.address)) {
                new AddressLoader(getActivity(), location).setListener(this).execute();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getActionBar().setTitle(getString(R.string.title_section_fav_locations));
    }

    @Override
    public void notifyPostExecute() {
        adapter.notifyDataSetChanged();
    }

    void delete(int position){
        Location location = adapter.getItem(position);
        LocationDAO locationDAO = new LocationDAO(getActivity());
        locationDAO.updateAsNotFavorite(location);
        locationDAO.close();
        adapter.remove(location);
    }

    void newNote(int position) {
        Location location = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), NewNoteActivity_.class);
        intent.putExtra(NewNoteActivity.EXTRA_NOTE, new Note("", location));
        startActivityForResult(intent, NewNoteActivity.NEW_NOTE_CODE);
    }

    @OnActivityResult(NewNoteActivity.NEW_NOTE_CODE)
    void afterNewNote(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            Note note = intent.getParcelableExtra(NewNoteActivity.EXTRA_NOTE);
            notifly.addNote(note, this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            return false;
        }

        selectedLocationPosition = position;
        actionMode = getActivity().startActionMode(this);
        view.setSelected(true);
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_fav_locations, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_location:
                delete(selectedLocationPosition);
                mode.finish();
                return true;
            case R.id.action_add_location_note:
                newNote(selectedLocationPosition);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        locationsListView.setItemChecked(-1, true);
        actionMode = null;
    }

    @Override
    public void drop(int from, int to) {
        Log.i(LocationFragment.class.getName(), String.format("Drop range: %d - %d", from, to));
        if (from != to) {
            reorderAdapter(from, to);
            updateOrder(Math.min(from, to), Math.max(from, to));
        }
    }

    private void reorderAdapter(int from, int to) {
        Location item = adapter.getItem(from);
        adapter.remove(item);
        adapter.insert(item, to);
        locationsListView.moveCheckState(from, to);
    }

    private void updateOrder(int from, int to) {
        List<Location> reordered = new ArrayList<Location>();

        for (int i = from; i <= to; i++) {
            Location location = adapter.getItem(i);
            location.setOrder(i);
            reordered.add(location);
        }

        updateOrder(reordered);
    }

    @Background
    void updateOrder(List<Location> reordered) {
        LocationDAO locationDAO = new LocationDAO(this.getActivity());
        locationDAO.updateOrder(reordered);
        locationDAO.close();
    }
}
