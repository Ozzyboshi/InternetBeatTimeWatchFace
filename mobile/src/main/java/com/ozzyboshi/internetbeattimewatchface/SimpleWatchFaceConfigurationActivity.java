
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
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.ozzyboshi.simpleandroidwatchface.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SimpleWatchFaceConfigurationActivity extends ActionBarActivity implements ColourChooserDialog.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SimpleWatchface";
    private static final String TAG_BACKGROUND_COLOUR_CHOOSER = "background_chooser";
    private static final String TAG_DATE_AND_TIME_COLOUR_CHOOSER = "date_time_chooser";

    private GoogleApiClient googleApiClient;
    private View backgroundColourImagePreview;
    private View dateAndTimeColourImagePreview;
    private WatchConfigurationPreferences watchConfigurationPreferences;

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
            @Override
            public void onClick(View v) {
                ColourChooserDialog.newInstance(getString(R.string.pick_background_colour))
                        .show(getFragmentManager(), TAG_BACKGROUND_COLOUR_CHOOSER);
            }
        });

        findViewById(R.id.configuration_time_colour).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColourChooserDialog.newInstance(getString(R.string.pick_date_time_colour))
                        .show(getFragmentManager(), TAG_DATE_AND_TIME_COLOUR_CHOOSER);
            }
        });

        ((ToggleButton) findViewById(R.id.configurazion_ambient_mode_accuracy)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);
                putDataMapReq.getDataMap().putBoolean(WatchfaceSyncCommons.KEY_AMBIENT_MODE_BEAT_ACCURACY, !isChecked);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
            }
        });

        backgroundColourImagePreview = findViewById(R.id.configuration_background_colour_preview);
        dateAndTimeColourImagePreview = findViewById(R.id.configuration_date_and_time_colour_preview);

        watchConfigurationPreferences = WatchConfigurationPreferences.newInstance(this);

        backgroundColourImagePreview.setBackgroundColor(watchConfigurationPreferences.getBackgroundColour());
        dateAndTimeColourImagePreview.setBackgroundColor(watchConfigurationPreferences.getDateAndTimeColour());

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
    public void onColourSelected(String colour, String tag) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WatchfaceSyncCommons.PATH);

        if (TAG_BACKGROUND_COLOUR_CHOOSER.equals(tag)) {
            backgroundColourImagePreview.setBackgroundColor(Color.parseColor(colour));
            watchConfigurationPreferences.setBackgroundColour(Color.parseColor(colour));
            putDataMapReq.getDataMap().putString(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR, colour);
        } else {
            dateAndTimeColourImagePreview.setBackgroundColor(Color.parseColor(colour));
            watchConfigurationPreferences.setDateAndTimeColour(Color.parseColor(colour));
            putDataMapReq.getDataMap().putString(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR, colour);
        }

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
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
