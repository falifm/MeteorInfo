package miculka.jakub.meteorinfo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity runs first
 */
public class MainActivity extends AppCompatActivity {

    //strings to exchange values between activities
    public static final String CURRENT_NAME = "miculka.jakub.meteorInfo.name";
    public static final String CURRENT_INFO = "miculka.jakub.meteorInfo.info";
    public static final String CURRENT_LAT = "miculka.jakub.meteorInfo.latitude";
    public static final String CURRENT_LONG = "miculka.jakub.meteorInfo.longitude";
    //keys to SharedPreferences
    private static final String ACTUAL_DAY = "actualDay";
    private static final String SAVED_LIST = "savedList";
    //some strings
    private static final String APP_TITLE = "MeteorInfo";
    private static final String APP_SUBTITLE = "Meteors found since 2011";
    private static final String APP_DATA_ACQUIRE = "Acquiring data...";
    private static final String APP_DATA_ERR = "Error during data acquiring!";
    //messages showing in dialog window
    private static final String NO_INTERNET_TITLE = "No connection error";
    private static final String NO_INTERNET_MSG = "This app requires internet connection for the run!";
    private static final String NO_DATA_TITLE = "Not enough data";
    private static final String NO_DATA_MSG = "Sorry, we don´t have geographical position for this item.";

    //token used for my app
    private static final String TOKEN = "zE7RaxyjctLgln59VL3FYqD9m";
    //this is URL containing path, filtering by the year and ordering follows below
    private static final String URL = "https://data.nasa.gov/resource/y77d-th95.json?$";
    private static final String YEARPARAM = "where=year%20>=%20%272011-01-01T00:00:00.000%27";
    private static final String ORDERPARAM = "order=mass%20DESC";

    private RequestQueue requestQueue;
    private ListView dataList;
    private List<Meteor> meteorList = new ArrayList<Meteor>();
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(APP_TITLE);
        toolbar.setSubtitle(APP_SUBTITLE);
        setSupportActionBar(toolbar);

        //setup progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(APP_DATA_ACQUIRE);
        pDialog.setCancelable(false);

        //determine next action based on actual time and network status
        if (isNewDay() && isNetworkAvailable()) {
            downloadActualData();
        }
        else {
            getMeteorListFromMemory();
            addMeteorsToDataList();
        }
    }

    /*
    Functions for data download - Volley library used
     */
    private void downloadActualData() {
        showpDialog();
        String query = URL + YEARPARAM + "&$" + ORDERPARAM;
        requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest arrayReq = new JsonArrayRequest(query,
                // second parameter Listener overrides the method onResponse() and passes
                //JSONArray as a parameter
                new Response.Listener<JSONArray>() {
                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //iterate through obtained array and fill the list
                            meteorList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject meteorJSON = response.getJSONObject(i);
                                Meteor meteorObj = new Meteor(meteorJSON.getString("name"),
                                        meteorJSON.getInt("mass"), meteorJSON.getString("year"),
                                        meteorJSON.getString("reclat"), meteorJSON.getString("reclong"));
                                meteorList.add(meteorObj);
                            }
                            putMeteorListToMemory();
                            addMeteorsToDataList();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hidepDialog();
                    }
                },
                // third parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("VOLLEY ERR", "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),APP_DATA_ERR, Toast.LENGTH_SHORT).show();
                        // hide the progress dialog
                        hidepDialog();
                    }
                }) {
            /**
             * Put token to HTTP headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-App-Token", TOKEN);
                return headers;
            }
        };
        requestQueue.add(arrayReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //if refresh was selected, download data
        if (id == R.id.action_refresh) {
            if (isNetworkAvailable()) {
                downloadActualData();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds meteor objects to the listView
     */
    private void addMeteorsToDataList() {
        ArrayAdapter<Meteor> adapter = new CustomListAdapter(MainActivity.this, meteorList);
        dataList = (ListView) findViewById(R.id.dataList);
        dataList.setAdapter(adapter);
        //when the item is clicked we will start new activity
        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Meteor clickedM = meteorList.get(position);
                //we need to have internet connection for google maps
                if (!isNetworkAvailable()) {
                    showAlertDialog(NO_INTERNET_TITLE, NO_INTERNET_MSG);
                }
                //also we dont need to show meteors without given latitude and longitude
                else if (((int)clickedM.getLatitude() == 0) && ((int)clickedM.getLongitude() == 0)) {
                    showAlertDialog(NO_DATA_TITLE, NO_DATA_MSG);
                }
                else {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra(CURRENT_NAME, clickedM.getName());
                    intent.putExtra(CURRENT_INFO, ((TextView) view.findViewById(R.id.listItemYearWeight)).getText());
                    intent.putExtra(CURRENT_LAT, clickedM.getLatitude());
                    intent.putExtra(CURRENT_LONG, clickedM.getLongitude());

                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Saving meteorlist to SharedPreferences - GSON library used for json->string conversion
     */
    private void putMeteorListToMemory() {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(meteorList);
        prefsEditor.putString(SAVED_LIST, json);
        prefsEditor.commit();
    }

    /**
     * Loading meteorlist from SharedPreferences - GSON library used for string->json conversion
     */
    private void getMeteorListFromMemory() {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString(SAVED_LIST, "");

        //if there is no list in memory try to download it - if there is no internet, show alert
        if (json.isEmpty()) {
            if (isNetworkAvailable()) {
                downloadActualData();
            }
            else {
                showAlertDialog(NO_INTERNET_TITLE, NO_INTERNET_MSG);
            }
        }
        else {
            Type type = new TypeToken<List<Meteor>>(){}.getType();
            meteorList = gson.fromJson(json, type);
        }
    }

    /**
     * Progress dialog functions
     */
    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isNewDay() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        int currentDay = Integer.parseInt(sdf.format(cal.getTime()));

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        int lastDay = appSharedPrefs.getInt(ACTUAL_DAY, -666);

        if ((lastDay == -666) || (currentDay > lastDay)) {
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(ACTUAL_DAY, currentDay);
            prefsEditor.commit();

            return true;
        }

        return false;
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("OK",null);
        alert.show();
    }
}
