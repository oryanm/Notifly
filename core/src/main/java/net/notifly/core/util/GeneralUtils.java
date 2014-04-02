package net.notifly.core.util;

import android.location.Address;
import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

public class GeneralUtils {
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
