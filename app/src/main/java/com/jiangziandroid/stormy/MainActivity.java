package com.jiangziandroid.stormy;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity{

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected double latitude = 37.8267;
    protected double longitude = -122.423;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create the API to Forecast Server
        String apiKey = "9131be663489e1f48549c9e550d00b38";
        //double latitude = 65.9667;//37.8267;
        //double longitude = -18.5333;//-122.423;

        if(isNetworkAvailable()) {
            buildGoogleApiClient();
            Log.d(TAG, String.valueOf(latitude));
            Log.d(TAG, String.valueOf(longitude));
            TextView latitudeTextView = (TextView) findViewById(R.id.locationLabel);
            latitudeTextView.setText(String.valueOf(latitude));
            String forecastUrl = "https://api.forecast.io/forecast/"+apiKey+"/"+latitude+","+longitude;
            OkHttpClient client = new OkHttpClient();

            Toast.makeText(this, "Initializing okhttp client...", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Initializing okhttp client...");

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
            currentWeather.setTimeZone(forecast.getString("timezone"));
            Log.d(TAG, currentWeather.getFormattedTime());
            Log.d(TAG, String.valueOf(currentWeather.getTemperature()));
            Log.d(TAG, String.valueOf(currentWeather.getCelsiusTemperature()));
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


    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        // Provides a simple way of getting a device's location and is well suited for
                        // applications that do not require a fine-grained location and that do not need location
                        // updates. Gets the best and most recent location currently available, which may be null
                        // in rare cases when a location is not available.
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                            Log.i(TAG, "mGoogleApiClient Connected.");
                            Toast.makeText(MainActivity.this, "mGoogleApiClient Connected.", Toast.LENGTH_LONG).show();
                            Log.i(TAG, String.valueOf(latitude));
                            Log.i(TAG, String.valueOf(longitude));
                        } else {
                            Toast.makeText(MainActivity.this, "No location detected", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        // The connection to Google Play services was lost for some reason. We call connect() to
                        // attempt to re-establish the connection.
                        Log.i(TAG, "Connection suspended");
                        Toast.makeText(MainActivity.this, "Connection suspended", Toast.LENGTH_LONG).show();
                        mGoogleApiClient.connect();

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
                        // onConnectionFailed.
                        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
                        Toast.makeText(MainActivity.this, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode(),
                                Toast.LENGTH_LONG).show();
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), MainActivity.this, 1);
                        dialog.show();
                    }
                })
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG, "mGoogleApiClient initializing...");
        Toast.makeText(this, "mGoogleApiClient initializing...", Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
        Log.i(TAG, "mGoogleApiClient Start Connecting...");
        Toast.makeText(this, "mGoogleApiClient Start Connecting...", Toast.LENGTH_LONG).show();

    }

    /*@Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.i(TAG, "mGoogleApiClient Start Connecting...");
        Toast.makeText(this, "mGoogleApiClient Start Connecting...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }*/

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */

    /*
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Log.i(TAG, "mGoogleApiClient Connected.");
            Toast.makeText(this, "mGoogleApiClient Connected.", Toast.LENGTH_LONG).show();
            Log.i(TAG, String.valueOf(latitude));
            Log.i(TAG, String.valueOf(longitude));
        } else {
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Toast.makeText(this, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode(),
                Toast.LENGTH_LONG).show();
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 1);
        dialog.show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
    }
    */

}

