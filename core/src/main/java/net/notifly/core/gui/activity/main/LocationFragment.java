package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_location)
@OptionsMenu(R.menu.fav_locations)
public class LocationFragment extends Fragment implements
        AddressLoader.Callbacks,
        ActionMode.Callback,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    @ViewById(android.R.id.list)
    AbsListView locationsListView;
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
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        locationsListView.setItemChecked(-1, true);
        actionMode = null;
    }
}
