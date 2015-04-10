package com.example.inmarsat.hellomap;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends FragmentActivity {
    private GoogleMap mMap;

    // longitude and latitude used to focus the map area
    private LatLngBounds STJOHNS_LONDON = new LatLngBounds(
            new LatLng(47, -71), new LatLng(53, 4));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        // set the background of the text view showing the data from the API calls
        if (findViewById(R.id.detailsText) != null) {
            TextView view = (TextView) findViewById(R.id.detailsText);
            view.setBackgroundColor(Color.BLACK);
            view.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            // already set up
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            // map not working
            return;
        }
        // Initialize map options
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void locationButtonPress(View view) {
        new CallLocationAPI().execute();
    }

    public void accessnetworkPress(View view) {
        new CallAccessNetworkAPI().execute();
    }

    class CallLocationAPI extends AsyncTask<String, String, String> {

        double latitude; // Used to store latitude from API response
        double longitude;// Used to store longitude from API response

        @Override
        protected String doInBackground(String... params) {

            // Set up with the REST API end point
            HttpGet request = new HttpGet("https://api-sandbox.inmarsat.com/v1/application/currentuser/location");
            // Add the authorization header, this key is for the sandbox only, use your own in the live environment
            request.addHeader("Authorization", "0MG9OAlw9vJMkJ58Vw2spBEjmsvWi8IA");

            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse;
            int responseCode;
            String message;
            try
            {
                // Try to connect to the API by sending the request
                httpResponse = client.execute(request);
                responseCode = httpResponse.getStatusLine().getStatusCode();
                message = httpResponse.getStatusLine().getReasonPhrase();
                if(responseCode == 200)
                {
                    HttpEntity entity = httpResponse.getEntity();

                    if (entity != null)
                    {
                        InputStream instream = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        try
                        {
                            while ((line = reader.readLine()) != null)
                            {
                                sb.append(line + "\n");
                            }
                            instream.close();
                        }
                        catch (IOException e)
                        { }
                        JSONObject json = new JSONObject(sb.toString());
                        latitude = json.getDouble("latitude");
                        longitude = json.getDouble("longitude");
                        return "Latitude: "+latitude+" "+"Longitude: "+longitude;
                    }
                }
            }
            catch (Exception e)
            { return "Exception: "+e;}

            return "Response Code: "+Integer.toString(responseCode)+" "+message;
        }

        protected void onPostExecute(String message) {
            // the asynchronous call to the API has completed, display the data
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(STJOHNS_LONDON, 0));
            if (mMap !=null)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(message));
                if (findViewById(R.id.detailsText) != null) {
                    TextView view = (TextView) findViewById(R.id.detailsText);
                    view.setText(message);
                }
            }
        }

    } // end CallLocationAPI

    class CallAccessNetworkAPI extends AsyncTask<String, String, String> {

        int responseCode;
        String message;

        @Override
        protected String doInBackground(String... params) {

            HttpGet request = new HttpGet("https://api-sandbox.inmarsat.com/v1/application/currentuser/accessNetwork");
            request.addHeader("Authorization", "0MG9OAlw9vJMkJ58Vw2spBEjmsvWi8IA");

            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse;
            try
            {
                httpResponse = client.execute(request);
                responseCode = httpResponse.getStatusLine().getStatusCode();
                message = httpResponse.getStatusLine().getReasonPhrase();
                if(responseCode == 200)
                {
                    HttpEntity entity = httpResponse.getEntity();

                    if (entity != null)
                    {
                        InputStream instream = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        try
                        {
                            while ((line = reader.readLine()) != null)
                            {
                                sb.append(line + "\n");
                            }
                            instream.close();
                        }
                        catch (IOException e)
                        { }
                        JSONObject json = new JSONObject(sb.toString());
                        String accessnetwork = json.getString("accessNetworkName");
                        return "Access Network: "+accessnetwork;
                    }
                }
            }
            catch (Exception e)
            { return "Exception: "+e;}

            return "Response Code: "+Integer.toString(responseCode)+" "+message;

        }

        protected void onPostExecute(String message) {
            if (findViewById(R.id.detailsText) != null) {
                TextView view = (TextView) findViewById(R.id.detailsText);
                view.setText(message);
            }
        }

    } // end CallAccessNetworkAPI
}
