package net.notifly.core.entity;


import org.joda.time.LocalDateTime;

//TODO implement Parcelable
public class Note
{
  int id;
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

  public Note()
  {
  }

  public int getId()
  {
    return id;
  }

  public String getTitle()
  {
    return title;
  }

  public String getDescription()
  {
    return description;
  }

  public LocalDateTime getTime()
  {
    return time;
  }

  public Location getLocation()
  {
    return location;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void setTime(LocalDateTime time)
  {
    this.time = time;
  }

  public void setLocation(Location location)
  {
    this.location = location;
  }
}
