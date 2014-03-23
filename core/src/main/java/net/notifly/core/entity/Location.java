package net.notifly.core.entity;

import android.location.Address;

public class Location
{
  private int id;
  private double longitude;
  private double latitude;

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
}
