
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import com.ozzyboshi.worldmap.WorldMapMaker;
import com.ozzyboshi.worldmap.androidgraphics.WorldMapAndroidGraphicsDraw;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("deprecation")
public class InternetBeatTimeWatchFace {

    private static final String TIME_FORMAT_WITHOUT_SECONDS = "%02d.%02d";
    private static final String TIME_FORMAT_WITH_SECONDS = TIME_FORMAT_WITHOUT_SECONDS + ".%02d";
    private static final String DATE_FORMAT = "%02d.%02d.%d";
    private static final int DATE_AND_TIME_DEFAULT_COLOUR = Color.WHITE;
    private static final int BACKGROUND_DEFAULT_COLOUR = Color.BLACK;

    private static final String TAG = "BEATS_SWATCH_FACE_DEBUG";

    private final Paint timePaint;
    private final Paint beatTimePaint;
    private final Paint dateOnlyPaint;
    private final Paint backgroundPaint;
    private final Time time;
    private org.joda.time.DateTime utc;
    private final TimeZone beatZone;

    //public static boolean wakelockDebug=false;

    private boolean shouldShowSeconds = true;
    private int backgroundColour = BACKGROUND_DEFAULT_COLOUR;
    private int dateAndTimeColour = DATE_AND_TIME_DEFAULT_COLOUR;

    private Bitmap worldMapBitmap=null;
    private long lastBackgroundUpdateTimeStamp=0; // Last timestamp when worldMapBitmap has been updated

    private boolean showInternetBeatTimeDate=false; // if true prints the internet beat time date

    public static InternetBeatTimeWatchFace newInstance(Context context) {
        Paint timePaint = new Paint();
        timePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        timePaint.setTextSize(context.getResources().getDimension(R.dimen.time_size));
        timePaint.setAntiAlias(true);

        Paint beatTimePaint = new Paint();
        beatTimePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        beatTimePaint.setTextSize(context.getResources().getDimension(R.dimen.date_size));
        final Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        beatTimePaint.setTypeface(font);
        beatTimePaint.setAntiAlias(true);

        Paint dateOnlyPaint = new Paint();
        dateOnlyPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        final Typeface dateFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
        dateOnlyPaint.setTypeface(dateFont);
        dateOnlyPaint.setTextSize(context.getResources().getDimension(R.dimen.date_size));
        dateOnlyPaint.setAntiAlias(true);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_DEFAULT_COLOUR);

        return new InternetBeatTimeWatchFace(timePaint, beatTimePaint, dateOnlyPaint, backgroundPaint, new Time());
    }

    public void setShowInternetBeatTimeDate(boolean showInternetBeatTimeDate) {
        this.showInternetBeatTimeDate = showInternetBeatTimeDate;
    }

    private boolean isShowInternetBeatTimeDate() {
        return showInternetBeatTimeDate;
    }

    @SuppressWarnings("deprecation")
    private InternetBeatTimeWatchFace(Paint timePaint, Paint beatTimePaint, Paint dateOnlyPaint, Paint backgroundPaint, @SuppressWarnings("deprecation") Time time) {
        this.timePaint = timePaint;
        this.beatTimePaint = beatTimePaint;
        this.dateOnlyPaint = dateOnlyPaint;
        this.backgroundPaint = backgroundPaint;
        this.time = time;
        utc = null;
        beatZone = TimeZone.getTimeZone("GMT+1");
    }

    private double getBeats() {
        utc = new org.joda.time.DateTime(org.joda.time.DateTimeZone.forTimeZone(beatZone));
        double swatch = (double)((utc.getHourOfDay() )%24)+(double) utc.getMinuteOfHour()/60 + (double) utc.getSecondOfMinute()/3600;
        return swatch*1000/24;
    }

    public Double getSecondsToNextBeat() {
        double beats=getBeats();
        String beatTimeText=String.format(java.util.Locale.ENGLISH, "%.3f", beats);

        String milliBeats=beatTimeText.substring(beatTimeText.length() - 3);
        Integer milliBeatsToNextBeat=1000-Integer.valueOf(milliBeats);
        Log.d(TAG,"getSecondsToNextBeat - text : "+beatTimeText+" millibeats "+milliBeats+" millibeats to next beat "+milliBeatsToNextBeat.toString());

        Double secondsToNextBeat = milliBeatsToNextBeat*0.0864;
        Log.d(TAG,"getSecondsToNextBeat - next beat at "+secondsToNextBeat);
        return secondsToNextBeat;
    }

    public void draw(Canvas canvas, Rect bounds,boolean wakeLockHeld) {

        //Log.d(TAG,"Start drawing watchface");
        time.setToNow();
        double beats=getBeats();
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);

        if (worldMapBitmap!=null) {
            Bitmap mBackgroundScaledBitmap = Bitmap.createScaledBitmap(worldMapBitmap,bounds.width(), (int)(bounds.height()/1.50), true);
            canvas.drawBitmap(mBackgroundScaledBitmap, 0, (int)(bounds.centerY()/2.50), null);
        }

        String timeText = String.format(java.util.Locale.ENGLISH,shouldShowSeconds ? TIME_FORMAT_WITH_SECONDS : TIME_FORMAT_WITHOUT_SECONDS, time.hour, time.minute, time.second);
        float timeXOffset = computeXOffset(timeText, timePaint, bounds);
        float timeYOffset = computeTimeYOffset(timeText, timePaint, bounds);
        canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint);

        String beatTimeText;
        if (shouldShowSeconds)
            beatTimeText=String.format(java.util.Locale.ENGLISH,"@         %.2f",beats);
        else {
            beatTimeText = String.format(java.util.Locale.ENGLISH, "@            %d", (int) beats);
            if (!wakeLockHeld)
               beatTimeText+=" *";
        }

        float beatTimeXOffset = computeXOffset(beatTimeText, beatTimePaint, bounds);
        float beatTimeYOffset = computeDateYOffset(beatTimeText, beatTimePaint);
        canvas.drawText(beatTimeText, beatTimeXOffset, timeYOffset + beatTimeYOffset+10, beatTimePaint);

        // Shows internet beat time date
        if (this.isShowInternetBeatTimeDate()) {
            String beatDateText="@      "+DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format( utc.toDate());

            /*if (this.dateFormat!=null) {
                Date data = utc.toDate();
                beatDateText="@      "+dateFormat.format(data);
            }
            else {
                beatDateText="@      "+utc.dayOfMonth().get()+"."+utc.monthOfYear().get()+"."+utc.year().get();
            }*/
            float beatDateXOffset = computeXOffset(beatDateText, beatTimePaint, bounds);
            canvas.drawText(beatDateText, beatDateXOffset, timeYOffset + beatTimeYOffset + computeDateYOffset(beatTimeText, dateOnlyPaint) + 10, beatTimePaint);
        }

        String dateOnlyText;
        /*if (this.dateFormat!=null)
        {
            Date data = new Date(time.toMillis(false));
            dateOnlyText= dateFormat.format(data);
        }
        else
        {
            dateOnlyText = String.format(java.util.Locale.ENGLISH,DATE_FORMAT, time.monthDay, (time.month + 1), time.year);
        }*/
        dateOnlyText = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format( new Date(time.toMillis(false)));

        float dateOnlyXOffset = computeXOffset(dateOnlyText, dateOnlyPaint, bounds);

        if (worldMapBitmap!=null)
            canvas.drawText(dateOnlyText, dateOnlyXOffset, timeYOffset / 3, dateOnlyPaint);
        else
            canvas.drawText(dateOnlyText, dateOnlyXOffset, timeYOffset / 2, dateOnlyPaint);
        //Log.d(TAG,"End drawing watchface");
    }

    private float computeXOffset(String text, Paint paint, Rect watchBounds) {
        float centerX = watchBounds.exactCenterX();
        float timeLength = paint.measureText(text);
        return centerX - (timeLength / 2.0f);
    }

    private float computeTimeYOffset(String timeText, Paint timePaint, Rect watchBounds) {
        float centerY = watchBounds.exactCenterY();
        Rect textBounds = new Rect();
        timePaint.getTextBounds(timeText, 0, timeText.length(), textBounds);
        int textHeight = textBounds.height();
        return centerY + (textHeight / 2.0f);
    }

    private float computeDateYOffset(String dateText, Paint datePaint) {
        Rect textBounds = new Rect();
        datePaint.getTextBounds(dateText, 0, dateText.length(), textBounds);
        return textBounds.height() + 10.0f;
    }

    public void setAntiAlias(boolean antiAlias) {
        timePaint.setAntiAlias(antiAlias);
        beatTimePaint.setAntiAlias(antiAlias);
        dateOnlyPaint.setAntiAlias(antiAlias);
    }

    public void updateDateAndTimeColourTo(int colour) {
        dateAndTimeColour = colour;
        timePaint.setColor(colour);
        beatTimePaint.setColor(colour);
        dateOnlyPaint.setColor(colour);
    }

    public void updateTimeZoneWith(String timeZone) {
        time.clear(timeZone);
        time.setToNow();
    }

    public void setShowSeconds(boolean showSeconds) {
        shouldShowSeconds = showSeconds;
    }

    public void updateBackgroundColourTo(int colour) {
        backgroundColour = colour;
        backgroundPaint.setColor(colour);
    }

    public void restoreBackgroundColour() {
        backgroundPaint.setColor(backgroundColour);
    }

    public void updateBackgroundColourToDefault() {
        backgroundPaint.setColor(BACKGROUND_DEFAULT_COLOUR);
    }

    public void updateDateAndTimeColourToDefault() {
        timePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        beatTimePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        dateOnlyPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
    }

    public void restoreDateAndTimeColour() {
        timePaint.setColor(dateAndTimeColour);
        beatTimePaint.setColor(dateAndTimeColour);
        dateOnlyPaint.setColor(dateAndTimeColour);
    }

    public void setWorldBitmap(Bitmap bitmap) {
        this.worldMapBitmap=bitmap;
    }

    public void setWorldMapBitmapIfNecessary(WorldMapMaker maker,WorldMapAndroidGraphicsDraw image) {
        if (maker==null || image==null)
            return ;
        final long timeWorldMapRefresh=60*15; // World Map Background must be updated every 15 min
        long timeAfterLastUpdate = System.currentTimeMillis() / 1000L-lastBackgroundUpdateTimeStamp;
        if (timeAfterLastUpdate>timeWorldMapRefresh) {
            lastBackgroundUpdateTimeStamp = System.currentTimeMillis() / 1000L;

            // Async calculation of the background World Map
            new WorldMapBackgroundUpdater(maker,image).execute();

        }
    }

    private class WorldMapBackgroundUpdater extends AsyncTask<String, Void, Bitmap> {

        final private WorldMapMaker maker;
        final private WorldMapAndroidGraphicsDraw image;

        public WorldMapBackgroundUpdater(WorldMapMaker maker,com.ozzyboshi.worldmap.androidgraphics.WorldMapAndroidGraphicsDraw image) {
            this.maker=maker;
            this.image=image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            maker.BuildMapFromUnixTimestamp(System.currentTimeMillis()/1000L);
            return image.getDestination();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            setWorldBitmap(result);
        }
    }
}
