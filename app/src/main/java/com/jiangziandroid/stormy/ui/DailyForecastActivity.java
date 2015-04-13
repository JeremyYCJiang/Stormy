package com.jiangziandroid.stormy.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiangziandroid.stormy.R;
import com.jiangziandroid.stormy.adapters.DayAdapter;
import com.jiangziandroid.stormy.weather.Day;

import java.util.Arrays;

public class DailyForecastActivity extends ListActivity {

    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        Intent intent = getIntent();
        String address = intent.getStringExtra("ADDRESS");
        TextView locationTextView = (TextView) findViewById(R.id.locationLabel);
        locationTextView.setText(address);
        //Receive data from MainActivity using Parcelable(Could not immediately receive the Object
        // because of the way activities are created and destroyed)
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        DayAdapter dayAdapter = new DayAdapter(this, mDays);
        setListAdapter(dayAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //the first line of code that we normally add when overriding a method from the superclass
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String conditions = mDays[position].getSummary();
        String highestTemperature = String.valueOf(mDays[position].getCelsiusTemperatureMax());
        String message = String.format("在%s最高气温为%s度，天气是%s",dayOfTheWeek, highestTemperature, conditions);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
