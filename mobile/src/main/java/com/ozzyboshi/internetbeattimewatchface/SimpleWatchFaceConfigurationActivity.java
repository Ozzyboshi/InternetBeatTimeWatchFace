
/*
 * Copyright (C) 2015 Alessio Garzi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ozzyboshi.internetbeattimewatchface;

import android.graphics.Color;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.woalk.apps.lib.colorpicker.ColorPickerDialog;
import com.woalk.apps.lib.colorpicker.ColorPickerSwatch;

public class SimpleWatchFaceConfigurationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SimpleWatchface";

    private GoogleApiClient googleApiClient;
    private View backgroundColourImagePreview;
    private View dateAndTimeColourImagePreview;
    private WatchConfigurationPreferences watchConfigurationPreferences;
    private PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_configuration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        findViewById(R.id.configuration_background_colour).setOnClickListener(new View.OnClickListener() {

            //private PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);

            @Override
            public void onClick(View v) {
                int defaultColor = putDataMapReq.getDataMap().getInt(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR, Color.BLACK);
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.pick_background_colour, getResources().getIntArray(R.array.colors_intarray), defaultColor, 4, ColorPickerDialog.SIZE_SMALL);
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int colour) {
                        watchConfigurationPreferences.setBackgroundColour(colour);
                        putDataMapReq.getDataMap().putInt(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR, colour);
                        ((GradientDrawable) backgroundColourImagePreview.getBackground()).setColor(colour);
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    }
                });
                dialog.show(getFragmentManager(), "backgroundColour");
            }
        });

        findViewById(R.id.configuration_time_colour).setOnClickListener(new View.OnClickListener() {

            //private PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);

            @Override
            public void onClick(View v) {
                int defaultColor = putDataMapReq.getDataMap().getInt(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR, Color.WHITE);
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.pick_date_time_colour, getResources().getIntArray(R.array.colors_intarray), defaultColor, 4, ColorPickerDialog.SIZE_SMALL);
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int colour) {
                        watchConfigurationPreferences.setDateAndTimeColour(colour);
                        putDataMapReq.getDataMap().putInt(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR, colour);
                        ((GradientDrawable) dateAndTimeColourImagePreview.getBackground()).setColor(colour);
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    }
                });
                dialog.show(getFragmentManager(), "dateAndTimeColour");
            }
        });

        ((ToggleButton) findViewById(R.id.configuration_ambient_mode_accuracy)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);
                putDataMapReq.getDataMap().putBoolean(WatchfaceSyncCommons.KEY_AMBIENT_MODE_BEAT_ACCURACY, !isChecked);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                watchConfigurationPreferences.setKeyAmbientModeAccuracy(isChecked);
            }
        });

        ((ToggleButton) findViewById(R.id.configuration_world_map)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);
                putDataMapReq.getDataMap().putBoolean(WatchfaceSyncCommons.KEY_WORLDMAP_BACKGROUND, isChecked);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                watchConfigurationPreferences.setWorldMapBackground(isChecked);
            }
        });

        ((ToggleButton)findViewById(R.id.configuration_internetbeattime_show_date)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);
                putDataMapReq.getDataMap().putBoolean(WatchfaceSyncCommons.KEY_WORLDMAP_SHOW_INTERNETBEATTIME_DATE, isChecked);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                watchConfigurationPreferences.setWorldMapInternetBeattimeShowDate(isChecked);
            }
        });

        backgroundColourImagePreview = findViewById(R.id.configuration_background_colour_preview);
        dateAndTimeColourImagePreview = findViewById(R.id.configuration_date_and_time_colour_preview);

        watchConfigurationPreferences = WatchConfigurationPreferences.newInstance(this);

        ((GradientDrawable) backgroundColourImagePreview.getBackground()).setColor(watchConfigurationPreferences.getBackgroundColour());
        ((GradientDrawable) dateAndTimeColourImagePreview.getBackground()).setColor(watchConfigurationPreferences.getDateAndTimeColour());

        if (watchConfigurationPreferences.showWorldMapBackground())
            ((ToggleButton) findViewById(R.id.configuration_world_map)).setChecked(true);

        if (watchConfigurationPreferences.getAmbientModeAccuracy())
            ((ToggleButton) findViewById(R.id.configuration_ambient_mode_accuracy)).setChecked(true);


        putDataMapReq.getDataMap().putInt(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR, watchConfigurationPreferences.getDateAndTimeColour());
        putDataMapReq.getDataMap().putInt(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR, watchConfigurationPreferences.getBackgroundColour());

        WebView credits = (WebView)findViewById(R.id.credits);
        credits.loadUrl("file:///android_asset/www/credits.html");
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }
}
