
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class InternetBeatTimeWatchFaceService extends CanvasWatchFaceService {

    private static final long TICK_PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private PowerManager.WakeLock wakeLock;
    private static final String TAG = "BEATS_SWATCH_FACE_DEBUG";

    @Override
    public Engine onCreateEngine() {
        return new SimpleEngine();
    }

    private class SimpleEngine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private static final String ACTION_TIME_ZONE = "time-zone";

        private InternetBeatTimeWatchFace watchFace;
        private Handler timeTick;
        private GoogleApiClient googleApiClient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(InternetBeatTimeWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            timeTick = new Handler(Looper.myLooper());
            startTimerIfNecessary();

            watchFace = InternetBeatTimeWatchFace.newInstance(InternetBeatTimeWatchFaceService.this);
            googleApiClient = new GoogleApiClient.Builder(InternetBeatTimeWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "WatchFaceWakelockTag");
        }

        private void startTimerIfNecessary() {
            Log.d(TAG,"Start of startTimerIfNecessary - remove callbacks");
            timeTick.removeCallbacks(timeRunnable);
            if (isVisible() && !isInAmbientMode()) {
                timeTick.post(timeRunnable);
            }

            // I enter here if is in ambient mode only
            else if (isVisible() && watchFace!=null ) {
                Log.d(TAG,"End of startTimerIfNecessary - start timeTick with beatsRunnable");
                timeTick.post(beatsRunnable);
            }
        }

        private final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                onSecondTick();

                if (isVisible() && !isInAmbientMode()) {
                    timeTick.postDelayed(this, TICK_PERIOD_MILLIS);
                }
            }
        };

        private final Runnable beatsRunnable = new Runnable() {
            @Override
            public void run() {

                if (isVisible() && isInAmbientMode()) {
                    Double secondsToNextBeat=watchFace.getSecondsToNextBeat();
                    Double milliSecondsToNextBeat=secondsToNextBeat*1000;
                    Log.d(TAG,"beatsRunnable - schedule next draw at "+milliSecondsToNextBeat.longValue());
                    timeTick.postDelayed(this,milliSecondsToNextBeat.longValue());
                }
                Log.d(TAG,"beatsRunnable - invalidate and draw");
                onTimeTick();
            }
        };

        private void onSecondTick() {
            invalidateIfNecessary();
        }

        private void invalidateIfNecessary() {
            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                registerTimeZoneReceiver();
                googleApiClient.connect();
                //Log.d(TAG,"Face visible, acquire wakelock");
                //if (!wakeLock.isHeld()) wakeLock.acquire();
            } else {
                //Log.d(TAG,"Face no more visible, release wakelock");
                //if (wakeLock.isHeld()) wakeLock.release();
                unregisterTimeZoneReceiver();
                releaseGoogleApiClient();
            }

            startTimerIfNecessary();
        }

        private void releaseGoogleApiClient() {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(googleApiClient, onDataChangedListener);
                googleApiClient.disconnect();
            }
        }

        private void unregisterTimeZoneReceiver() {
            unregisterReceiver(timeZoneChangedReceiver);
        }

        private void registerTimeZoneReceiver() {
            IntentFilter timeZoneFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(timeZoneChangedReceiver, timeZoneFilter);
        }

        final private BroadcastReceiver timeZoneChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                    watchFace.updateTimeZoneWith(intent.getStringExtra(ACTION_TIME_ZONE));
                }
            }
        };

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            watchFace.draw(canvas, bounds);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            watchFace.setAntiAlias(!inAmbientMode);
            watchFace.setShowSeconds(!isInAmbientMode());

            if (inAmbientMode) {
                watchFace.updateBackgroundColourToDefault();
                watchFace.updateDateAndTimeColourToDefault();
            } else {
                watchFace.restoreBackgroundColour();
                watchFace.restoreDateAndTimeColour();
            }

            invalidate();

            startTimerIfNecessary();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "connected GoogleAPI");

            Wearable.DataApi.addListener(googleApiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(onConnectedResultCallback);
        }

        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        processConfigurationFor(item);
                    }
                }

                dataEvents.release();
                invalidateIfNecessary();
            }
        };

        private void processConfigurationFor(DataItem item) {
            if (WatchfaceSyncCommons.PATH.equals(item.getUri().getPath())) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.containsKey(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR)) {
                    String backgroundColour = dataMap.getString(WatchfaceSyncCommons.KEY_BACKGROUND_COLOUR);
                    watchFace.updateBackgroundColourTo(Color.parseColor(backgroundColour));
                }

                if (dataMap.containsKey(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR)) {
                    String timeColour = dataMap.getString(WatchfaceSyncCommons.KEY_DATE_TIME_COLOUR);
                    watchFace.updateDateAndTimeColourTo(Color.parseColor(timeColour));
                }
                if (dataMap.containsKey(WatchfaceSyncCommons.KEY_AMBIENT_MODE_BEAT_ACCURACY)) {
                    boolean ambientModeAccuracy=dataMap.getBoolean(WatchfaceSyncCommons.KEY_AMBIENT_MODE_BEAT_ACCURACY, false);
                    Log.d(TAG,"Ambient mode accuracy is "+ambientModeAccuracy);
                    if (!ambientModeAccuracy && wakeLock.isHeld())
                        wakeLock.release();
                    else if (ambientModeAccuracy && !wakeLock.isHeld())
                        wakeLock.acquire();
                    //watchFace.wakelockDebug=wakeLock.isHeld();
                }
            }
        }

        private final ResultCallback<DataItemBuffer> onConnectedResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    processConfigurationFor(item);
                }

                dataItems.release();
                invalidateIfNecessary();
            }
        };

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "suspended GoogleAPI");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "connectionFailed GoogleAPI");
        }

        @Override
        public void onDestroy() {
            Log.d(TAG,"Destroy engine");
            wakeLock.release();
            //watchFace.wakelockDebug=wakeLock.isHeld();
            timeTick.removeCallbacks(timeRunnable);
            releaseGoogleApiClient();

            super.onDestroy();
        }
    }
}
