package net.notifly.core.gui.activity.note;

import android.content.Context;
import android.location.Address;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import net.notifly.core.Notifly;
import net.notifly.core.entity.Location;
import net.notifly.core.sql.LocationDAO;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends ArrayAdapter<String> implements Filterable {
    public static final int MAX_RESULTS = 5;

    private List<Address> addresses = new ArrayList<Address>();
    private List<Location> favorites = new ArrayList<Location>();
    private LocationHandler locationHandler;

    public AddressAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        locationHandler = new LocationHandler(context);

        LocationDAO locationDAO = new LocationDAO(context);
        favorites = locationDAO.getFavoriteLocations();
        locationDAO.close();
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public String getItem(int index) {
        return GeneralUtils.toString(addresses.get(index));
    }

    public Address getAddress(int index) {
        return addresses.get(index);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    filterResults.count = 0;
                    addresses.clear();
                    filterResults.values = addresses;

                    getFavoriteAddresses(constraint);
                    getAddresses(constraint);

                    // Assign the data to the FilterResults
                    filterResults.values = addresses;
                    filterResults.count = addresses.size();
                }

                return filterResults;
            }

            private void getAddresses(CharSequence constraint) {
                addresses.addAll(locationHandler.getAddresses(constraint.toString(), MAX_RESULTS));
            }

            private void getFavoriteAddresses(CharSequence constraint) {
                for (Location location : favorites) {
                    if (location.getTitle().contains(constraint)) {
                        Address address = ((Notifly) getContext().getApplicationContext()).get(location);

                        if (address == null) {
                            address = locationHandler.getAddress(location);
                            ((Notifly) getContext().getApplicationContext()).put(location, address);
                        }

                        addresses.add(address);
                    }
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
