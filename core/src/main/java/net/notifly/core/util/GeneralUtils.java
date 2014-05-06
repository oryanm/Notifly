package net.notifly.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Parcel;
import android.preference.PreferenceManager;

import com.google.common.collect.HashBiMap;

import net.notifly.core.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeneralUtils {
    public static HashBiMap<String, Locale> countryToLocaleMap = HashBiMap.create();

    public static void initCountryToLocaleMap(Context context) {
        countryToLocaleMap.put(context.getString(R.string.israel), new Locale("he", "IL"));
        countryToLocaleMap.put(context.getString(R.string.usa), new Locale("en", "US"));

        initLocaleByPreference(context);
    }

    private static void initLocaleByPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String country = preferences.getString(context.getString(R.string.curr_location_preference_key), null);
        if (country != null)
        {
            Locale.setDefault(countryToLocaleMap.get(country));
        }
    }

    public static <T> T getOrDefault(T object, T def) {
        return object != null ? object : def;
    }

    public static String join(String delimiter, String... strings) {
        StringBuilder builder = new StringBuilder();
        String del = "";

        for (String item : strings) {
            builder.append(del);
            builder.append(item);
            del = delimiter;
        }

        return builder.toString();
    }

    public static String toString(Address address) {
        List<String> builder = new ArrayList<String>();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            builder.add(address.getAddressLine(i));
        }

        return GeneralUtils.join(", ", builder.toArray(new String[builder.size()]));
    }

    public static void writeBoolean(Parcel parcel, boolean b) {
        parcel.writeByte((byte) (b ? 1 : 0));
    }

    public static boolean readBoolean(Parcel parcel) {
        return parcel.readByte() != 0;
    }
}
