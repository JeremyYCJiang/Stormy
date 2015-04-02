package com.jiangziandroid.stormy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create the API to Forecast Server
        String apiKey = "9131be663489e1f48549c9e550d00b38";
        double latitude = 37.8267;
        double longitude = -122.423;
        String forecastUrl = "https://api.forecast.io/forecast/"+apiKey+"/"+latitude+","+longitude;

        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();
            Call call = client.newCall(request);
            //Transfer synchronous to asynchronous
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        // execute() is synchronous method, so delete it
                        // Response response = call.execute();
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                        } else {
                            alertUserAboutError();
                        }
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this, "Main UI code is running!", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "Main UI code is running!");

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
            return currentWeather;

            /*
            String icon = currently.getString("icon");
            long time = currently.getLong("time");
            double temperature = currently.getDouble("temperature");
            double humidity = currently.getDouble("humidity");
            double precipProbability = currently.getDouble("precipProbability");
            String summary = currently.getString("summary");
            */
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

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}
