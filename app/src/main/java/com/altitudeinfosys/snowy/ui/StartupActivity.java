package com.altitudeinfosys.snowy.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.altitudeinfosys.snowy.MyScheduledReceiver;
import com.altitudeinfosys.snowy.SettingsActivity;
import com.firebase.client.ServerValue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import android.Manifest;

import altitudeinfosys.com.snowy.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.altitudeinfosys.snowy.weather.*;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;



public class StartupActivity extends Activity {

    @Nullable
    @BindView(R.id.btnStart)
    ImageButton btnStart;
    @Nullable
    @BindView(R.id.etRefreshRate)
    EditText etRefreshRate;
    @Nullable
    @BindView(R.id.btnPreference)
    Button btnPreference;
    @Nullable
    @BindView(R.id.txtLatitude)
    TextView txtLatitude;
    @Nullable
    @BindView(R.id.txtLongitude)
    TextView txtLongitude;
    @Nullable
    @BindView(R.id.txtLocation)
    TextView txtLocation;
    @Nullable
    @BindView(R.id.txtName)
    TextView txtName;
    @Nullable
    @BindView(R.id.btnExitSnowy)
    Button btnExitSnowy;
    @Nullable
    @BindView(R.id.imageViewLogo)
    ImageView snowyLogo;

    LocationManager locationManager;

    LocationListener locationListener;

    double latitude = 0;
    double longitude = 0;
    String strLocation;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GoogleApiClient mGoogleApiClient;


    public static final String TAG = StartupActivity.class.getSimpleName();

    SharedPreferences sharedPreferences;
    SharedPreferences sp;
    String prefRefreshRate;

    private FirebaseAnalytics mFirebaseAnalytics;

    //firebase auth object
    private FirebaseAuth firebaseAuth;
    FirebaseUser user;

    //firebase database object
    private DatabaseReference mDatabase;


    //boom menu button
    BoomMenuButton bmb;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                Log.i(TAG, "onRequestPermissionResult - get request location updates");

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                logFireBase("onRequestPermissionsResult", "location manage got called, or executed.");

        }

    }


    private void logFireBase(String title, String message)
    {
        Bundle params = new Bundle();
        params.putString("image_name", title);
        params.putString("full_text", message);
        mFirebaseAnalytics.logEvent("snowy_event", params);
    }
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);


        // more code as before
        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setButtonEnum(ButtonEnum.SimpleCircle);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.DOT_3_1);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.SC_3_3);
        bmb.addBuilder(BuilderManager.getSimpleCircleButtonBuilder());

        // log some events with firebase analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefRefreshRate = sharedPreferences.getString("refresh_time", "5");
        etRefreshRate.setText(prefRefreshRate);

        txtName.setText(sharedPreferences.getString("your_name", "Bob"));

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        sp = getSharedPreferences("OURINFO", MODE_PRIVATE);

        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        snowyLogo.startAnimation(animation1);
        snowyLogo.startAnimation(animation2);

        FirebaseCrash.log("Snowy Starting Right NOW  - " + currentDateTimeString);

        Log.i(TAG, " On Create Start ");

      /*  //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
*/

       /* locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

*//*        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);*//*

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {


                String LongLat="";

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.i(TAG, "IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude);

                if (latitude!=0 && longitude!=0) {
                    LongLat = longitude + ";" + latitude;
                    writeSharedPrefLongLat(LongLat);
                }


                handleNewLocation(location);

                try {
                    locationManager.removeUpdates(this);
                } catch (SecurityException e) {
                    Log.e(TAG, "PERMISSION_EXCEPTION "  +  "- PERMISSION_NOT_GRANTED");
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.i(TAG, "IN onStatusChanged, lat=" + latitude + ", lon=" + longitude);

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };

        // If device is running SDK < 23

        if (Build.VERSION.SDK_INT < 23) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
            {
                //do nothing
                Log.i(TAG, "onCreate - get request location updates - cencelled ");
            }
            else {
                Log.i(TAG, "onCreate - get request location updates - executed ");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                handleNewLocation(location);
            }

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // ask for permission

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {

                // we have permission!
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
                {
                    Log.i(TAG, "onCreate - get request location updates - cenceled ");
                }
                else
                {
                    Log.i(TAG, "onCreate - get request location updates - executed ");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    handleNewLocation(location);
                }


            }

        }*/

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();






        //if the user is not logged in
        //that means current user will return null
        if(firebaseAuth.getCurrentUser() == null){
            //closing this activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginActivity.class));
        }

        //getting current user
        user = firebaseAuth.getCurrentUser();



    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }*/


    private void saveLocationInfoToFireBase()
    {
        String longitue=txtLongitude.getText().toString().trim();
        String latitude=txtLatitude.getText().toString().trim();
        String location=txtLocation.getText().toString().trim();

        if (TextUtils.isEmpty(longitue)) {
            Toast.makeText(this, "Longitude is missing - can't save now", Toast.LENGTH_LONG);
            return;
        }

        if (TextUtils.isEmpty(latitude)) {
            Toast.makeText(this, "Latitude is missing - can't save now", Toast.LENGTH_LONG);
            return;
        }

        if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Location is missing - can't save now", Toast.LENGTH_LONG);
            return;
        }

        DatabaseReference dbLocation = FirebaseDatabase.getInstance().getReference("location");

        UserProfile userInformation = new UserProfile(longitue, latitude, location, user.getEmail() );
        dbLocation.child(user.getUid()).setValue(userInformation);
        //dbLocation.setValue(ServerValue.TIMESTAMP);
        Toast.makeText(this, "User Location Info Saved", Toast.LENGTH_LONG).show();


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
        strSummary = sp.getString("LongitudeLatitude","NONE");
        return strSummary;

    }

    @OnClick(R.id.btnPreference)
    public void btnPreferences() {
        Toast.makeText(this, "Button preferences Clicked!", Toast.LENGTH_LONG).show();
        // TODO Auto-generated method stub

        Intent intentSetPref = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(intentSetPref, 0);

        Log.i(TAG, "Start Preferences Activity");


    }
    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ErrorText", "Longitue and Latitude Aren't Available ");
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @OnClick(R.id.btnStart)
    public void btnStart() {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Button Start Clicked!", Toast.LENGTH_LONG).show();


        Intent myIntent = new Intent(getBaseContext(),
                MyScheduledReceiver.class);
        //set for Ausitn, Texas, for the time being
        // in the future, we are going to use automated location detection

        if (latitude!=0 &&longitude !=0) {
            myIntent.putExtra("latitude", latitude);
            myIntent.putExtra("longitude", longitude);
        }
        else
            alertUserAboutError();

        PendingIntent pendingIntent
                = PendingIntent.getBroadcast(getBaseContext(),
                0, myIntent, 0);

        AlarmManager alarmManager
                = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        int minuteIntervals = Integer.parseInt(etRefreshRate.getText().toString());
        calendar.add(Calendar.MINUTE, minuteIntervals);
        long interval = minuteIntervals * 60 * 1000; //
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), interval, pendingIntent);

        Log.i(TAG, "Another call to Scheduled Activity - Alarm Crated ");

        /*******************************************************/
        startWeatherDetailsActivity(longitude, latitude);

        /******************************************************/


        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        prefRefreshRate = sharedPreferences.getString("refresh_time", "5");
        String name = sharedPreferences.getString("your_name", "Bob");
        String viewType = sharedPreferences.getString("view_list", "1");

        etRefreshRate.setText(prefRefreshRate);

        txtName.setText("Hello " + name);


    }

   /* private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
*/
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        111).show();
            }

            return false;
        }

        return true;
    }

    public void startWeatherDetailsActivity(double longitude, double latitude) {
        Intent intent = new Intent(this, RecurringMainActivity.class);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        //intent.putExtra("longitude", -95.072718);
        //intent.putExtra("latitude", 31.650455);
        intent.putExtra("location", strLocation);

        saveLocationInfoToFireBase();

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "***** We are Resuming ********.");

        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        snowyLogo.startAnimation(animation1);
        snowyLogo.startAnimation(animation2);


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                Log.i("**** Location ****", location.toString());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.i(TAG, "On Reume - IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude);
                logFireBase(TAG, "On Reume - IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude);
                handleNewLocation(location);
                try {
                    locationManager.removeUpdates(this);
                    Log.i(TAG, "On Reume - Updates Disabled, lat=" + latitude + ", lon=" + longitude);
                    logFireBase(TAG, "On Reume - Updates Disabled, lat=" + latitude + ", lon=" + longitude);

                } catch (SecurityException e) {
                    Log.e(TAG, "PERMISSION_EXCEPTION "  +  "- PERMISSION_NOT_GRANTED");
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };

        // If device is running SDK < 23

        if (Build.VERSION.SDK_INT < 23) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
            {
                // do nothing
                Log.i(TAG, "on Resume - location polling cancelled");
                logFireBase(TAG,"on Resume - location polling cancelled");
            }
            else {
                Log.i(TAG, "on Resume - location polling received");
                logFireBase(TAG, "on Resume - location polling received");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                handleNewLocation(location);
            }

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // ask for permission

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {

                // we have permission!
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
                {
                    // do nothing
                    Log.i(TAG, "on Resume - location polling cancelled");
                    logFireBase(TAG,  "on Resume - location polling cancelled");
                }
                else {
                    Log.i(TAG, "on Resume - location polling happened");
                    logFireBase(TAG,"on Resume - location polling happende");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    handleNewLocation(location);
                }
            }

        }

        if ( (TextUtils.isEmpty(txtLatitude.getText())) || (TextUtils.isEmpty(txtLongitude.getText())))
        {
            /*DatabaseReference ref = mDatabase.getRef().get
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    //iterating through all the nodes
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //getting artist
                        Artist artist = postSnapshot.getValue(Artist.class);
                        //adding artist to the list
                        artists.add(artist);
                    }

                    //creating adapter
                    ArtistList artistAdapter = new ArtistList(MainActivity.this, artists);
                    //attaching adapter to the listview
                    listViewArtists.setAdapter(artistAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        */
        }
    }

    public void forceLocationDetecion(View view)
    {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                Log.i("**** Location ****", location.toString());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.i(TAG, "On Reume - IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude);
                logFireBase(TAG, "On Reume - IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude);
                handleNewLocation(location);
                try {
                    locationManager.removeUpdates(this);
                    Log.i(TAG, "On Reume - Updates Disabled, lat=" + latitude + ", lon=" + longitude);
                    logFireBase(TAG, "On Reume - Updates Disabled, lat=" + latitude + ", lon=" + longitude);

                } catch (SecurityException e) {
                    Log.e(TAG, "PERMISSION_EXCEPTION "  +  "- PERMISSION_NOT_GRANTED");
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };

        // If device is running SDK < 23

        if (Build.VERSION.SDK_INT < 23) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
            {
                // do nothing
                Log.i(TAG, "on Resume - location polling cancelled");
                logFireBase(TAG,"on Resume - location polling cancelled");
            }
            else {
                Log.i(TAG, "on Resume - location polling received");
                logFireBase(TAG, "on Resume - location polling received");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                handleNewLocation(location);
            }

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // ask for permission

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {

                // we have permission!
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000)
                {
                    // do nothing
                    Log.i(TAG, "on Resume - location polling cancelled");
                    logFireBase(TAG,  "on Resume - location polling cancelled");
                }
                else {
                    Log.i(TAG, "on Resume - location polling happened");
                    logFireBase(TAG,"on Resume - location polling happende");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    handleNewLocation(location);
                }
            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "***** We are Pausing ********.");
    }

    private void handleNewLocation(Location location) {

        if (location != null ) {
            Log.i(TAG, location.toString());

            logFireBase(TAG, "in handle new location - " + location.toString());

            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();

            latitude = currentLatitude;
            longitude = currentLongitude;


            txtLatitude.setText(Double.toString(latitude));
            txtLongitude.setText(Double.toString(longitude));


            Log.i(TAG, " Latitude : " + currentLatitude);
            Log.i(TAG, " Longtiude : " + currentLongitude);


            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(currentLatitude, currentLongitude, 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    Log.i(TAG, "Location name : " + addresses.get(0).getLocality());
                    strLocation = addresses.get(0).getLocality();
                    txtLocation.setText(strLocation);
                    logFireBase(TAG, " in handleNewLocation - current location is " + strLocation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i(TAG, "Inside handleNewLocation - Location is null ");

        }

    }


    @OnClick(R.id.btnExitSnowy)
    public void btnExitSnowy(){
        Log.i(TAG, "We are exiting Snowy - Begin");
        Intent intent = new Intent(this, MyScheduledReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i(TAG, "We are exiting Snowy - End");
        logFireBase(TAG, "on btnExitSnowy - we are exiting snowy ");
        goBackToHome();


    }

    private void goBackToHome()
    {
        //finish();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void logout(View view)
    {
        //logging out the user
        firebaseAuth.signOut();
        //closing activity
        finish();
        //starting login activity
        startActivity(new Intent(this, LoginActivity.class));
    }


}