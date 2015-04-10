package com.jiangziandroid.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiangziandroid.stormy.R;
import com.jiangziandroid.stormy.weather.Day;

/**
 * Created by JeremyYCJiang on 2015/4/9.
 */
public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    public DayAdapter(Context context, Day[] days){
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0; //We aren't going to use this. Tag items for easy reference.
    }


    //called to create the layout for each item being adapted
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            viewHolder.dayNameLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);
            viewHolder.temperatureLabelMax = (TextView) convertView.findViewById(R.id.temperatureLabelMax);
            viewHolder.temperatureLabelMin = (TextView) convertView.findViewById(R.id.temperatureLabelMin);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Day day = mDays[position];
        viewHolder.iconImageView.setImageResource(day.getIconId());
        viewHolder.temperatureLabelMax.setText(String.valueOf(day.getCelsiusTemperatureMax()));
        viewHolder.temperatureLabelMin.setText(String.valueOf(day.getCelsiusTemperatureMin()));
        if(position==0){
            viewHolder.dayNameLabel.setText("今天");
        }else {
            viewHolder.dayNameLabel.setText(day.getDayOfTheWeek());
        }
        return convertView;
    }

    private static class ViewHolder{
        ImageView iconImageView;
        TextView dayNameLabel;
        TextView temperatureLabelMax;
        TextView temperatureLabelMin;
    }
}
