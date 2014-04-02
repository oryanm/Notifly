package net.notifly.core.gui.activity.main;

import android.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import net.notifly.core.R;
import net.notifly.core.entity.Location;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_location)
public class LocationFragment extends Fragment implements AbsListView.OnItemClickListener {

    @ViewById(android.R.id.list)
    AbsListView locationsListView;

    LocationHandler locationHandler;

    List<Location> locations;

    public static LocationFragment newInstance() {
        LocationFragment_ fragment = new LocationFragment_();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @AfterViews
    void onCreate() {
        locationHandler = new LocationHandler(getActivity());

        if (locations == null) {
            LocationDAO locationDAO = new LocationDAO(getActivity());
            locations = locationDAO.getFavoriteLocations();
            locationDAO.close();

            for (Location location : locations) {
                if (location.address.isEmpty() ||
                        LocationHandler.ERROR_ADDRESS.getFeatureName().equals(location.address)) {
                    getAddress(location);
                }
            }
        }

        if (locations.isEmpty()) setEmptyText();

        ListAdapter adapter = new ArrayAdapter<Location>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, locations);
        locationsListView.setAdapter(adapter);
        // Set OnItemClickListener so we can be notified on item clicks
        locationsListView.setOnItemClickListener(this);
    }

    @Background
    void getAddress(Location location) {
        location.setAddress(GeneralUtils.toString(locationHandler.forceGetAddress(location)));
        update();
    }

    @UiThread
    void update(){
        ((ArrayAdapter) locationsListView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText() {
        View emptyView = locationsListView.getEmptyView();

        if (emptyView instanceof ImageView) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public interface OnFragmentInteractionListener {
//        public void onFragmentInteraction(String id);
    }
}
