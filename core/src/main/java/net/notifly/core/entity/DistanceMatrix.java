package net.notifly.core.entity;

/**
 * Created by Barak on 22/03/2014.
 */
public class DistanceMatrix
{
  private long duration; // In seconds
  private long distance; // In meters

  private String durationText;
  private String distanceText;

  public DistanceMatrix(long duration, long distance)
  {
    this.duration = duration;
    this.distance = distance;
  }

  public DistanceMatrix(long duration, long distance, String durationText, String distanceText)
  {
    this.duration = duration;
    this.distance = distance;
    this.durationText = durationText;
    this.distanceText = distanceText;
  }

  public long getDuration()
  {
    return duration;
  }

  public void setDuration(long duration)
  {
    this.duration = duration;
  }

  public long getDistance()
  {
    return distance;
  }

  public void setDistance(long distance)
  {
    this.distance = distance;
  }

  public String getDurationText()
  {
    return durationText;
  }

  public void setDurationText(String durationText)
  {
    this.durationText = durationText;
  }

  public String getDistanceText()
  {
    return distanceText;
  }

  public void setDistanceText(String distanceText)
  {
    this.distanceText = distanceText;
  }

  @Override
  public String toString()
  {
    return "DistanceMatrix{" +
      "durationText='" + durationText + '\'' +
      ", distanceText='" + distanceText + '\'' +
      '}';
  }
}
