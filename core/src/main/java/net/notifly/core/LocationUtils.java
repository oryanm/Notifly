package net.notifly.core;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Barak on 27/02/14.
 */
public class LocationUtils
{
  static
  {
    Locale.setDefault(new Locale("he", "IL"));
  }

  public static String getLocationByName(Activity activity, String address)
  {
    try
    {
      Geocoder geo = new Geocoder(activity.getBaseContext(), Locale.getDefault());
      List<Address> addresses = geo.getFromLocationName(address, 5, 29.39406, 33.21458,
        33.14897, 36.09300);
      return addresses.get(0).getFeatureName();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return "";
  }

}
