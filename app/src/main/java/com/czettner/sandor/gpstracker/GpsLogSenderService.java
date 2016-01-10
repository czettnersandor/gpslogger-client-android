package com.czettner.sandor.gpstracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class GpsLogSenderService extends IntentService {

    private static final String ACTION_POST_LOCATION = "com.czettner.sandor.gpstracker.action.POST_LOCATION";
    private static final String EXTRA_LAT = "com.czettner.sandor.gpstracker.extra.LAT";
    private static final String EXTRA_LNG = "com.czettner.sandor.gpstracker.extra.LNG";
    private static final String EXTRA_TIMESTAMP = "com.czettner.sandor.gpstracker.extra.TIMESTAMP";
    private static final String EXTRA_HASH = "com.czettner.sandor.gpstracker.extra.HASH";
    private static final String TAG = "GPS_LOGGER";

    private SharedPreferences settings;

    public GpsLogSenderService() {
        super("GpsLogSenderService");
        settings = getSharedPreferences(getString(R.string.preference_file_key), CONTEXT_IGNORE_SECURITY);
    }

    /**
     * Starts this service to perform action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPostLocation(Context context, Double lat, Double lng, long timestamp, String hash) {
        Intent intent = new Intent(context, GpsLogSenderService.class);
        intent.setAction(ACTION_POST_LOCATION);
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_TIMESTAMP, timestamp);
        intent.putExtra(EXTRA_HASH, hash);
        intent.putExtra(EXTRA_LNG, lng);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POST_LOCATION.equals(action)) {
                final Double lat = intent.getDoubleExtra(EXTRA_LAT, 0.00);
                final Double lng = intent.getDoubleExtra(EXTRA_LNG, 0.00);
                final long timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, (long) 0);
                final String hash = intent.getStringExtra(EXTRA_HASH);
                handleActionPostLocation(lat, lng, timestamp, hash);
            }
        }
    }

    protected String getServerUrl() {
        String url = "";
        url = settings.getString("url", url);
        return url;
    }

    private void handleActionPostLocation(Double lat, Double lng, long timestamp, String hash) {
        URL url;
        HttpURLConnection connection = null;
        String targetURL = getServerUrl();
        String urlParameters =
                "lat=" + URLEncoder.encode("", "UTF-8") +
                "&lng=" + URLEncoder.encode("???", "UTF-8") +
                "&timestamp=" + URLEncoder.encode("???", "UTF-8") +
                "&hash=" + URLEncoder.encode("???", "UTF-8");
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            Log.i(TAG, response.toString());

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }


}
