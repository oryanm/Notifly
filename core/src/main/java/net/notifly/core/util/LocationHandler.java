package net.notifly.core.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import net.notifly.core.entity.DistanceMatrix;

import org.androidannotations.annotations.EBean;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@EBean
public class LocationHandler {
    public static class TravelMode
    {
        public static final String DRIVING = "driving";
        public static final String WALKING = "walking";
        public static final String BICYCLING = "bicycling";
        public static final String TRANSIT = "transit";
    }

    // TODO: Incorporate this in the app to get transit distance and time
    private final static String DIRECTIONS_URL = "http://maps.googleapis.com/maps/api/directions/" +
            "json?origin=:org&destination=:dest&sensor=false&departure_time=:departTime&mode=transit";

    private final static String DISTANCE_MATRIX_URL = "http://maps.googleapis.com/maps/api/distancematrix/" +
            "json?origins=:org&destinations=:dest&mode=:mode&language=:displayLanguage&sensor=true";

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/" +
            "json?address=:geoAddress&language=:displayLanguage&sensor=true&" +
            "bounds=:lowerLeftLatitude,:lowerLeftLongitude|:upperRightLatitude,:upperRightLongitude";

    private final static double LOWER_LEFT_LATITUDE = 29.39406;
    private final static double LOWER_LEFT_LONGITUDE = 33.21458;
    private final static double UPPER_RIGHT_LATITUDE = 33.14897;
    private final static double UPPER_RIGHT_LONGITUDE = 36.09300;

    /**
     * use this const to signify en error with address loading. <br/>
     * IMPORTANT: because we use {@link android.os.Parcelable} to pass objects around
     * and because {@link android.location.Address} doesn't implements {@code Object.equals()},
     * we have to override it ourselves. this also means that: {@code address.equals(ERROR_ADDRESS)} is
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

    public LocationHandler(Context context) {
        geocoder = new Geocoder(context, new Locale("he", "IL"));
    }

    public Address getAddress(net.notifly.core.entity.Location location) {
        return getAddress(location.getLatitude(), location.getLongitude());
    }

    public Address getAddress(double latitude, double longitude) {
        Log.d(LocationHandler.class.getName(), String.format(
                "Looking for address at (%f, %f)", latitude, longitude));
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

    public static List<Address> getAddressesFromWeb(String geoAddress, int maxResults) {
        List<Address> addresses = new ArrayList<Address>();

        try {
            String urlString = GEOCODE_URL;
            urlString = urlString.replace(":lowerLeftLatitude", String.valueOf(LOWER_LEFT_LATITUDE));
            urlString = urlString.replace(":lowerLeftLongitude", String.valueOf(LOWER_LEFT_LONGITUDE));
            urlString = urlString.replace(":upperRightLatitude", String.valueOf(UPPER_RIGHT_LATITUDE));
            urlString = urlString.replace(":upperRightLongitude", String.valueOf(UPPER_RIGHT_LONGITUDE));
            urlString = urlString.replace(":geoAddress", URLEncoder.encode(geoAddress, "utf-8"));
            urlString = setDisplayLanguage(urlString);

            // get the JSON And parse it to get the data.
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                //Parse data
                response += temp;
            }
            //Close the reader, stream & connection
            bReader.close();
            inStream.close();
            urlConnection.disconnect();

            //Sortout JSONresponse
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            JSONArray results = object.getJSONArray("results");
            if (results.length() < maxResults) maxResults = results.length();
            for (int i = 0; i < maxResults; i++)
            {
                Address address = new Address(Locale.getDefault());

                JSONArray address_components = results.getJSONObject(i).getJSONArray("address_components");
                for (int j = 0; j < address_components.length(); j++) {
                    address.setAddressLine(j, address_components.getJSONObject(j).getString("long_name"));
                }

                address.setFeatureName(results.getJSONObject(i).getString("formatted_address"));

                JSONObject location = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");

                address.setLatitude(location.getDouble("lat"));
                address.setLongitude(location.getDouble("lng"));

                addresses.add(address);
            }

            return addresses;
        }
        catch (Exception e)
        {
            return Arrays.asList(ERROR_ADDRESS);
        }
    }

    private static String setDisplayLanguage(String urlString) {
        return urlString.replace(":displayLanguage", Locale.getDefault().toString());
    }

    public static boolean isValid(Address address) {
        return !ERROR_ADDRESS.equals(address);
    }

    public List<Address> getAddresses(String name, int maxResults) {
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(name, maxResults,
                    LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
        } catch (IOException e) {
            try {
                addresses = getAddressesUsingTask(name, maxResults);
            } catch (Exception e1) {
                Log.e(LocationHandler.class.getName(),
                        String.format("Failed to load addresses from: %s", name), e);
            }
        }

        // in case google returns null or exception happened
        return addresses != null ? addresses : new ArrayList<Address>();
    }

    private List<Address> getAddressesUsingTask(String name, int maxResults)
            throws InterruptedException, ExecutionException {
        return new AsyncTask<Object, Void, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Object... params) {
                List<Address> addresses = getAddressesFromWeb(params[0].toString(), (Integer) params[1]);
                return (addresses.isEmpty() ||
                        addresses.iterator().next().equals(ERROR_ADDRESS)) ? null : addresses;
            }
        }.execute(name, maxResults).get();
    }

    public static DistanceMatrix getDistanceMatrix(String orgAddress, String destAddress, String mode) throws IOException, JSONException {
        String urlString = DISTANCE_MATRIX_URL;
        urlString = urlString.replace(":org", orgAddress);
        urlString = urlString.replace(":dest", destAddress);
        urlString = urlString.replace(":mode", mode);
        urlString = setDisplayLanguage(urlString);

        // get the JSON And parse it to get the data.
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inStream = urlConnection.getInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

        String temp, response = "";
        while ((temp = bReader.readLine()) != null) {
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

        JSONObject distance = (JSONObject) ((JSONObject) elements.get(0)).get("distance");
        JSONObject duration = (JSONObject) ((JSONObject) elements.get(0)).get("duration");

        return new DistanceMatrix(duration.getLong("value"), distance.getLong("value"),
                duration.getString("text"), distance.getString("text"));
    }

    public static String getLatitudeLongitudeString(net.notifly.core.entity.Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    public static String getLatitudeLongitudeString(Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    public static DistanceMatrix getDistanceMatrixUsingTask(String orgAddress, String destAddress,
                                                            String mode) throws ExecutionException, InterruptedException {
        return new AsyncTask<String, Void, DistanceMatrix>() {
            @Override
            protected DistanceMatrix doInBackground(String... params) {
                try {
                    return getDistanceMatrix(params[0], params[1], params[2]);
                } catch (Exception e) {
                    return null;
                }
            }
        }.execute(orgAddress, destAddress, mode).get();
    }
}
