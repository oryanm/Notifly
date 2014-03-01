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
  private final static double LOWER_LEFT_LATITUDE = 29.39406;
  private final static double LOWER_LEFT_LONGITUDE = 33.21458;
  private final static double UPPER_RIGHT_LATITUDE = 33.14897;
  private final static double UPPER_RIGHT_LONGITUDE = 36.09300;

  static
  {
    Locale.setDefault(new Locale("he", "IL"));
  }

  public static String getLocationByName(Activity activity, String address)
  {
    try
    {
      Geocoder geo = new Geocoder(activity.getBaseContext(), Locale.getDefault());
      List<Address> addresses = geo.getFromLocationName(address, 5, LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE,
        UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
      return addresses.get(0).getFeatureName();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return "";
  }

}
