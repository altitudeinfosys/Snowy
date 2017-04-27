package com.altitudeinfosys.snowy.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by benjakuben on 2/5/15.
 */
public class Day implements Parcelable {
    private long mTime;
    private String mSummary;
    private double mTemperatureMin;
    private double mTemperatureMax;
    private String mTemperatureSummary;
    private String mIcon;
    private String mTimezone;

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperatureMin() {
        return (int) Math.round(mTemperatureMin);
    }

    public void setTemperatureMin(double temperatureMin) {
        mTemperatureMin = temperatureMin;
    }


    public int getTemperatureMax() {
        return (int) Math.round(mTemperatureMax);
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }

    public String getTemperatureSummary() {return mTemperatureSummary;}

    public void setTemperatureSummary()
    {
        mTemperatureSummary = Integer.toString(getTemperatureMax())
         + " / " + Integer.toString(getTemperatureMin());
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public int getIconId(){
        return Forecast.getIconId(mIcon);
    }

    public String getDayOfTheWeek(){
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    @Override
    public int describeContents() {

        return 0;                   // We won't be using this
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeString(mTemperatureSummary);
        dest.writeString(mIcon);
        dest.writeString(mTimezone);

    }

    private Day(Parcel in)
    {
        mTime = in.readLong();
        mSummary = in.readString();
        mTemperatureSummary = in.readString();
        mIcon = in.readString();
        mTimezone = in.readString();
    }

    public Day(){

    }

    public static final Parcelable.Creator<Day> CREATOR = new Parcelable.Creator<Day>() {
        public Day createFromParcel(Parcel in) {
            return new Day(in);
        }

        public Day[] newArray(int size) {
            return new Day[size];
        }
    };



}
