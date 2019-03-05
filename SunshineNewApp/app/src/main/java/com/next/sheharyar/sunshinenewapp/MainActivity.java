package com.next.sheharyar.sunshinenewapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.next.sheharyar.sunshinenewapp.Adapters.ForeCastRecyclerViewAdapter;
import com.next.sheharyar.sunshinenewapp.Adapters.ForecastAdapter;
import com.next.sheharyar.sunshinenewapp.data.SunshinePreferences;
import com.next.sheharyar.sunshinenewapp.utilities.NetworkUtils;
import com.next.sheharyar.sunshinenewapp.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String[]>{

    TextView mWeatherTextView, mDisplayErrorMessage;
    ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;
    private ForeCastRecyclerViewAdapter mForecastAdapter;
    private static final int FORECAST_LOADER_ID = 0;
    private static final String LOG_TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherTextView = (TextView)findViewById(R.id.tv_weather_data);
        mDisplayErrorMessage = (TextView)findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_forecast);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */

         mRecyclerView.setHasFixedSize(true);

         /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForeCastRecyclerViewAdapter();

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // COMPLETED (7) Remove the code for the AsyncTask and initialize the AsyncTaskLoader
        /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */

        int loaderId = FORECAST_LOADER_ID;

         /*
         * From MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */

        LoaderManager.LoaderCallbacks<String[]> callBack = MainActivity.this;

         /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */

         Bundle bundleForLoader = null;

         /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */

         getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callBack);

        /* Once all of our views are setup, we can load the weather data. */

//        loadWeatherData();
    }

//    private void loadWeatherData(){
//        String location = SunshinePreferences.getPreferredWeatherLocation(this);
//        new FetchWeaatherTask().execute(location);
//    }

    private void showWeatherDataView(){
        mDisplayErrorMessage.setVisibility(View.INVISIBLE);
//        mWeatherTextView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
        mDisplayErrorMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    // COMPLETED (2) Within onCreateLoader, return a new AsyncTaskLoader that looks a lot like the existing FetchWeatherTask.
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param loaderArgs Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle loaderArgs) {
        return new AsyncTaskLoader<String[]>(this) {

            /* This String array will hold and help cache our weather data */
            String [] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * This is the method of the AsyncTaskLoader that will load and parse the JSON data
             * from OpenWeatherMap in the background.
             *
             * @return Weather data from OpenWeatherMap as an array of Strings.
             *         null if an error occurs
             */
            @Override
            public String[] loadInBackground() {

                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(MainActivity.this);
                URL weatherUrl = NetworkUtils.buildUrl(locationQuery);
                try {
                    String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherUrl);
                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);
                    return simpleJsonWeatherData;
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;


            }

            /**
             * Sends the result of the load to the registered listener.
             *
             * @param data The result of the load
             */
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    // COMPLETED (4) When the load is finished, show either the data or an error message if there is no data
    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);
        if (data == null){
            showErrorMessage();
        } else {
            showWeatherDataView();
        }

    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

//    public class FetchWeaatherTask extends AsyncTask<String, Void, String[]>{
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//            /* if there is no zip code there's nothing to lookup ..*/
//
//            if (params.length == 0){
//                return null;
//            }
//
//            String location = params[0];
//            URL weatherRequestUrl = NetworkUtils.buildUrl(location);
//
//            try {
//                String jsonWeatherResponse = NetworkUtils
//                        .getResponseFromHttpUrl(weatherRequestUrl);
//
//                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
//                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);
//
//                return simpleJsonWeatherData;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String[] weatherData) {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
////            if (weatherData != null){
////
////               showWeatherDataView();
////                 /*
////                 * Iterate through the array and append the Strings to the TextView. The reason why we add
////                 * the "\n\n\n" after the String is to give visual separation between each String in the
////                 * TextView. Later, we'll learn about a better way to display lists of data.
////                 */
////                 for (String weatherString : weatherData){
////                     mWeatherTextView.append((weatherString) + "\n\n\n");
////                 }
////            } else {
////                showErrorMessage();
////            }
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            if (weatherData != null) {
//                showWeatherDataView();
//                mForecastAdapter.setWeatherData(weatherData);
//            } else {
//                showErrorMessage();
//            }
//        }
//
//    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invalidateData(){
        mForecastAdapter.setWeatherData(null);
    }
    /**
     * This method uses the URI scheme for showing a location found on a map in conjunction with
     * an implicit Intent. This super-handy intent is detailed in the "Common Intents" page of
     * Android's developer site:
     *
     * @see "http://developer.android.com/guide/components/intents-common.html#Maps"
     * <p>
     * Protip: Hold Command on Mac or Control on Windows and click that link to automagically
     * open the Common Intents page
     */
    private void openLocationInMap(){
        String addressString = "1600 Ampitheatre Parkway, CA";
        Uri geolocation = Uri.parse("geo:0,0?q="+addressString);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);

        if (intent.resolveActivity(getPackageManager())!= null){
            startActivity(intent);
        }else {
            Log.d(LOG_TAG, "Could not call" + geolocation.toString() + ", no recieving apps installed");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.action_refresh){
            invalidateData();
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);

            return true;
        }

        if (itemID == R.id.action_map){
            openLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
