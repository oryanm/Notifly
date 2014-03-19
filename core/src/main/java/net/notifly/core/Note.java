package net.notifly.core;

import org.joda.time.LocalDateTime;

//TODO implement Parcelable
public class Note
{
  String title;
  String description;
  LocalDateTime time;
  Location location;

  public Note(String title, LocalDateTime time)
  {
    this.title = title;
    this.time = time;
  }

  public Note(String title, Location location)
  {
    this.title = title;
    this.location = location;
  }

  public Note(String title, LocalDateTime time, Location location)
  {
    this.title = title;
    this.time = time;
    this.location = location;
  }

  public String getTitle()
  {
    return title;
  }

  public LocalDateTime getTime()
  {
    return time;
  }

  public Location getLocation()
  {
    return location;
  }
}
