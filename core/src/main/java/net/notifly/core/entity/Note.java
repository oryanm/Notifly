package net.notifly.core.entity;


import android.os.Parcel;
import android.os.Parcelable;

import net.notifly.core.util.TravelMode;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Note implements Parcelable {
    int id = -1;
    String title;
    String description;
    LocalDateTime time;
    Repetition repetition;
    Location location;
    TravelMode travelMode = TravelMode.DRIVING;
    Set<String> tags = new HashSet<String>();

    public Note(String title, LocalDateTime time) {
        this.title = title;
        this.time = time;
    }

    public Note(String title, Location location) {
        this.title = title;
        this.location = location;
    }

    public Note(String title, LocalDateTime time, Location location) {
        this.title = title;
        this.time = time;
        this.location = location;
    }

    public Note() {
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Repetition getRepetition() {
        return repetition;
    }

    public Location getLocation() {
        return location;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setRepetition(Repetition repetition) {
        this.repetition = repetition;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public boolean hasTime() {
        return this.time != null;
    }

    public boolean repeats() {
        return this.repetition != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(time == null ? 0 : time.toDate().getTime());
        dest.writeParcelable(location, flags);
        dest.writeString(travelMode.toString());
        dest.writeStringList(new ArrayList<String>(tags));
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    private Note(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        long timeLong = in.readLong();
        this.time = timeLong == 0 ? null : new LocalDateTime(timeLong);
        this.location = in.readParcelable(Location.class.getClassLoader());
        this.travelMode = TravelMode.getMode(in.readString());
        ArrayList<String> tags = new ArrayList<String>();
        in.readStringList(tags);
        this.tags = new HashSet<String>(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        return id == note.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
