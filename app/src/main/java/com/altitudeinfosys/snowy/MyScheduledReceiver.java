package com.altitudeinfosys.snowy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.altitudeinfosys.snowy.ui.RecurringMainActivity;
import com.altitudeinfosys.snowy.ui.VoiceService;

public class MyScheduledReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences; //to save app preferences

    public static final String TAG = MyScheduledReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //if (isConnected) {
            Log.i(TAG, "we are connected and online" + isConnected);

            //Toast.makeText(this, "WE are online", Toast.LENGTH_LONG).show();
            if (sharedPreferences.getBoolean("VoiceReportingOnly",false) ) {
                Log.i(TAG, "Inside the Receiver class - calling the voice activity now");



                Intent serviceIntent = new Intent(context,VoiceService.class);
                serviceIntent.putExtra("longitude", intent.getDoubleExtra("longitude", 0));
                serviceIntent.putExtra("latitude", intent.getDoubleExtra("latitude", 0));
                context.startService(serviceIntent);
            }
            else
            {
                Log.i(TAG, "Inside the Receiver class - calling the recurring activity now");
                Intent scheduledIntent = new Intent(context, RecurringMainActivity.class);
                scheduledIntent.putExtra("longitude", intent.getDoubleExtra("longitude", 0));
                scheduledIntent.putExtra("latitude", intent.getDoubleExtra("latitude", 0));
                scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(scheduledIntent);
            }
        //}
        //else Log.i(TAG, "we are not connected" +isConnected);



    }

}