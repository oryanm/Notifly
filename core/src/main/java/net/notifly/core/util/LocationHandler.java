package net.notifly.core.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import net.notifly.core.entity.DistanceMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationHandler
{
  private final static String DISTANCE_MATRIX_URL = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=:org&destinations=:dest&mode=:mode&language=en-US&sensor=false";

  private final static double LOWER_LEFT_LATITUDE = 29.39406;
  private final static double LOWER_LEFT_LONGITUDE = 33.21458;
  private final static double UPPER_RIGHT_LATITUDE = 33.14897;
  private final static double UPPER_RIGHT_LONGITUDE = 36.09300;
  private static final long LOCATION_REFRESH_TIME = 5;
  private static final float LOCATION_REFRESH_DISTANCE = 5;

    /**
     * use this const to signify en error with address loading. <br/>
     * IMPORTANT: because we use {@link android.os.Parcelable} to pass objects around
     * and because {@link android.location.Address} doesn't implements {@code Object.equals()},
     * we have to override if ourselves. this also means that: {@code address.equals(ERROR_ADDRESS)} is
     * always false so we have to always use: {@code ERROR_ADDRESS.equals(address)} <br/>
     * Use {@code LocationHandler.isValid(Address)} to be sure you're dealing we the good stuff.
     */
    public static final Address ERROR_ADDRESS = new Address(Locale.getDefault()) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Address && ((Address) o).getLatitude() == getLatitude();
        }

        {
            setLongitude(-1);
            setLatitude(-1);
            setAddressLine(0, "Something went wrong");
            setFeatureName("Something went wrong");
        }
    };

    private Geocoder geocoder;

  public LocationHandler(Context context)
  {
    geocoder = new Geocoder(context, new Locale("he", "IL"));
  }

  public Address getAddress(net.notifly.core.entity.Location location)
  {
    return getAddress(location.getLatitude(), location.getLongitude());
  }

    public Address getAddress(double latitude, double longitude) {
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(LocationHandler.class.getName(), "Failed to load address from google", e);
            return ERROR_ADDRESS;
        }

        if (addresses == null || addresses.isEmpty()) {
            Log.w(LocationHandler.class.getName(), String.format(
                    "Could not find address at (%f, %f)", latitude, longitude));
            return ERROR_ADDRESS;
        }

        return addresses.iterator().next();
    }

    public static boolean isValid(Address address) {
        return !ERROR_ADDRESS.equals(address);
    }

  public String getLocationByName(String name) throws IOException
  {
    return getAddresses(name, /*TODO: why 5?*/ 5).iterator().next().getFeatureName();
  }

  public List<Address> getAddresses(String name, int maxResults) throws IOException
  {
    List<Address> addresses = geocoder.getFromLocationName(name, maxResults,
      LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
    // in case google returns null
    return addresses != null ? addresses : new ArrayList<Address>();
  }

  public static DistanceMatrix getDistanceMatrix(String orgAddress, String destAddress, String mode) throws IOException, JSONException
  {
    String urlString = DISTANCE_MATRIX_URL;
    urlString = urlString.replace(":org", orgAddress);
    urlString = urlString.replace(":dest", destAddress);
    urlString = urlString.replace(":mode", mode);

    // get the JSON And parse it to get the data.
    URL url = new URL(urlString);
    HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
    urlConnection.setRequestMethod("GET");
    urlConnection.connect();

    InputStream inStream = urlConnection.getInputStream();
    BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

    String temp, response = "";
    while((temp = bReader.readLine()) != null){
      //Parse data
      response += temp;
    }
    //Close the reader, stream & connection
    bReader.close();
    inStream.close();
    urlConnection.disconnect();

    //Sortout JSONresponse
    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
    JSONArray rows = object.getJSONArray("rows");

    JSONArray elements = (JSONArray) rows.getJSONObject(0).get("elements");

    JSONObject distance = (JSONObject) ((JSONObject)elements.get(0)).get("distance");
    JSONObject duration = (JSONObject) ((JSONObject)elements.get(0)).get("duration");

    return new DistanceMatrix(duration.getLong("value"), distance.getLong("value"),
      duration.getString("text"), distance.getString("text"));
  }
}
