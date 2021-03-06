package com.jiangziandroid.stormy.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by JeremyYCJiang on 2015/4/8.
 */
public class Day implements Parcelable{
    private long mTime;
    private String mTimezone;
    private String mIcon;
    private double mTemperatureMax;
    private double mTemperatureMin;
    private String mSummary;


    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getTimeZone() {
        return mTimezone;
    }

    public void setTimeZone(String timezone) {
        mTimezone = timezone;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public int getIconId(){
       return Forecast.getIconId(mIcon);
    }

    public double getTemperatureMax() {
        return mTemperatureMax;
    }

    public int getCelsiusTemperatureMax(){
        double CelsiusTempereture = (getTemperatureMax()-32)*5/9;
        return (int)Math.round(CelsiusTempereture);
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }



    public double getTemperatureMin() {
        return mTemperatureMin;
    }

    public int getCelsiusTemperatureMin(){
        double CelsiusTempereture = (getTemperatureMin()-32)*5/9;
        return (int)Math.round(CelsiusTempereture);
    }

    public void setTemperatureMin(double temperatureMin) {
        mTemperatureMin = temperatureMin;
    }

    public String getDayOfTheWeek() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime*1000);
        return formatter.format(dateTime);
    }



    @Override
    public int describeContents() {
        return 0;
    }


    //wrap data
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mTimezone);
        dest.writeString(mIcon);
        dest.writeString(mSummary);
        dest.writeDouble(mTemperatureMax);
        dest.writeDouble(mTemperatureMin);
    }

    //default public Constructor
    public Day(){}

    //unwrap data
    private Day(Parcel in){
        mTime = in.readLong();
        mTimezone = in.readString();
        mIcon = in.readString();
        mSummary = in.readString();
        mTemperatureMax = in.readDouble();
        mTemperatureMin = in.readDouble();
    }

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };
}
