package net.notifly.core.util;

import android.location.Address;

public class GeneralUtils
{
  public static <T> T getOrDefault(T object, T def)
  {
     return object != null ? object : def;
  }

  public  static String join(String delimiter, String... strings) {
    StringBuilder builder = new StringBuilder();
    String del = "";

    for (String item : strings)
    {
      builder.append(del);
      builder.append(item);
      del = delimiter;
    }

    return builder.toString();
  }

  public static String toString(Address address)
  {
    return String.format(
      "%s, %s, %s",
      // If there's a street address, add it
      address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
      // Locality is usually a city
      address.getLocality(),
      // The country of the address
      address.getCountryName());
  }
}
