package com.nexuspulse.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static String getTimeAgo(Date date) {
        if (date == null || date.getTime() < 100000) {
            return "";  // Returns empty string for null / epoch dates
        }

        long time = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + "m";
        } else if (hours < 24) {
            return hours + "h";
        } else if (days < 7) {
            return days + "d";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(date);
        }
    }

    // ⭐ OVERLOAD: Accept long timestamp directly
    public static String getTimeAgo(long timestamp) {
        return getTimeAgo(new Date(timestamp));
    }

    public static String getFormattedDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    // ⭐ OVERLOAD: Accept long timestamp directly
    public static String getFormattedDate(long timestamp) {
        return getFormattedDate(new Date(timestamp));
    }
}
