package net.notifly.core;

import android.app.Application;
import android.location.Address;
import android.util.LruCache;

import net.notifly.core.entity.Location;
import net.notifly.core.util.GeneralUtils;

import org.androidannotations.annotations.EApplication;

@EApplication
public class Notifly extends Application{
    LruCache<Location, Address> locationAddressCache = new LruCache<Location, Address>(50);

    public Address get(Location location) {
        return locationAddressCache.get(location);
    }

    public Address put(Location location, Address address) {
        location.address = GeneralUtils.toString(address);
        return locationAddressCache.put(location, address);
    }
}
