package com.czettner.sandor.gpstracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

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

    public GpsLogSenderService() {
        super("GpsLogSenderService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
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
                final String lat = intent.getStringExtra(EXTRA_LAT);
                final String lng = intent.getStringExtra(EXTRA_LNG);
                final String timestamp = intent.getStringExtra(EXTRA_TIMESTAMP);
                final String hash = intent.getStringExtra(EXTRA_HASH);
                handleActionPostLocation(lat, lng, timestamp, hash);
            }
        }
    }

    private void handleActionPostLocation(String lat, String lng, String timestamp, String hash) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
