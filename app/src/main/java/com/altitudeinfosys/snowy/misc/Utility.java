package com.altitudeinfosys.snowy.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Tarek on 8/3/2015.
 */
public final class Utility {

    public static long timeDiffernce (String t1, String t2)
    {
        // Custom date format
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(t1);
            d2 = format.parse(t2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get msec from each, and subtract.
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        System.out.println("Time in seconds: " + diffSeconds + " seconds.");
        System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        System.out.println("Time in hours: " + diffHours + " hours.");

        return diffMinutes;
    }

    public static long timeDiffernce (long t1, long t2)
    {
        // Custom date format
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(getFormattedTime(t1));
            d2 = format.parse(getFormattedTime(t2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get msec from each, and subtract.
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = Math.abs(diff / (60 * 1000));
        long diffHours = diff / (60 * 60 * 1000);
        System.out.println("Time in seconds: " + diffSeconds + " seconds.");
        System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        System.out.println("Time in hours: " + diffHours + " hours.");

        return diffMinutes;
    }
    public static String getFormattedTime(long t) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
        Date dateTime = new Date(t * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }



}
