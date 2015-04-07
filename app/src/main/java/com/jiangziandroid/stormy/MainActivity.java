package com.jiangziandroid.stormy;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements AMapLocationListener{

    public static final String TAG = MainActivity.class.getSimpleName();
    protected static final String apiKey = "9131be663489e1f48549c9e550d00b38";
    protected CurrentWeather mCurrentWeather;
    protected double latitude;
    protected double longitude;
    protected String address;
    protected String city;
    protected String district;
    protected LocationManagerProxy mLocationManagerProxy;

    //inject TextView and ImageView member variable.
    @InjectView(R.id.weatherIconImageView)  ImageView mWeatherIconImageView;
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel)  TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryTextView)   TextView mSummaryTextView;
    @InjectView(R.id.locationLabel) TextView mLocationTextView;
    @InjectView(R.id.windspeedValue) TextView mWindspeedValue;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.refreshProgressBar)    ProgressBar mRefreshProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Use ButterKnife to inject views into our Activity
        ButterKnife.inject(this);

        mRefreshProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocate();
            }
        });
        initLocate();
        Toast.makeText(this, "Main UI code is running!", Toast.LENGTH_LONG).show();
    }


    private void initLocate(){
        if(isNetworkAvailable()){
        toggleRefresh();
        //根据给定的参数构造一个 LocationManagerProxy 的对象。
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        //设置是否使用GPS定位，仅在使用 privoder 为 LocationProviderProxy.AMapNetwork 时生效。
        mLocationManagerProxy.setGpsEnable(false);
        // LocationProviderProxy:位置提供者，提供定期报告设备的地理位置。
        // AMapNetwork:高德网络定位服务。
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        //requestLocationData(java.lang.String provider, long minTime, float minDistance,
        // AMapLocationListener listener)注册监听。
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 15, this);
        }
        else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }



    //返回当前的定位位置，在 requestLocationData 条件满足时触发回调。
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation!=null
                && aMapLocation.getAMapException().getErrorCode() == 0){
            // 定位成功回调信息，设置相关消息
            latitude = aMapLocation.getLatitude();
            longitude = aMapLocation.getLongitude();
            address = aMapLocation.getAddress();
            city = aMapLocation.getCity();
            district = aMapLocation.getDistrict();
            getForecast();
        }
        else{
            toggleRefresh();
            Log.e("AmapErr","Location ERR:" + aMapLocation.getAMapException().getErrorCode());
            Toast.makeText(this, "AmapErr: Location ERR:" + aMapLocation.getAMapException().getErrorCode(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getForecast() {
                String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(forecastUrl).build();
                Call call = client.newCall(request);
                //Transfer synchronous to asynchronous
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        alertUserAboutError();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        try {
                            // execute() is synchronous method, so delete it
                            // Response response = call.execute();
                            String jsonData = response.body().string();
                            Log.v(TAG, jsonData);
                            if (response.isSuccessful()) {
                                mCurrentWeather = getCurrentDetails(jsonData);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            } else {
                                alertUserAboutError();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                    }

                });
            }


    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
            JSONObject forecast = new JSONObject(jsonData);
            String timezone = forecast.getString("timezone");
                Log.i(TAG, "From JSON:" + timezone);
            JSONObject currently = forecast.getJSONObject("currently");

            CurrentWeather currentWeather = new CurrentWeather();
            currentWeather.setIcon(currently.getString("icon"));
            currentWeather.setTime(currently.getLong("time"));
            currentWeather.setTemperature(currently.getDouble("temperature"));
            currentWeather.setHumidity(currently.getDouble("humidity"));
            currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
            currentWeather.setSummary(currently.getString("summary"));
            currentWeather.setTimeZone(forecast.getString("timezone"));
            currentWeather.setLatitude(forecast.getDouble("latitude"));
            currentWeather.setLongitude(forecast.getDouble("longitude"));
            currentWeather.setWindspeed(currently.getDouble("windSpeed"));
            return currentWeather;
    }


    private void updateDisplay() {
        mTemperatureLabel.setText(String.valueOf(mCurrentWeather.getCelsiusTemperature()));
        mTimeLabel.setText("At "+mCurrentWeather.getFormattedTime()+" it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity()+"%");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance()+"%");
        mSummaryTextView.setText(mCurrentWeather.getSummary());
        mWeatherIconImageView.setImageResource(mCurrentWeather.getIconId());
        mLocationTextView.setText(String.valueOf(mCurrentWeather.getLatitude()) + " , " +
                String.valueOf(mCurrentWeather.getLongitude())+'\n'+ city+" , "+district);
        Toast.makeText(this, latitude + " , " +longitude, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, address, Toast.LENGTH_LONG).show();
        mWindspeedValue.setText(String.valueOf(mCurrentWeather.getWindspeed())+"m/s");
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo!=null&&networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void toggleRefresh() {
        if(mRefreshProgressBar.getVisibility() == View.INVISIBLE){
            mRefreshProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else{
            mRefreshProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}

