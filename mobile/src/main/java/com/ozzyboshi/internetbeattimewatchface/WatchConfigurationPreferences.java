
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
import android.content.SharedPreferences;
import android.graphics.Color;

public class WatchConfigurationPreferences {

    private static final String NAME = "WatchConfigurationPreferences";
    private static final String KEY_BACKGROUND_COLOUR = NAME + ".KEY_BACKGROUND_COLOUR";
    private static final String KEY_DATE_TIME_COLOUR = NAME + ".KEY_DATE_TIME_COLOUR";
    private static final int DEFAULT_BACKGROUND_COLOUR = Color.parseColor("black");
    private static final int DEFAULT_DATE_TIME_COLOUR = Color.parseColor("white");
    private static final String KEY_WORDMAP_BACKGROUND = NAME+".WORLDMAP_BACKGROUND";
    private static final String KEY_AMBIENT_MODE_ACCURACY = NAME+".AMBIENT_MODE_ACCURACY";

    private final SharedPreferences preferences;

    public static WatchConfigurationPreferences newInstance(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return new WatchConfigurationPreferences(preferences);
    }

    private WatchConfigurationPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public int getBackgroundColour() {
        return preferences.getInt(KEY_BACKGROUND_COLOUR, DEFAULT_BACKGROUND_COLOUR);
    }

    public int getDateAndTimeColour() {
        return preferences.getInt(KEY_DATE_TIME_COLOUR, DEFAULT_DATE_TIME_COLOUR);
    }

    public void setBackgroundColour(int color) {
        preferences.edit().putInt(KEY_BACKGROUND_COLOUR, color).apply();
    }

    public void setDateAndTimeColour(int color) {
        preferences.edit().putInt(KEY_DATE_TIME_COLOUR, color).apply();
    }

    public void setWorldMapBackground(boolean mode) {
        preferences.edit().putBoolean(KEY_WORDMAP_BACKGROUND, mode).apply();
    }

    public boolean showWorldMapBackground() {
        return preferences.getBoolean(KEY_WORDMAP_BACKGROUND,false);
    }

    public boolean getAmbientModeAccuracy() {
        return preferences.getBoolean(KEY_AMBIENT_MODE_ACCURACY,false);
    }

    public void setKeyAmbientModeAccuracy(boolean mode) {
        preferences.edit().putBoolean(KEY_AMBIENT_MODE_ACCURACY,mode).apply();
    }
}
