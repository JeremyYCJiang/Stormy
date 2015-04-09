package com.jiangziandroid.stormy.ui;

import android.app.ListActivity;
import android.os.Bundle;

import com.jiangziandroid.stormy.R;
import com.jiangziandroid.stormy.adapters.DayAdapter;
import com.jiangziandroid.stormy.weather.Day;

public class DailyForecastActivity extends ListActivity {

    //For test
    private Day[] days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        /**
        String[] daysOfTheWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                daysOfTheWeek);
        setListAdapter(adapter);
        **/

        DayAdapter dayAdapter = new DayAdapter(this, days);
    }

}
