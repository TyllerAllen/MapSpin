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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListPlacesActivity extends AppCompatActivity {
    private static final String TAG = "ListPlacesActivity";

    private SQLiteDatabase database;
    private BlacklistDBHelper dbHelper;

    ListView listView;
    Button btnBack;
    Button btnReset;
    ArrayList<Place> places;
    ArrayList<String> names;
    ArrayList<String> ids;
    ProgressBar pbIds;
    PlaceAdapter nameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_places);

        listView = (ListView) findViewById(R.id.lv_list_places);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnReset = (Button) findViewById(R.id.btn_reset_db);
        names = new ArrayList<String>();
        pbIds = (ProgressBar) findViewById(R.id.pb_ids);

        Intent intent = getIntent();
        places = intent.getParcelableArrayListExtra("PlaceList");

        nameAdapter = new PlaceAdapter(this);
        listView.setAdapter(nameAdapter);

        dbHelper = new BlacklistDBHelper(this);
        database = dbHelper.getWritableDatabase();

        updateIds();

        for(int i = 0; i < places.size(); i++){
            String pname = places.get(i).name;
            names.add(pname);
            nameAdapter.notifyDataSetChanged();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListPlacesActivity.this);
                builder.setMessage(R.string.warning_dialog_message)
                        .setTitle(R.string.warning_dialog_title)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pbIds.setVisibility(View.VISIBLE);
                                //forceClean();
                                ClearTable ct = new ClearTable();
                                ct.execute();
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
            LayoutInflater inflater = ListPlacesActivity.this.getLayoutInflater();
            View result = null;
            Place p = places.get(position);

            updateIds();

            if(!ids.contains(p.id)){
                result = inflater.inflate(R.layout.place_row_light, null);

                ImageButton btnBlacklist = (ImageButton) result.findViewById(R.id.img_btn_blacklist);

                btnBlacklist.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        ContentValues values = new ContentValues();
                        values.put(BlacklistDBHelper.COLUMN_ID, p.id);
                        values.put(BlacklistDBHelper.COLUMN_NAME, p.name);
                        values.put(BlacklistDBHelper.COLUMN_LAT, p.lat);
                        values.put(BlacklistDBHelper.COLUMN_LANG, p.lang);
                        database.insert(BlacklistDBHelper.TABLE_MESSAGES, null, values);
                        Log.i(TAG, "\nInserted ID: " + p.id + "\nName: " + p.name + "\nLat: " + p.lat + "\nLong: " + p.lang);
                        notifyDataSetChanged();
                    }
                });
            }
            else{
                result = inflater.inflate(R.layout.place_row_dark, null);

                ImageButton btnWhitelist = (ImageButton) result.findViewById(R.id.img_btn_whitelist);

                btnWhitelist.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        database.delete(BlacklistDBHelper.TABLE_MESSAGES, BlacklistDBHelper.COLUMN_ID + " = '" + p.id + "'", null);
                        Log.i(TAG, "\nRemoved ID: " + p.id + "\nName: " + p.name + "\nLat: " + p.lat + "\nLong: " + p.lang);
                        notifyDataSetChanged();
                    }
                });
            }

            TextView name = (TextView)result.findViewById(R.id.place_name);
            name.setText(getItem(position));

            return result;
        }
    }

    private void updateIds(){
        ids = new ArrayList<String>();
        Cursor cursor = database.query(false,
                BlacklistDBHelper.TABLE_MESSAGES,
                new String[] {BlacklistDBHelper.COLUMN_ID},
                BlacklistDBHelper.COLUMN_ID + " not null",
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            ids.add(cursor.getString(cursor.getColumnIndex(BlacklistDBHelper.COLUMN_ID)));
            //Log.i(TAG, "Reading: " + ids.get(ids.size() - 1));
            cursor.moveToNext();
        }
        cursor.close();
    }

    private void forceClean(){
        database.delete(BlacklistDBHelper.TABLE_MESSAGES, null, null);
    }

    private class ClearTable extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... args){
            String id;
            int s = ids.size();

            for(int i = 0; i < s; i++){
                id = ids.get(i);
                Log.i(TAG, "\n" + id + ", " + s);
                database.delete(BlacklistDBHelper.TABLE_MESSAGES, BlacklistDBHelper.COLUMN_ID + "=?", new String[]{id});

                try{
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    Log.i(TAG, "Thread interrupted");
                }
                publishProgress(100 / ((s + 1) / (i + 1)));
            }

            return "";
        }

        @Override
        protected void onPostExecute(String a) {
            pbIds.setVisibility(View.INVISIBLE);
            nameAdapter.notifyDataSetChanged();

            Snackbar.make((RelativeLayout) findViewById(R.id.act_list_places), R.string.blacklist_reset, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            pbIds.setProgress(values[0]);
        }
    }
}
