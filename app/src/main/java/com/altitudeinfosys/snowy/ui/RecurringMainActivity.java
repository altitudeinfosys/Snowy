package com.altitudeinfosys.snowy.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.altitudeinfosys.snowy.misc.Utility;
import com.altitudeinfosys.snowy.weather.Alert;
import com.altitudeinfosys.snowy.weather.Current;
import com.altitudeinfosys.snowy.weather.Day;
import com.altitudeinfosys.snowy.weather.Forecast;
import com.altitudeinfosys.snowy.weather.Hour;
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

import altitudeinfosys.com.snowy.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import android.speech.tts.TextToSpeech;


public class RecurringMainActivity extends Activity {

    public static final String TAG = RecurringMainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    //private Current mCurrent;
    private Forecast mForcast;

     @Nullable @BindView(R.id.timeLabel) TextView mTimeLabel;
     @Nullable @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
     @Nullable @BindView(R.id.humidityValue) TextView mHumidityValue;
     @Nullable @BindView(R.id.precipValue) TextView mPrecipValue;
     @Nullable @BindView(R.id.summaryLabel) TextView mSummaryLabel;
     @Nullable @BindView(R.id.iconImageView) ImageView mIconImageView;
     @Nullable @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
     @Nullable @BindView(R.id.progressBar) ProgressBar mProgressBar;
     @Nullable @BindView(R.id.locationLabel) TextView mLocationLabel;
     @Nullable @BindView(R.id.dailyButton) Button mDailyButton;
     @Nullable @BindView(R.id.hourlyButton) Button mHourlyButton;
     @Nullable @BindView(R.id.backButton) Button mBackToSettings;



    String strLocation = "Los Angeles, CA";

    boolean bolAlternate = false;

    SharedPreferences sharedPreferences; //to save app preferences

    SharedPreferences sp;

    String viewStyle;

    private Speaker speaker;

    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;

    private int speakPauseTime = 0;

    double latitude=0;
    double longitude=0;

    boolean speakItNowRegardless = false;

    boolean bolScreenOn = false;

    float x1,x2;
    float y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("OURINFO", MODE_PRIVATE);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        viewStyle = sharedPreferences.getString("view_list", "1");

        speakPauseTime = Integer.parseInt(sharedPreferences.getString("speak_refresh_time","60"));

        bolScreenOn = sharedPreferences.getBoolean("screenOn",true);


        if (viewStyle.compareTo("1")==0)
            setContentView(R.layout.brief_layout);
        else if (viewStyle.compareTo("2")==0)
            setContentView(R.layout.activity_recurring_main);
        else if (viewStyle.compareTo("3")==0)
        {
            bolAlternate = sp.getBoolean("alternate", true);

            if (bolAlternate)
                setContentView(R.layout.brief_layout);
            else
                setContentView(R.layout.activity_recurring_main);

            bolAlternate = !bolAlternate;
            SharedPreferences.Editor ed = sp.edit();
            ed.putBoolean("alternate", bolAlternate);
            ed.commit();

        }
        else
            setContentView(R.layout.activity_recurring_main);

        ButterKnife.bind(this);

        if (bolScreenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mProgressBar.setVisibility(View.INVISIBLE);

        //final double latitude = 30.2669444;    //37.8267;
        //final double longitude = -97.7427778; //-122.423;

        /*final double latitude =getIntent().getDoubleExtra("latitude",0);
        final double longitude = getIntent().getDoubleExtra("longitude", 0);*/

        latitude =getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        strLocation = getIntent().getStringExtra("location");


        // to store the last location name in the shared preferences storage

        if ((strLocation != null ) && (!strLocation.isEmpty()))
        {
            // Store our shared preference

            SharedPreferences.Editor ed = sp.edit();
            ed.putString("OurLocation", strLocation);
            ed.commit();
        }
        else // check if their is a location stored in our shared preferences
        {
            sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
            strLocation = sp.getString("OurLocation", "Austin, Texas");
        }


        Log.i(TAG, "Latitude:" + latitude + ", Longitude:" + longitude + " , Location : " + strLocation);



        /*mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(latitude, longitude);
            }
        });*/

        if (sharedPreferences.getBoolean("speakTemperature",false)) {
            checkTTS();
        }



        getForecast(latitude, longitude);



        Log.i(TAG, "Main UI code is running!");
    } // end onCreate

    @OnClick(R.id.refreshImageView)
    public void refreshImageView()
    {
        getForecast(latitude, longitude);
        speakItNowRegardless = true;

    }

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                speaker = new Speaker(this);
            }else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }


    private void getForecast(double latitude, double longitude) {
        String apiKey = "27974c4bc33201748eaf542a6769c3b7";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        //TODO: we should change the forecastUrl instead of string change to uriBuilder

        Log.i(TAG, "Forcast URL : " + forecastUrl);

        if (isNetworkAvailable()) {
            toggleRefresh();

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
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
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
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
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

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForcast.getCurrent();

        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        if (mHumidityValue != null)
            mHumidityValue.setText(current.getHumidity() + "");
        if (mPrecipValue != null)
            mPrecipValue.setText(current.getPrecipChance() + "%");
        if (mSummaryLabel != null)
            mSummaryLabel.setText(current.getSummary());
        if (mLocationLabel != null)
            mLocationLabel.setText(strLocation);

        Drawable drawable = getResources().getDrawable(current.getIconId());
        if (mIconImageView!=null)
            mIconImageView.setImageDrawable(drawable);


        if (!speakItNowRegardless) {
            // start talking
            speakItIfPossible(current);
        }
        else
            speakItIfForSure(current);

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

       if ((mForcast.getAlert()!=null) && (bolShowNotification))
       {

           // prepare intent which is triggered if the
           // notification is selected

           Log.i(TAG,"I am about to add a notification check the top.");

           addNotification();


       }

    }

    private void addNotification() {

        Log.i(TAG, "notification start");

        Current current = mForcast.getCurrent();
        Alert alert = mForcast.getAlert()[0];

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Weather Alert")
                        .setContentText(" It is " + current.getFormattedTime() + " and the weather alert issued, and the alert is  " + alert.getDescription());


        Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.putExtra("NotificationText", alert.getDescription());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }


    private void speakItIfPossible(Current current)
    {
        //calculate time difference

        boolean timePassed = false;
        if (Utility.timeDiffernce(current.getTime(), sp.getLong("LastSpeakTime",0)) >= speakPauseTime)
            timePassed = true;


        if (sharedPreferences.getBoolean("speakTemperature",false) && timePassed) {
            if (speaker !=null) {
                speaker.allow(true);
                if (sharedPreferences.getBoolean("speakName", false))
                    speaker.speak(sharedPreferences.getString("your_name", "") + " It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now");
                else
                    speaker.speak("It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now");
                writeSharedPref(current.getTime());
            }
        }
    }

    private void speakSummaryChange(Current current, String oldSummary, String newSummary)
    {



        if (sharedPreferences.getBoolean("speakTemperature",false) ) {
            if (speaker !=null) {
                speaker.allow(true);
                if (sharedPreferences.getBoolean("speakName", false))
                    speaker.speak(sharedPreferences.getString("your_name", "") + " It is " + current.getFormattedTime() + " and the weather summary has changed from " + oldSummary + " to " + newSummary);
                else
                    speaker.speak(" It is " + current.getFormattedTime() + " and the weather summary has changed from " + oldSummary + " to " + newSummary);
                writeSharedPref(current.getTime());
            }
        }
    }

    private void speakAlerts(Current current)
    {

        Log.i(TAG, "Current Time, Last Alert Speak Time, Pause Time and Time Differeence -- > " + current.getTime() + " ,"  + sp.getLong("LastAlertSpeakTime",0) + " " + speakPauseTime + " and " + Utility.timeDiffernce(current.getTime(), sp.getLong("LastAlertSpeakTime",0)) );

        boolean timePassed = false;
        if (Utility.timeDiffernce(current.getTime(), sp.getLong("LastAlertSpeakTime",0)) >= speakPauseTime)
            timePassed = true;


        if (timePassed) {
            Alert alert = new Alert();
            if (mForcast.getAlert().length > 0)
                alert = mForcast.getAlert()[0];


            if (sharedPreferences.getBoolean("speakAlerts", false)) {
                if (speaker != null) {
                    speaker.allow(true);
                    if (sharedPreferences.getBoolean("speakName", false)) {
                        speaker.speak(sharedPreferences.getString("your_name", "") + " A weather Alert issued at " + alert.getFormattedTime(current.getTimeZone()) + " the weather alert say " + alert.getDescription() + " and the current summary is   " + current.getSummary());
                    } else
                        speaker.speak(" It is " + current.getFormattedTime() + " and the weather alert issued, and the alert is  " + alert.getDescription());
                    writeLastAlertSpeakTimeSharedPref(current.getTime());
                }
            }
        }
    }




    private void speakItIfForSure(Current current)
    {

        if (sharedPreferences.getBoolean("speakTemperature",false) ) {
            speaker.allow(true);
            if (sharedPreferences.getBoolean("speakName",false))
                speaker.speak(sharedPreferences.getString("your_name","") + " It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now");
            else
                speaker.speak("It is " + current.getFormattedTime() + " and the temperature is " + current.getTemperature() + "Right Now");

        }

        speakItNowRegardless = false;
    }


    private Forecast parseForecastDetails(String jsonData)throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
         // TODO: will take care of that later
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        forecast.setAlert(getAlertDetailsIfAny(jsonData));
        return forecast;
    }

    private Hour[] getHourlyForecast(String jsonData)throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        Log.i(TAG, "From JSON: " + timezone);

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for(int i=0;i<data.length();++i)
        {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setTime(jsonHour.getLong("time"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setTimezone(timezone);

            hours[i] = hour;

        }
        return hours;
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

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.i(TAG, current.getFormattedTime());

        return current;
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

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ErrorText", "Can't load Weather Data");
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @Override
    protected void onDestroy() {


        //Close the Text to Speech Library
        if(speaker != null) {

            speaker.destroy();
            Log.d(TAG, "TTS Destroyed");
        }
        Process.killProcess(Process.myPid());
        super.onDestroy();


    }


    private void writeSharedPref(long t)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong("LastSpeakTime", t);
        ed.commit();
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

    private void writeLastAlertSpeakTimeSharedPref(long t)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong("LastAlertSpeakTime", t);
        ed.commit();
    }

    @Optional @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view)
    {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForcast.getDailyForecast());
        startActivity(intent);
    }

    @Optional
    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view)
    {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForcast.getHourlyForecast());
        startActivity(intent);
    }

    // onTouchEvent () method gets called when User performs any touch event on screen
    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent)
    {
        switch (touchevent.getAction())
        {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                //if left to right sweep event on screen
                if (x1 < x2)
                {
                    Toast.makeText(this, "Left to Right Swap Performed", Toast.LENGTH_LONG).show();

                    /*Intent intent = new Intent(this, HourlyForecastActivity.class);
                    intent.putExtra(HOURLY_FORECAST, mForcast.getHourlyForecast());
                    startActivity(intent);*/
                }

                // if right to left sweep event on screen
                if (x1 > x2)
                {
                    Toast.makeText(this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();

                    /*Intent intent = new Intent(this, DailyForecastActivity.class);
                    intent.putExtra(DAILY_FORECAST, mForcast.getDailyForecast());
                    startActivity(intent);*/
                }

                // if UP to Down sweep event on screen
                if (y1 < y2)
                {
                    //startMainStartupActivity();
                    Toast.makeText(this, "UP to Down Swap Performed", Toast.LENGTH_LONG).show();
                }

                //if Down to UP sweep event on screen
                if (y1 > y2)
                {
                    //startMainStartupActivity();
                    Toast.makeText(this, "Down to UP Swap Performed", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        return false;
    }

    @Optional
    @OnClick(R.id.backButton)
    public void backButton(View view)
    {
        startMainStartupActivity();
    }

    public void startMainStartupActivity()
    {
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

}














