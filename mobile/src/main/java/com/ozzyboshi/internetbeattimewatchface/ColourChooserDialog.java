
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.ozzyboshi.simpleandroidwatchface.R;

public class ColourChooserDialog extends DialogFragment {

    private static final String ARG_TITLE = "ARG_TITLE";
    private Listener colourSelectedListener;

    public static ColourChooserDialog newInstance(String dialogTitle) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, dialogTitle);
        ColourChooserDialog dialog = new ColourChooserDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        colourSelectedListener = (Listener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] colours = getResources().getStringArray(R.array.colors_array);
                        colourSelectedListener.onColourSelected(colours[which], getTag());
                    }
                });
        return builder.create();
    }

    interface Listener {
        void onColourSelected(String colour, String tag);
    }
}
