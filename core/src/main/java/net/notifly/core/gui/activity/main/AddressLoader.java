package net.notifly.core.gui.activity.main;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;

import net.notifly.core.Notifly;
import net.notifly.core.entity.Location;
import net.notifly.core.util.LocationHandler;

public class AddressLoader extends AsyncTask<Void, Void, Address> {
    Notifly notifly;
    Location location;
    Callbacks listener;

    public AddressLoader(Context context, Location location) {
        this.notifly = (Notifly) context.getApplicationContext();
        this.location = location;
    }

    @Override
    protected Address doInBackground(Void... params) {
        Address address = notifly.get(location);

        if (address == null) {
            address = notifly.getLocationHandler().getAddress(location);
            if (address.equals(LocationHandler.ERROR_ADDRESS))
            {
                return LocationHandler.getAddressesFromWeb(Double.toString(location.getLatitude()) +
                        "," + Double.toString(location.getLongitude()), 1).iterator().next();
            }
        }

        return address;
    }

    @Override
    protected void onPostExecute(Address address) {
        notifly.put(location, address);
        if (listener != null) listener.notifyPostExecute();
        Log.i(this.getClass().getName(), "Done loading: " + location);
    }

    public AddressLoader setListener(Callbacks listener) {
        this.listener = listener;
        return this;
    }

    public interface Callbacks {
        void notifyPostExecute();
    }
}

