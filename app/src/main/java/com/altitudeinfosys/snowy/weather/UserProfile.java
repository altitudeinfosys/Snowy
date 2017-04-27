package com.altitudeinfosys.snowy.weather;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tarek on 3/1/2017.
 */

public class UserProfile {
    private String longitude;
    private String latitude;
    private String town;
    private String email;
    private String updateDate;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();

    public UserProfile(String longitude, String latitude, String town, String email)
    {
        this.longitude = longitude;
        this.latitude = latitude;
        this.town = town;
        this.email = email;
        updateDate =dateFormat.format(date);

    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
