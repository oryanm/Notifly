package net.notifly.core;

import android.app.Application;
import android.location.Address;
import android.util.LruCache;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.entity.Location;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.AddressLoader;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.GeneralUtils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@EApplication
public class Notifly extends Application {
    List<Note> notes = new ArrayList<Note>();

    LruCache<Location, Address> locationAddressCache = new LruCache<Location, Address>(50);

    @Override
    public void onCreate() {
        super.onCreate();
        ResourceZoneInfoProvider.init(this);
        loadNotes();
    }

    void loadNotes() {
        NotesDAO notesDAO = new NotesDAO(this);
        notes = notesDAO.getAllNotes();
        notesDAO.close();
        loadLocations();
    }

    @Background
    void loadLocations() {
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

    public void addNote(Note note, @Nullable AddressLoader.Callbacks callback) {
        notes.add(note);
        if (note.hasLocation()) {
            new AddressLoader(this, note.getLocation()).setListener(callback).execute();
        }
    }
}
