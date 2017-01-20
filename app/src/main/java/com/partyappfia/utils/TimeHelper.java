package com.partyappfia.utils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Great Summit on 3/16/2016.
 */
public class TimeHelper {

    public final static String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public final static String NOT_YET = "not yet";

    public static String getTimeStamp() {
        return getTimeStamp(new Date());
    }

    public static String getTimeStamp(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String getDiffTime(String basicTime) {
        if (basicTime == null)
            return NOT_YET;

        if (basicTime.isEmpty())
            return NOT_YET;

        //time set
        SimpleDateFormat sf = new SimpleDateFormat(ISO8601DATEFORMAT);
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startday = sf.parse(basicTime, new ParsePosition(0));
        if (startday == null)
            return NOT_YET;

        long startTime = startday.getTime();

        //current time set
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        long endTime = endDate.getTime();

        int diffTime = (int)(endTime - startTime) / 1000;   // seconds

        diffTime = diffTime / 60;   // minutes
        if (diffTime == 0)
            return "just now";

        if (diffTime / 60 == 0) {
            if (diffTime == 1)
                return "a minute ago";
            else
                return String.format("%d minutes ago", diffTime);
        }

        diffTime = diffTime / 60;   // hours
        if (diffTime / 24 == 0) {
            if (diffTime == 1)
                return "a hour ago";
            else
                return String.format("%d hours ago", diffTime);
        }

        diffTime = diffTime / 24;   // days
        if (diffTime / 7 == 0) {
            if (diffTime == 1)
                return "a day ago";
            else
                return String.format("%d days ago", diffTime);
        } else if (diffTime / 7 == 1) {
            return "a week ago";
        } else if (diffTime / 7 == 2) {
            return "2 weeks ago";
        } else if (diffTime / 7 == 3) {
            return "2 weeks ago";
        }

        diffTime = diffTime / 30;   // months
        if (diffTime / 12 == 0) {
            if (diffTime == 1)
                return "a month ago";
            else
                return String.format("%d months ago", diffTime);
        }

        diffTime = diffTime / 12;   // year
        if (diffTime == 1)
            return "a year ago";
        else
            return String.format("%d years ago", diffTime);
    }
}
