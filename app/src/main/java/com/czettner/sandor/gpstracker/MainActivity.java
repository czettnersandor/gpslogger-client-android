package com.czettner.sandor.gpstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;
    MapView map;
    private IMapController mapController;
    private SimpleLocationOverlay myLocationOverlay;
    private ScaleBarOverlay scaleBarOverlay;
    private ItemizedIconOverlay<OverlayItem> currentLocationOverlay;
    private DefaultResourceProxyImpl resourceProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(9);
        myLocationOverlay = new SimpleLocationOverlay(this);
        map.getOverlays().add(myLocationOverlay);
        scaleBarOverlay = new ScaleBarOverlay(this);
        map.getOverlays().add(scaleBarOverlay);
        // TODO: http://stackoverflow.com/questions/10319094/adding-overlay-to-a-mapview-in-osmdroid

        settings = getSharedPreferences(getString(R.string.preference_file_key), CONTEXT_IGNORE_SECURITY);
        String hash = settings.getString("device_id", "");
        new LoadHistory().execute(settings.getString("url", "") + "/history/" + hash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void settingsClick(MenuItem item) {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

    private class LoadHistory extends AsyncTask<String, Void, String> {

        private static final String TAG = "HISTORY";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: loading indicator
        }

        @Override
        protected String doInBackground(String... urls) {
            String json = null;
            try {
                json = doHttpUrlConnectionAction(urls[0]);
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
            Log.v(TAG, json);
            try {
                // Add markers to the map
                addMapMarkers(json);
            } catch (JSONException e) {
                // TODO
                e.printStackTrace();
            }
            return json;
        }

        protected void addMapMarkers(String json) throws JSONException {
            JSONObject obj = new JSONObject(json);
            String pageName = obj.getJSONObject("pageInfo").getString("pageName");

            JSONArray arr = obj.getJSONArray("posts");
            for (int i = 0; i < arr.length(); i++)
            {
                String post_id = arr.getJSONObject(i).getString("post_id");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        /**
         * Returns the output from the given URL.
         *
         * I tried to hide some of the ugliness of the exception-handling
         * in this method, and just return a high level Exception from here.
         * Modify this behavior as desired.
         * Credit: http://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java
         *
         * @param desiredUrl
         * @return
         * @throws Exception
         */
        private String doHttpUrlConnectionAction(String desiredUrl) throws Exception {
            URL url = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try {
                // create the HttpURLConnection
                url = new URL(desiredUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // just want to do an HTTP GET here
                connection.setRequestMethod("GET");

                // uncomment this if you want to write output to this url
                //connection.setDoOutput(true);

                // give it 15 seconds to respond
                connection.setReadTimeout(30 * 1000);
                connection.connect();

                // read the output from the server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }
}
