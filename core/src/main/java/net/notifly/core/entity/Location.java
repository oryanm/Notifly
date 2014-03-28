package net.notifly.core.entity;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Location implements Parcelable
{
  private int id;
  private double longitude;
  private double latitude;

  // todo: flip long and lat
  public Location(int id, double longitude, double latitude)
  {
    this.id = id;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public Location(double longitude, double latitude)
  {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public static Location from(Address address)
  {
    return new Location(address.getLongitude(), address.getLatitude());
  }

  public static Location from(LatLng latLng)
  {
    return new Location(latLng.longitude, latLng.latitude);
  }

  public static Location from(android.location.Location location)
  {
    return new Location(location.getLongitude(), location.getLatitude());
  }

  public int getId()
  {
    return id;
  }

  public double getLongitude()
  {
    return longitude;
  }

  public double getLatitude()
  {
    return latitude;
  }

  @Override
  public String toString()
  {
    return getLatitude() + "," + getLongitude();
  }

  @Override
  public int describeContents()
  {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(id);
    dest.writeDouble(latitude);
    dest.writeDouble(longitude);
  }

  public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>()
  {
    public Location createFromParcel(Parcel in)
    {
      return new Location(in);
    }

    public Location[] newArray(int size)
    {
      return new Location[size];
    }
  };

  private Location(Parcel in)
  {
    this.id = in.readInt();
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
  }
}
