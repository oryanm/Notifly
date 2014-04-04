package net.notifly.core.gui.activity.main;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import net.notifly.core.Notifly;
import net.notifly.core.entity.Location;
import net.notifly.core.util.LocationHandler;

public class AddressLoader extends AsyncTask<Void, Void, Address> {
    Notifly notifly;
    LocationHandler locationHandler;
    ArrayAdapter adapter;
    Location location;

    public AddressLoader(Context context, ArrayAdapter adapter, Location location) {
        this.notifly = (Notifly) context.getApplicationContext();
        this.locationHandler = new LocationHandler(context);
        this.adapter = adapter;
        this.location = location;
    }

    @Override
    protected Address doInBackground(Void... params) {
        return locationHandler.getAddress(location);
    }

    @Override
    protected void onPostExecute(Address address) {
        notifly.put(location, address);
        adapter.notifyDataSetChanged();
        Log.i(this.getClass().getName(), "Done loading: " + location);
    }
}

