package net.notifly.core;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils
{
  private final static double LOWER_LEFT_LATITUDE = 29.39406;
  private final static double LOWER_LEFT_LONGITUDE = 33.21458;
  private final static double UPPER_RIGHT_LATITUDE = 33.14897;
  private final static double UPPER_RIGHT_LONGITUDE = 36.09300;

  static
  {
    // todo way is the default before we set a new default?
    Locale.setDefault(new Locale("he", "IL"));
  }

  public static Address getAddress(Activity activity, double longitude, double latitude) throws IOException
  {
    Geocoder geocoder = new Geocoder(activity.getBaseContext());

    return geocoder.getFromLocation(latitude, longitude, 1).iterator().next();
  }

  public static String getLocationByName(Activity activity, String name) throws IOException
  {
    Geocoder geo = new Geocoder(activity.getBaseContext());
    List<Address> addresses = geo.getFromLocationName(name, /* TODO why 5?*/ 5,
      LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
    return addresses.iterator().next().getFeatureName();
  }
}
