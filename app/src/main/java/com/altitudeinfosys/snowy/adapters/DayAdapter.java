package com.altitudeinfosys.snowy.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.altitudeinfosys.snowy.weather.Day;
import com.altitudeinfosys.snowy.weather.Hour;

import altitudeinfosys.com.snowy.R;

/**
 * Created by Tarek on 7/11/2016.
 */
public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private Day[] mDays;

    public DayAdapter(Day[] days){
        mDays = days;
    }
    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_list_item, parent, false);
        DayViewHolder viewHolder = new DayViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, int position) {

        holder.bindDay(mDays[position]);
    }

    @Override
    public int getItemCount() {
        return mDays.length;
    }

    public class DayViewHolder extends RecyclerView.ViewHolder{

        public TextView mTimeLabel;
        public TextView mSummaryLabel;
        public TextView mTempartureLabel;
        public ImageView mIconImageView;


        public DayViewHolder(View itemView) {
            super(itemView);

            mTimeLabel = (TextView) itemView.findViewById(R.id.dayLabel);
            mSummaryLabel = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTempartureLabel = (TextView)itemView.findViewById(R.id.temperatureLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);


        }

        public void bindDay(Day day){
            mTimeLabel.setText(day.getDayOfTheWeek());
            mSummaryLabel.setText(day.getSummary());
            mTempartureLabel.setText(day.getTemperatureSummary());
            mIconImageView.setImageResource(day.getIconId());
        }
    }
}
