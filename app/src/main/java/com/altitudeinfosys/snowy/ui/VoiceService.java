package com.altitudeinfosys.snowy.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.altitudeinfosys.snowy.misc.Utility;
import com.altitudeinfosys.snowy.weather.Alert;
import com.altitudeinfosys.snowy.weather.Current;
import com.altitudeinfosys.snowy.weather.Day;
import com.altitudeinfosys.snowy.weather.Forecast;
import com.altitudeinfosys.snowy.weather.Speaker;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import altitudeinfosys.com.snowy.R;

import static altitudeinfosys.com.snowy.R.id.notificationText;
import static android.R.attr.data;
//import static com.google.android.gms.internal.zzhl.runOnUiThread;

import android.os.Handler;

/**
 * Created by Tarek on 10/17/2016.
 */

public class VoiceService extends Service  {


    Handler handler;
    public static final String TAG = VoiceService.class.getSimpleName();

    private Speaker speaker;
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;

    //private Current mCurrent;
    private Forecast mForcast;

    double latitude=0;
    double longitude=0;

    String strLocation = "Los Angeles, CA";

    SharedPreferences sharedPreferences; //to save app preferences

    SharedPreferences sp;

    private int speakPauseTime = 0;


    @Override
    public void onCreate() {
        speaker = new Speaker(this);

        handler = new Handler();

        sp = getSharedPreferences("OURINFO", MODE_PRIVATE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        speakPauseTime = Integer.parseInt(sharedPreferences.getString("speak_refresh_time","60"));

        // This is a good place to set spokenText
        Log.i(TAG,"I am inside the onCreate voice service");
    }
    private void writeSharedPrefLongLat(String LongLat)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("LongitudeLatitude", LongLat);
        ed.commit();
    }

    private String getSharedPrefSummaryLongLat()
    {
        String strSummary = "";
        sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        strSummary = sp.getString("LongitudeLatitude","");
        return strSummary;

    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.i(TAG, "I am inside the onStartCommand voice service");

        try {
            latitude = (double) intent.getExtras().get("latitude");
            longitude = (double) intent.getExtras().get("longitude");
        }
        catch (Exception e)
        {

            Log.i(TAG, "I am inside the onStartCommand voice service, but exception happened, we need to handle it");
            if (latitude == 0 || longitude == 0)
            {
                String latlong = getSharedPrefSummaryLongLat();
                if (!(latlong.isEmpty()))
                {
                    String[] temp = latlong.split(";");
                    latitude = Double.parseDouble(temp[0]);
                    longitude = Double.parseDouble(temp[1]);

                }
            }

        }
        finally {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            getForecast(latitude, longitude);
            return START_STICKY;

        }





        //return START_STICKY;

    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "27974c4bc33201748eaf542a6769c3b7";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        //TODO: we should change the forecastUrl instead of string change to uriBuilder

        Log.i(TAG, "Forcast URL : " + forecastUrl);

        if (isNetworkAvailable()) {


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();
                        }
                    });
                    Log.i(TAG, "Error 1 fetching info from voice service");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForcast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    speakIt();
                                }
                            });
                        } else {
                            Log.i(TAG, "Error 2 fetching info from voice service");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
        }
    }

    private Forecast parseForecastDetails(String jsonData)throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        forecast.setAlert(getAlertDetailsIfAny(jsonData));
        return forecast;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.i(TAG, current.getFormattedTime());

        return current;
    }

    private Day[] getDailyForecast(String jsonData)throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        Log.i(TAG, "From JSON: " + timezone);

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for(int i=0;i<data.length();++i)
        {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setTime(jsonDay.getLong("time"));
            day.setIcon(jsonDay.getString("icon"));
            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMin(jsonDay.getDouble("temperatureMin"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTemperatureSummary();
            day.setTimezone(timezone);

            days[i] = day;

        }
        return days;
    }

    private Alert[] getAlertDetailsIfAny(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);


        String timezone = forecast.getString("timezone");

        Log.i(TAG, "From JSON: " + timezone);

        if ((forecast.isNull("alerts")))
            return null;

        //JSONObject alertObject = forecast.getJSONObject("alerts");
        JSONArray alertArray = forecast.getJSONArray("alerts");
        Alert[] alerts = new Alert[alertArray.length()];

        for(int i=0;i<alertArray.length();++i)
        {
            JSONObject jsonAlert = alertArray.getJSONObject(i);

            Alert alert = new Alert();

            alert.setTitle(jsonAlert.getString("title"));
            alert.setDescription(jsonAlert.getString("description"));
            alert.setExpire(jsonAlert.getLong("expires"));
            alert.setUri(jsonAlert.getString("uri"));
            Log.i(TAG, alert.getFormattedTime(timezone));

            alerts[i] = alert;

        }



        return alerts;
    }


    private void speakItIfPossible(Current current)
    {
        //calculate time difference

        Log.i(TAG, "Current Time, Last Speak Time, Pause Time and Time Differeence -- > " + current.getTime() + " ,"  + sp.getLong("LastSpeakTime",0) + " " + speakPauseTime + " and " + Utility.timeDiffernce(current.getTime(), sp.getLong("LastSpeakTime",0)) );
        String speakerText="";

        if (sharedPreferences.getBoolean("speakName", false))
            speakerText = sharedPreferences.getString("your_name", "") + " It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now";
        else
            speakerText="It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now";

        boolean timePassed = false;
        if (Utility.timeDiffernce(current.getTime(), sp.getLong("LastSpeakTime",0)) >= speakPauseTime)
            timePassed = true;


        if (sharedPreferences.getBoolean("speakTemperature",false) && timePassed) {
            if (speaker !=null) {
                speaker.allow(true);
                speaker.speak(speakerText);
                writeSharedPref(current.getTime());
            }
        }

        addNotification("Current Weather", speakerText);
    }


    private void speakItIfForSure(Current current)
    {

        String strMessage = "";


        if (sharedPreferences.getBoolean("speakTemperature",false) ) {
            if (speaker !=null) {
                speaker.allow(true);
                if (sharedPreferences.getBoolean("speakName", false)){
                    strMessage = sharedPreferences.getString("your_name", "") + " It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now";
                    speaker.speak(strMessage);}
                else{
                    strMessage = "It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now";
                    speaker.speak(strMessage);
                }
            }
        }

        addNotification("Current Weather Situation", strMessage);

    }

    protected void speakIt()
    {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Log.i(TAG, "Inside Speak it in voice activity");

        Current current = mForcast.getCurrent();

        speakItIfPossible(current);

        /*******************************************************/
        // compare summary and see if any changes happen
        String lastSummary = getSharedPrefSummary();
        String currentSummary = current.getSummary();
        writeSharedPrefSummary(currentSummary);
        boolean bolSpeakSummaryChange = sharedPreferences.getBoolean("speakSummary",false);
        boolean bolSpeakAlerts = sharedPreferences.getBoolean("speakAlerts",false);
        boolean bolShowNotification = sharedPreferences.getBoolean("AlertNotification",false);


        Log.i(TAG, "Last Summary --> " + lastSummary + " and here is current summary --> " + currentSummary);

        if ((lastSummary.compareToIgnoreCase(currentSummary)!=0) && (bolSpeakSummaryChange))
        {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
                speakSummaryChange(current, lastSummary, current.getSummary());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

       if ((mForcast.getAlert()!=null) && (bolSpeakAlerts))
        {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
                speakAlerts(current);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       /* if ((mForcast.getAlert()!=null) && (bolShowNotification))
        {

            // prepare intent which is triggered if the
            // notification is selected

            Log.i(TAG,"I am about to add a notification check the top.");

            String strTemp = " It is " + current.getFormattedTime() + " and the weather alert issued, and the alert is  " + mForcast.getAlert()[0].getDescription();
            addNotification("New Weather Alert Issued", strTemp);


        }*/

    }

    private void addNotification(String subjectText, String notificationText) {

        Log.i(TAG, "notification start");



        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(subjectText)
                        .setContentText(notificationText);


        Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.putExtra("NotificationText", notificationText);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void speakAlerts(Current current)
    {

        Log.i(TAG, "Current Time, Last Alert Speak Time, Pause Time and Time Differeence -- > " + current.getTime() + " ,"  + sp.getLong("LastAlertSpeakTime",0) + " " + speakPauseTime + " and " + Utility.timeDiffernce(current.getTime(), sp.getLong("LastAlertSpeakTime",0)) );

        String spokenText ="";
        Alert alert = new Alert();
        if (mForcast.getAlert().length > 0)
            alert = mForcast.getAlert()[0];

        boolean timePassed = false;
        if (Utility.timeDiffernce(current.getTime(), sp.getLong("LastAlertSpeakTime",0)) >= speakPauseTime)
            timePassed = true;

        if (sharedPreferences.getBoolean("speakName", false)) {
            spokenText = sharedPreferences.getString("your_name", "") + " A weather Alert issued at " + alert.getFormattedTime(current.getTimeZone()) + " the weather alert say " + alert.getDescription() + " and the current summary is   " + current.getSummary();
        } else {
            spokenText = " It is " + current.getFormattedTime() + " and the weather alert issued, and the alert is  " + alert.getDescription();
        }

        if (timePassed) {

            if (sharedPreferences.getBoolean("speakAlerts", false)) {
                if (speaker != null) {
                        speaker.allow(true);
                        speaker.speak(spokenText);
                        writeLastAlertSpeakTimeSharedPref(current.getTime());
                }
            }
        }

        boolean bolShowNotification = sharedPreferences.getBoolean("AlertNotification",false);
        if ((bolShowNotification) && (!spokenText.isEmpty()))
            addNotification("New Weather Alert issued", spokenText);
    }




    private void speakSummaryChange(Current current, String oldSummary, String newSummary)
    {

        String spokenText="";

        boolean timePassed = false;
        if (Utility.timeDiffernce(current.getTime(), sp.getLong("LastSummarySpeakTime",0)) >= speakPauseTime)
            timePassed = true;


        if (sharedPreferences.getBoolean("speakName", false)) {
            spokenText = sharedPreferences.getString("your_name", "") + " It is " + current.getFormattedTime() + " and the weather summary has changed from " + oldSummary + " to " + newSummary;
        }
        else {
            spokenText = " It is " + current.getFormattedTime() + " and the weather summary has changed from " + oldSummary + " to " + newSummary;
        }

        if (sharedPreferences.getBoolean("speakTemperature",false) ) {
            if (speaker !=null) {
                speaker.allow(true);
                speaker.speak(spokenText);
                writeSummaryChangeSharedPref(current.getTime());
            }

        }
        boolean bolShowNotification = sharedPreferences.getBoolean("AlertNotification",false);

        if ((bolShowNotification) && (!spokenText.isEmpty()))
            addNotification("Weather Summary Change" , spokenText);
    }

    private void writeSharedPrefSummary(String summary)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("Summary", summary);
        ed.commit();
    }

    private String getSharedPrefSummary()
    {
        String strSummary = "";
        sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        strSummary = sp.getString("Summary","NONE");
        return strSummary;

    }


    private void writeSummaryChangeSharedPref(Long t)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong("LastSummarySpeakTime", t);
        ed.commit();

    }

    private void writeSharedPref(long t)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong("LastSpeakTime", t);
        ed.commit();
    }

    private void writeLastAlertSpeakTimeSharedPref(long t)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong("LastAlertSpeakTime", t);
        ed.commit();
    }

    @Override
    public void onDestroy() {


        //Close the Text to Speech Library
        if(speaker != null) {

            speaker.destroy();
            Log.d(TAG, "TTS Destroyed");
        }
        Process.killProcess(Process.myPid());
        super.onDestroy();


    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

}