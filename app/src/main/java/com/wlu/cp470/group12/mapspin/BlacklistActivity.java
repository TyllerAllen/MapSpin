package com.wlu.cp470.group12.mapspin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class BlacklistActivity extends AppCompatActivity {
    private static final String TAG = "BlacklistActivity";

    private SQLiteDatabase database;
    private BlacklistDBHelper dbHelper;

    ListView listView;
    Button btnBack;
    Button btnReset;
    ArrayList<String> names;
    ArrayList<String> ids;
    BlacklistActivity.PlaceAdapter nameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_places);

        listView = (ListView) findViewById(R.id.lv_list_places);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnReset = (Button) findViewById(R.id.btn_reset_db);
        names = new ArrayList<String>();

        nameAdapter = new BlacklistActivity.PlaceAdapter(this);
        listView.setAdapter(nameAdapter);

        dbHelper = new BlacklistDBHelper(this);
        database = dbHelper.getWritableDatabase();

        updateIds();
        nameAdapter.notifyDataSetChanged();

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BlacklistActivity.this);
                builder.setMessage(R.string.warning_dialog_message)
                        .setTitle(R.string.warning_dialog_title)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                database.delete(BlacklistDBHelper.TABLE_MESSAGES, null, null);
                                nameAdapter.notifyDataSetChanged();

                                Snackbar.make((RelativeLayout) findViewById(R.id.act_list_places), R.string.blacklist_reset, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null)
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //return
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbHelper.close();
    }

    private class PlaceAdapter extends ArrayAdapter<String> {
        public PlaceAdapter(Context ctx){
            super(ctx, 0);
        }

        public int getCount(){
            return names.size();
        }

        public String getItem(int position){
            return names.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = BlacklistActivity.this.getLayoutInflater();
            View result = null;

            updateIds();

            result = inflater.inflate(R.layout.place_row_dark, null);

            ImageButton btnWhitelist = (ImageButton) result.findViewById(R.id.img_btn_whitelist);

            btnWhitelist.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    database.delete(BlacklistDBHelper.TABLE_MESSAGES, BlacklistDBHelper.COLUMN_ID + " = '" + ids.get(position) + "'", null);
                    updateIds();
                    notifyDataSetChanged();
                }
            });


            TextView name = (TextView)result.findViewById(R.id.place_name);
            if (getCount() > 0)
                name.setText(getItem(position));

            return result;
        }
    }

    private void updateIds(){
        ids = new ArrayList<String>();
        names = new ArrayList<String>();
        Cursor cursor = database.query(false,
                BlacklistDBHelper.TABLE_MESSAGES,
                new String[] {BlacklistDBHelper.COLUMN_ID + ", " + BlacklistDBHelper.COLUMN_NAME},
                BlacklistDBHelper.COLUMN_ID + " not null",
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            ids.add(cursor.getString(cursor.getColumnIndex(BlacklistDBHelper.COLUMN_ID)));
            names.add(cursor.getString(cursor.getColumnIndex(BlacklistDBHelper.COLUMN_NAME)));
            //Log.i(TAG, "Reading: " + ids.get(ids.size() - 1));
            cursor.moveToNext();
        }
        cursor.close();
    }
}
