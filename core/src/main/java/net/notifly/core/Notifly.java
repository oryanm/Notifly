package net.notifly.core;

import android.app.Application;
import android.location.Address;
import android.util.LruCache;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.AddressLoader;
import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;

import java.util.ArrayList;
import java.util.List;

@EApplication
public class Notifly extends Application {
    List<Note> notes = new ArrayList<Note>();

    public LruCache<Location, Address> locationAddressCache = new LruCache<Location, Address>(50);

    LocationHandler locationHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        GeneralUtils.initCountryToLocaleMap(this);
        ResourceZoneInfoProvider.init(this);
        locationHandler = new LocationHandler(this);
        loadNotes();
    }

    void loadNotes() {
        NotesDAO notesDAO = new NotesDAO(this);
        notes = notesDAO.getAllNotes();
        notesDAO.close();
        loadAddresses();
    }

    @Background
    public void loadAddresses() {
        for (Note note : notes) {
            if (note.hasLocation()) {
                new AddressLoader(this, note.getLocation()).execute();
            }
        }
    }

    public Address get(Location location) {
        return locationAddressCache.get(location);
    }

    public Address put(Location location, Address address) {
        location.address = address;
        return locationAddressCache.put(location, address);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void addNote(Note note, AddressLoader.Callbacks callback) {
        notes.add(note);
        if (note.hasLocation()) {
            new AddressLoader(this, note.getLocation()).setListener(callback).execute();
        }
    }

    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    public void resetAddresses() {
        locationHandler = new LocationHandler(this);
        locationAddressCache.evictAll();
        loadAddresses();
    }
}
