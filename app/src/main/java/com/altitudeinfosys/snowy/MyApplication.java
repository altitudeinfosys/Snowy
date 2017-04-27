package com.altitudeinfosys.snowy;


/*import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;*/
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.firebase.client.Firebase;

import java.util.HashMap;

import altitudeinfosys.com.snowy.R;


public class MyApplication extends Application
{
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

        super.onCreate();
        Firebase.setAndroidContext(this);


        //acra mailer -- disabled because it seems like acra doesn't work well with proguard
        /*ACRA.init(this);
        super.onCreate();
        HashMap<String,String> ACRAData = new HashMap<String,String>();
        ACRAData.put("my_app_info", "custom data");
        ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));*/

        //attach the default settings from the preferences xml file
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);


    }



    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = true;
    }

    private static boolean activityVisible;
}