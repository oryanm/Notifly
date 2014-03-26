package net.notifly.core.gui.activity.note;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import net.notifly.core.LocationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends ArrayAdapter<String> implements Filterable
{
  public static final int MAX_RESULTS = 5;

  private List<Address> locations = new ArrayList<Address>();
  private LocationHandler locationHandler;

  public AddressAdapter(Context context, int textViewResourceId)
  {
    super(context, textViewResourceId);

    locationHandler = new LocationHandler(context, false);
  }

  @Override
  public int getCount()
  {
    return locations.size();
  }

  @Override
  public String getItem(int index)
  {
    Address address = locations.get(index);
    //TODO: this string is hard
    return String.format(
      "%s, %s, %s",
      // If there's a street address, add it
      address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
      // Locality is usually a city
      address.getLocality(),
      // The country of the address
      address.getCountryName());
  }

  public Address getAddress(int index)
  {
    return locations.get(index);
  }

  @Override
  public Filter getFilter()
  {
    return new Filter()
    {
      @Override
      protected FilterResults performFiltering(CharSequence constraint)
      {
        FilterResults filterResults = new FilterResults();

        if (constraint != null)
        {
          getAddresses(constraint);
          // Assign the data to the FilterResults
          filterResults.values = locations;
          filterResults.count = locations.size();
        }

        return filterResults;
      }

      private void getAddresses(CharSequence constraint)
      {
        try
        {
          locations = locationHandler.getAddresses(constraint.toString(), MAX_RESULTS);
        } catch (IOException e)
        {
          Log.e(AddressAdapter.class.getName(),
            String.format("Could not load locations from: %s", constraint), e);
        }
      }

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results)
      {
        if (results != null && results.count > 0)
        {
          notifyDataSetChanged();
        } else
        {
          notifyDataSetInvalidated();
        }
      }
    };
  }
}
