package com.altitudeinfosys.snowy.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Tarek on 6/17/2016.
 */
public class Alert {
    private long mExpire;
    private String mTitle;
    private String mDescription;
    private String mUri;

    public long getExpire() {
        return mExpire;
    }

    public void setExpire(long mExpire) {
        this.mExpire = mExpire;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String mUri) {
        this.mUri = mUri;
    }

    public String getFormattedTime(String timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        Date dateTime = new Date(getExpire() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }

}
