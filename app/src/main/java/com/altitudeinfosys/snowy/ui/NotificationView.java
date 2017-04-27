package com.altitudeinfosys.snowy.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import altitudeinfosys.com.snowy.R;

public class NotificationView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.notification);


        String strLocation = getIntent().getStringExtra("NotificationText");

        TextView tvNotificationText = (TextView)findViewById(R.id.notificationText);

        tvNotificationText.setText(strLocation);


    }

    public void close(View view)
    {
        //startStartupActivity();
        this.finish();
    }

    public void startStartupActivity() {
        Intent intent = new Intent(this, StartupActivity.class);

        startActivityForResult(intent, 0);
    }
}
