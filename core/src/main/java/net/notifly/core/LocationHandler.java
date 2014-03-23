package net.notifly.core;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

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

  private Location currentLocation;
  private Geocoder geocoder;

  public LocationHandler(Context context)
  {
    setLocationTracker(context);
    geocoder = new Geocoder(context, new Locale("he", "IL"));
  }

  public Address getAddress(double longitude, double latitude) throws IOException
  {
    return geocoder.getFromLocation(latitude, longitude, 1).iterator().next();
  }

  public String getLocationByName(String name) throws IOException
  {
    return getLocationsByName(name, /*TODO: why 5?*/ 5).iterator().next().getFeatureName();
  }

  public List<Address> getLocationsByName(String name, int maxResults) throws IOException
  {
    return geocoder.getFromLocationName(name, maxResults,
      LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
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

  public void setLocationTracker(Context context)
  {
    LocationManager locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
    currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
      LOCATION_REFRESH_DISTANCE, new LocationAdapter()
      {
        @Override
        public void onLocationChanged(android.location.Location location)
        {
          currentLocation = location;
          Log.d("Location Update: ", location.toString());
        }
      }
    );
  }

  public Location getCurrentLocation()
  {
    return currentLocation;
  }
}
