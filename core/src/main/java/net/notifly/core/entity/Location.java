package net.notifly.core.entity;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import net.notifly.core.util.GeneralUtils;
import net.notifly.core.util.LocationHandler;

public class Location implements Parcelable {
    private int id;
    private double longitude;
    private double latitude;
    boolean isFavorite = false;
    String title = "";
    int order = -1;

    // transient member
    public Address address = LocationHandler.ERROR_ADDRESS;

    public Location(int id, double longitude, double latitude, int order) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.order = order;
    }

    private Location(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    private Location(Address address) {
        this.longitude = address.getLongitude();
        this.latitude = address.getLatitude();
        this.address = address;
    }

    public Location asFavorite(String title) {
        Location location = new Location(this.id, this.latitude, this.longitude, this.order);
        location.isFavorite = true;
        location.title = title;
        return location;
    }

    public static Location from(Address address) {
        return new Location(address);
    }

    public static Location from(LatLng latLng) {
        return new Location(latLng.latitude, latLng.longitude);
    }

    public static Location from(android.location.Location location) {
        return new Location(location.getLatitude(), location.getLongitude());
    }

    public int getId() {
        return id;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return LocationHandler.isValid(address) ? GeneralUtils.toString(address) :
                !title.isEmpty() ? title :
                        getLatitude() + "," + getLongitude();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        GeneralUtils.writeBoolean(dest, isFavorite);
        dest.writeString(title);
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private Location(Parcel in) {
        this.id = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.isFavorite = GeneralUtils.readBoolean(in);
        this.title = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (Double.compare(location.latitude, latitude) != 0) return false;
        if (Double.compare(location.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(longitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
