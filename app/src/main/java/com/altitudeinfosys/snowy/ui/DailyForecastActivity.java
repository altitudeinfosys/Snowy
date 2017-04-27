package com.altitudeinfosys.snowy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.widget.Toast;

import com.altitudeinfosys.snowy.adapters.DayAdapter;
import com.altitudeinfosys.snowy.adapters.HourAdapter;
import com.altitudeinfosys.snowy.weather.Day;
import com.altitudeinfosys.snowy.weather.Hour;

import java.util.Arrays;

import altitudeinfosys.com.snowy.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Tarek on 7/8/2016.
 */
public class DailyForecastActivity extends ActionBarActivity{

    private Day[] mDays;


    float x1,x2;
    float y1, y2;

    @Nullable @BindView(R.id.recyclerViewDaily) RecyclerView mRecycleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelable = intent.getParcelableArrayExtra(RecurringMainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelable,parcelable.length,Day[].class);

        DayAdapter adapter = new DayAdapter(mDays);
        mRecycleView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(layoutManager);

        mRecycleView.setHasFixedSize(true);


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


                }

                // if right to left sweep event on screen
                if (x1 > x2)
                {
                    Toast.makeText(this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                    finish();

                }

                // if UP to Down sweep event on screen
                if (y1 < y2)
                {
                    Toast.makeText(this, "UP to Down Swap Performed", Toast.LENGTH_LONG).show();
                }

                //if Down to UP sweep event on screen
                if (y1 > y2)
                {
                    Toast.makeText(this, "Down to UP Swap Performed", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        return false;
    }




}
