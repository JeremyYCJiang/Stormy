package com.jiangziandroid.stormy.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by JeremyYCJiang on 2015/4/8.
 */
public class Hour {
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
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime()*1000);
        String timeString = formatter.format(dateTime);
        return timeString;
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
}
