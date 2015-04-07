package com.jiangziandroid.stormy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity{

    public static final String TAG = MainActivity.class.getSimpleName();
    protected static final String apiKey = "9131be663489e1f48549c9e550d00b38";
    protected CurrentWeather mCurrentWeather;
    protected GoogleApiClient mGoogleApiClient;
    protected AddressResultReceiver mResultReceiver;
    protected Location mLastLocation;
    protected double latitude;
    protected double longitude;

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
                getForecast();
            }
        });

        getForecast();
        Toast.makeText(this, "Main UI code is running!", Toast.LENGTH_LONG).show();
    }

    private void getForecast() {
        if(isNetworkAvailable()) {
            toggleRefresh();
            buildGoogleApiClient();
        }
        else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
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
                        mResultReceiver = new AddressResultReceiver(new Handler());
                        startIntentService();
                        if ( mLastLocation != null || mResultReceiver.getAddressOutput() != null) {
                            Toast.makeText(MainActivity.this, mResultReceiver.getAddressOutput(), Toast.LENGTH_LONG).show();
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                            //Toast.makeText(MainActivity.this, "mGoogleApiClient Connected.", Toast.LENGTH_LONG).show();
                            //TextView latitudeTextView = (TextView) findViewById(R.id.locationLabel);
                            //latitudeTextView.setText(String.valueOf(latitude));
                            Toast.makeText(MainActivity.this, String.valueOf(latitude)+" , "+String.valueOf(longitude),
                                    Toast.LENGTH_LONG).show();
                            String forecastUrl = "https://api.forecast.io/forecast/"+apiKey+"/"+latitude+","+longitude;

                            OkHttpClient client = new OkHttpClient();
                            //Toast.makeText(MainActivity.this, "Initializing okhttp client...", Toast.LENGTH_LONG).show();
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
                                    }
                                    catch (IOException e) {
                                        Log.e(TAG, "Exception caught: ", e);
                                    }
                                    catch (JSONException e){
                                        Log.e(TAG, "Exception caught: ", e);
                                    }
                                }
                            });
                        } else {
                            toggleRefresh();
                            Toast.makeText(MainActivity.this, "No location detected", Toast.LENGTH_LONG).show();
                        }
                    }
            @Override
            public void onConnectionSuspended(int cause) {
                        // The connection to Google Play services was lost for some reason. We call connect() to
                        // attempt to re-establish the connection.
                        toggleRefresh();
                        Toast.makeText(MainActivity.this, "Connection suspended", Toast.LENGTH_LONG).show();
                        mGoogleApiClient.connect();
                    }
             })
            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult result) {
                    // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
                    // onConnectionFailed.
                    toggleRefresh();
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
        //Toast.makeText(this, "mGoogleApiClient initializing...", Toast.LENGTH_LONG).show();
        mGoogleApiClient.connect();
        Log.i(TAG, "mGoogleApiClient Start Connecting...");
        //Toast.makeText(this, "mGoogleApiClient Start Connecting...", Toast.LENGTH_LONG).show();

    }


    private void updateDisplay() {
        mTemperatureLabel.setText(String.valueOf(mCurrentWeather.getCelsiusTemperature()));
        mTimeLabel.setText("At "+mCurrentWeather.getFormattedTime()+" it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity()+"%");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance()+"%");
        mSummaryTextView.setText(mCurrentWeather.getSummary());
        mWeatherIconImageView.setImageResource(mCurrentWeather.getIconId());
        mLocationTextView.setText(String.valueOf(mCurrentWeather.getLatitude())+" , "+
                                  String.valueOf(mCurrentWeather.getLongitude()));
        mWindspeedValue.setText(String.valueOf(mCurrentWeather.getWindspeed())+"m/s");
    }


    protected void startIntentService() {
        Intent intent = new Intent(MainActivity.this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }


    public class AddressResultReceiver extends ResultReceiver{
        protected String mAddressOutput;

        public String getAddressOutput() {
            return mAddressOutput;
        }

        public void setAddressOutput(String addressOutput) {
            mAddressOutput = addressOutput;
        }

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData){
            mResultReceiver.setAddressOutput(resultData.getString(Constants.RESULT_DATA_KEY));
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MainActivity.this, "Address found!", Toast.LENGTH_LONG).show();
            }
        }
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

}

