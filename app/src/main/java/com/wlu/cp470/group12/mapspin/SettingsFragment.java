package com.wlu.cp470.group12.mapspin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference about_pref = findPreference("pref_about");
        Preference help_pref= findPreference("pref_help");
        Preference toggle_pref = findPreference("pref_toggle_ani");
        Preference note_pref = findPreference("pref_notes");

        about_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), getContext().getString(R.string.about_info), Toast.LENGTH_LONG ).show();
                return true;
            }
        });

        help_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.help_dialog_message)
                        .setTitle(R.string.help_dialog_title);

                builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked OK button
                    }
                });

                // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        toggle_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Snackbar.make(getActivity().findViewById(R.id.settings), "Toggling animations...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                return true;
            }
        });

        note_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), NoteWindow.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
