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

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends ArrayAdapter<String> implements Filterable {
    public static final int MAX_RESULTS = 5;

    private Notifly notifly;

    private List<Address> addresses = new ArrayList<Address>();
    private List<Location> favorites = new ArrayList<Location>();

    public AddressAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        notifly = (Notifly) context.getApplicationContext();
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
        if (addresses.isEmpty()) return "";
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

                if (constraint != null &&
                        // limit constraint to min 3 cause any less might
                        // cause slow reaction from google maps service
                        constraint.length() >= 3) {
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
                addresses.addAll(notifly.getLocationHandler().getAddresses(constraint.toString(), MAX_RESULTS));
            }

            private void getFavoriteAddresses(CharSequence constraint) {
                for (Location location : favorites) {
                    if (location.getTitle().contains(constraint)) {
                        Address address = notifly.get(location);

                        if (address == null) {
                            address = notifly.getLocationHandler().getAddress(location);
                            notifly.put(location, address);
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
