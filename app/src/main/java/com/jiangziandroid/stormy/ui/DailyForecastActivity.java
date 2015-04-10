package com.jiangziandroid.stormy.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import com.jiangziandroid.stormy.R;
import com.jiangziandroid.stormy.adapters.DayAdapter;
import com.jiangziandroid.stormy.weather.Day;

import java.util.Arrays;

public class DailyForecastActivity extends ListActivity {

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
        Day[] days = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        DayAdapter dayAdapter = new DayAdapter(this, days);
        setListAdapter(dayAdapter);


        /**
         String[] daysOfTheWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
         daysOfTheWeek);
         setListAdapter(adapter);
         **/
    }

}
