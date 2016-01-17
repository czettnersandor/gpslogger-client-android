package com.czettner.sandor.gpstracker;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class GpsLoggerService extends Service {

    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public Intent intent;
    public static final String BROADCAST_ACTION = "com.czettner.sandor.gpstracker.action.POST_LOCATION";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int TWENTY_MINUTES = 1000 * 60 * 20;
    private static final int MIN_DISTANCE = 20;
    private static final String TAG = "GPS_LOGGER";
    private SharedPreferences settings;

    public GpsLoggerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        settings = getSharedPreferences(getString(R.string.preference_file_key), CONTEXT_IGNORE_SECURITY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            listener = new MyLocationListener();
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TWENTY_MINUTES, MIN_DISTANCE, listener);
            }
            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TWENTY_MINUTES, MIN_DISTANCE, listener);
            }
        }
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(listener);
        }
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.i(TAG, "Location changed");
            if(isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                sendBroadcast(intent);

                GpsLogSenderService.startActionPostLocation(
                        getApplicationContext(),
                        loc.getLatitude(),
                        loc.getLongitude(),
                        System.currentTimeMillis() / 1000,
                        settings.getString("device_id", "")
                );
            }
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
}

