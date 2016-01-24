package com.czettner.sandor.gpstracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    protected SharedPreferences settings;
    protected String url;
    protected String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatusText();
        loadSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void changeStatusText() {
        TextView t = (TextView) findViewById(R.id.textView);
        if (isServiceRunning(GpsLoggerService.class)) {
            t.setText(R.string.logger_running);
        } else {
            t.setText(R.string.logger_stopped);
        }
    }

    protected void loadSettings() {
        url = settings.getString("url", "");
        device_id = settings.getString("device_id", "");
        boolean editChanged = false;
        SharedPreferences.Editor editor = settings.edit();
        if (url == null || url.isEmpty()) {
            // If empty, save with default
            url = getString(R.string.default_url);
            editor.putString("url", url);
            editChanged = true;
        }
        if (device_id == null || device_id.isEmpty()) {
            device_id = getString(R.string.default_device_id);
            editor.putString("device_id", device_id);
            editChanged = true;
        }

        if (editChanged) {
            editor.apply();
        }

        EditText e = (EditText) findViewById(R.id.editText);
        e.setText(url);
        EditText e2 = (EditText) findViewById(R.id.editText2);
        e2.setText(device_id);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start button
     *
     * @param view
     */
    public void startService(View view) {
        Intent intent = new Intent(this, GpsLoggerService.class);
        startService(intent);
        changeStatusText();
    }

    /**
     * Stop button
     *
     * @param view
     */
    public void stopService(View view) {
        Intent intent = new Intent(this, GpsLoggerService.class);
        stopService(intent);
        changeStatusText();
    }

    /**
     * Send Now button
     *
     * @param view
     */
    public void sendNow(View view) {
        // TODO
    }

    /**
     * Save action (toolbar)
     *
     * @param item
     */
    public void saveSettings(MenuItem item) {
        SharedPreferences.Editor editor = settings.edit();
        EditText e = (EditText) findViewById(R.id.editText);
        String url = e.getText().toString();
        EditText e2 = (EditText) findViewById(R.id.editText2);
        String device_id = e2.getText().toString();

        editor.putString("url", url);
        editor.putString("device_id", device_id);
        editor.apply();
        this.finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
