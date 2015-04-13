package com.jiangziandroid.stormy.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by JeremyYCJiang on 2015/4/8.
 */
public class Hour implements Parcelable{
    private long mTime;
    private String mTimezone;
    private String mIcon;
    private Double mTemperature;
    private String mSummary;



    public long getTime() {
        return mTime;
    }

    //Convert UNIX time to readable time
    public String getFormattedTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("a h");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime()*1000);
        return formatter.format(dateTime);
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

    public Double getTemperature() {
        return mTemperature;
    }

    public int getCelsiusTemperature(){
        double CelsiusTempereture = (getTemperature()-32)*5/9;
        return (int)Math.round(CelsiusTempereture);
    }

    public void setTemperature(Double temperature) {
        mTemperature = temperature;
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
        dest.writeDouble(mTemperature);
        dest.writeString(mSummary);
    }

    //default public Constructor
    public Hour(){}

    //unwrap data
    private Hour(Parcel in){
        mTime = in.readLong();
        mTimezone = in.readString();
        mIcon = in.readString();
        mTemperature = in.readDouble();
        mSummary = in.readString();
    }

    public static final Creator<Hour> CREATOR = new Creator<Hour>() {
        @Override
        public Hour createFromParcel(Parcel source) {
            return new Hour(source);
        }

        @Override
        public Hour[] newArray(int size) {
            return new Hour[size];
        }
    };
}
