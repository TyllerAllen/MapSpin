package com.wlu.cp470.group12.mapspin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MapsActivity extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback, CircleImageView.OnSpinStartListener, CircleImageView.OnSpinEndListener {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    AlertDialog dialog;

    //private Spinner spinner;
    //private Button btnListPlaces;
    Button applyFilterButton;
    private ArrayList<Place> places;
    private ArrayList<Place> preloadedPlaces;

    //Bottom Settings Bar Content
    Spinner parentOptionSpinner;
    ArrayAdapter<String> parentOptionAdapter;
    ArrayList<String> parentOptionList;

    Spinner refineOptionSpinner;
    ArrayAdapter<String> refineOptionAdapter;
    ArrayList<String> refineOptionList;

    Map<String, String> tryingToDoMap = Place.createTryingToDoMap();
    Map<String, String> eatOptionsMap = Place.createEatOptionsMap();
    Map<String, String> doSomethingMap = Place.createDoSomethingMap();
    Map<String, String> partyMap = Place.createPartyMap();
    Map<String, String> relaxMap = Place.createRelaxMap();
    ArrayList<String> optionsKeys = new ArrayList<String>(Arrays.asList("Eat Something", "Do Something", "Party Somewhere", "Relax"));
    Map<String, Map> selectedMap = new HashMap<String, Map>() {{
        put("Eat Something", eatOptionsMap);
        put("Do Something", doSomethingMap);
        put("Party Somewhere", partyMap);
        put("Relax", relaxMap);
    }};

    private String queryType;
    ArrayList<String> pricesOptions = new ArrayList<>(Arrays.asList("Any", "Free", "$", "$$", "$$$", "$$$$"));
    ArrayList<String> ratingsOptions = new ArrayList<>(Arrays.asList("Any", "1", "2", "3", "4", "5"));
    private IntervalSeekBar priceSeekBar = null;
    private IntervalSeekBar ratingSeekBar = null;
    private TextView activePriceText = null;
    private TextView activeRatingText = null;
    private String activePriceFilter = "Any";
    private String activeRatingFilter = "Any";

    private SQLiteDatabase database;
    private BlacklistDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dbHelper = new BlacklistDBHelper(this);
        database = dbHelper.getWritableDatabase();

        CircleImageView circle = findViewById(R.id.green_circle);
        circle.setOnStartSpinListener(this);
        circle.setOnSpinEndListener(this);
        StaticMapFragment mapFragment = (StaticMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String colour_pref = prefs.getString("pref_colour_scheme", "");

        mapFragment.getMapAsync(map -> {
            mMap = map;

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            if (colour_pref.equals("Light")) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.light_style));
            } else if (colour_pref.equals("Dark")) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.dark_style));
            } else{
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
            }

            getLocationPermission();

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
        });

        preloadedPlaces = new ArrayList<>();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preloadPlaces("https://maps.googleapis.com/maps/api/place/textsearch/json?input=bar&inputtype=textquery&fields=photos,formatted_address,name,opening_hours,rating&location=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "&radius=15000&key=AIzaSyBo0Z7WWYJ6CzTUNm5t6gfqPpzwST8d2ME", "bar");
            }
        }, 1000);//need to wait 2 seconds before requesting next page token

        //  preloadPlaces(url);//replace with place type
        // spinner = (Spinner) findViewById(R.id.spinner);
        FrameLayout bottomSheetLayout = (FrameLayout) findViewById(R.id.bottom_container);
        // LinearLayout bottomSheetContent = (LinearLayout) findViewById(R.id.bottom_sheet_content);
        View child = getLayoutInflater().inflate(R.layout.options_bottom_content, null);
        bottomSheetLayout.addView(child);

        parentOptionSpinner = (Spinner) findViewById(R.id.placeTypeDropdown);
        refineOptionSpinner = (Spinner) findViewById(R.id.refinedPlaceTypeDropdown);

        parentOptionList = new ArrayList<String>(tryingToDoMap.keySet());
        refineOptionList = new ArrayList<String>(selectedMap.get(optionsKeys.get(0)).keySet());
        parentOptionAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                optionsKeys
        );

        refineOptionAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                refineOptionList
        );

        parentOptionSpinner.setAdapter(parentOptionAdapter);
        refineOptionSpinner.setAdapter(refineOptionAdapter);

        int basePos = refineOptionAdapter.getPosition("Any");
        refineOptionSpinner.setSelection(basePos);

        parentOptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> spinnerOptions = new ArrayList<>(selectedMap.get(optionsKeys.get(position)).keySet());
                Collections.sort(spinnerOptions);
                refineOptionAdapter.clear();
                refineOptionAdapter.addAll(spinnerOptions);
                refineOptionAdapter.notifyDataSetChanged();
                int basePos = refineOptionAdapter.getPosition("Any");
                refineOptionSpinner.setSelection(basePos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        parentOptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> spinnerOptions = new ArrayList<>(selectedMap.get(optionsKeys.get(position)).keySet());
                Collections.sort(spinnerOptions);
                refineOptionAdapter.clear();
                refineOptionAdapter.addAll(spinnerOptions);
                refineOptionAdapter.notifyDataSetChanged();
                int basePos = refineOptionAdapter.getPosition("Any");
                refineOptionSpinner.setSelection(basePos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        activePriceText = (TextView) findViewById(R.id.activePriceLevel);
        activeRatingText = (TextView) findViewById(R.id.activeRatingLevel);

        getIntervalSeekBar(R.id.priceSeekBarWithIntervals).setIntervals(pricesOptions);
        getIntervalSeekBar(R.id.ratingSeekBarWithIntervals).setIntervals(ratingsOptions);

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                activePriceText.setText(pricesOptions.get(progress));
                activePriceFilter = pricesOptions.get(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                activeRatingText.setText(ratingsOptions.get(progress));
                activeRatingFilter = pricesOptions.get(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        applyFilterButton = (Button) findViewById(R.id.applyFilters);
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MAIN_ACTIVITY: Price", getActivePriceFilter());
                Log.d("Main_acitivty: rating", getActiveRatingFilter());
                Log.d("Main_activity: types", getActiveTypesFilter().toString());

                preloadedPlaces = new ArrayList<>();
                preloadPlaces("https://maps.googleapis.com/maps/api/place/textsearch/json?query="+getActiveTypesFilter()+"&inputtype=textquery&fields=photos,formatted_address,name,opening_hours,rating&location=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "&radius=15000&key=AIzaSyBo0Z7WWYJ6CzTUNm5t6gfqPpzwST8d2ME", "bar");
            }
        });
        Button btnListPlaces = (Button) findViewById(R.id.btn_list_places);

        btnListPlaces.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "User clicked List Places");
                if (places != null) {
                    Intent intent = new Intent(MapsActivity.this, ListPlacesActivity.class);
                    intent.putParcelableArrayListExtra("PlaceList", MapsActivity.this.places);
                    startActivityForResult(intent, 10);
                }
                else{
                    Toast toast = Toast.makeText(MapsActivity.this, R.string.spin_prompt, Toast.LENGTH_LONG); //this is the ListActivity
                    toast.show();
                }
            }

        });
    }

    private IntervalSeekBar getIntervalSeekBar(int id) {
        IntervalSeekBar seekbar;
        if (id == R.id.ratingSeekBarWithIntervals) {
            if (ratingSeekBar == null) {
                seekbar = (IntervalSeekBar) findViewById(id);
                ratingSeekBar = seekbar;
            } else {
                seekbar = ratingSeekBar;
            }
        } else {
            if (priceSeekBar == null) {
                seekbar = (IntervalSeekBar) findViewById(id);
                priceSeekBar = seekbar;
            } else {
                seekbar = priceSeekBar;
            }
        }
        return seekbar;
    }

    public String getActiveRatingFilter() {
        return activeRatingFilter;
    }

    public String getActiveTypesFilter() {
        /*
        Returns a List of what to filter by
         */
        if (refineOptionSpinner.getSelectedItem().equals("Any")) {
            List<String> keys = new ArrayList<>(selectedMap.get(parentOptionSpinner.getSelectedItem()).values());
            //keys.remove("any");

           // Log.i(TAG, parentOptionSpinner.getSelectedItem().toString());
            //return keys;
            if (parentOptionSpinner.getSelectedItem().toString().equals("Eat Something")){
                return ("food");
            } else if (parentOptionSpinner.getSelectedItem().toString().equals("Do Something")){
                return ("activity");
            } else if (parentOptionSpinner.getSelectedItem().toString().equals("Party Somewhere")){
                return ("drink");
            } else if (parentOptionSpinner.getSelectedItem().toString().equals("Relax")){
                return ("relax");
            }
            return parentOptionSpinner.getSelectedItem().toString();
        } else {
            return (refineOptionSpinner.getSelectedItem().toString());
        }
    }

    public String getActivePriceFilter() {
        if(activePriceFilter != null) {
            if (!activePriceFilter.equals("Any")) {
                StringBuilder sb = new StringBuilder();
                sb.append("&minprice=");
                int activePriceInteger = pricesOptions.indexOf(activePriceFilter) - 1;
                sb.append(activePriceInteger);
                return sb.toString();
            } else {
                return "";
            }
        }else{
            return "";
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);

                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private Place currentPlace;


    @Override
    public CompletableFuture<Float> startSpin() {
        mMap.clear();
        currentPlace = null;
        float zoom = mMap.getCameraPosition().zoom;
        Log.i("Spin Time", String.valueOf(zoom));
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        double llNeLat = bounds.northeast.latitude;
        double llNwLat = bounds.northeast.latitude;
        double llNeLng = bounds.northeast.longitude;
        double llNwLng = bounds.southwest.longitude;
        float[] distanceResults = new float[5];
        Location.distanceBetween(llNeLat, llNeLng, llNwLat, llNwLng, distanceResults);
        Log.i("Position", mMap.getCameraPosition().toString());
        final double lat = mMap.getCameraPosition().target.latitude;
        final double lng = mMap.getCameraPosition().target.longitude;
        final double radius = distanceResults[0] * 0.85523809524;
        String max_price = getActivePriceFilter();
        Log.i("Max Price", max_price);
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="+getActiveTypesFilter()+"&inputtype=textquery&fields=photos,formatted_address,name,opening_hours,rating&location=" + lat + "," + lng + "&radius=" + radius + "&key=AIzaSyBo0Z7WWYJ6CzTUNm5t6gfqPpzwST8d2ME";
        Log.i("Radius", url);
        RequestQueue queue = Volley.newRequestQueue(this);
        CompletableFuture<Float> result = new CompletableFuture<>();

        places = new ArrayList<>();
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    //Log.i("Response:", response);
                    JSONObject results = null;
                    try {
                        String pFilter;
                        String rFilter;
                        results = new JSONObject(response);
                        JSONArray placesJSON = results.getJSONArray("results");
                        JSONObject placeJSON;
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        Point center = new Point(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2);
                        for (int i = 0; i < placesJSON.length(); i++) {
                            placeJSON = placesJSON.getJSONObject(i);
                            //place object has all details for place
                            Place place = new Place(placeJSON);
                            Point screenLocation = mMap.getProjection().toScreenLocation(new LatLng(place.lat, place.lang));
                            double dist = Math.sqrt(Math.pow((center.x - screenLocation.x), 2) + Math.pow((center.y - screenLocation.y), 2));
                            rFilter=getActiveRatingFilter();
                            pFilter=getActivePriceFilter();
                            Log.i(TAG, "Price filter: " + getActivePriceFilter());
                            if (pFilter.equals("")){
                                pFilter="100";
                            }
                            if (rFilter.equals("Any")){
                                rFilter="0";
                            }
                            Log.i(TAG, "rating filter: " + getActiveRatingFilter());
                            if (dist < displayMetrics.widthPixels / 2.0 && place.rating>=Double.parseDouble(rFilter) && place.price_level<=Double.parseDouble(pFilter)) {
                                places.add(place);
                            }

                        }
                        Place preloaded;
                        double dist;
                        Boolean duplicate=false;
                        for (int i = 0; i < preloadedPlaces.size(); i++) {
                            preloaded = preloadedPlaces.get(i);
                            duplicate=false;
                            for (int j = 0; j < places.size(); j++) {
                                if ((places.get(j).place_id).equals(preloaded.place_id)){
                                    duplicate=true;
                                }

                            }
                            //place object has all details for place
                            if (duplicate==false) {
                                Point screenLocation = mMap.getProjection().toScreenLocation(new LatLng(preloaded.lat, preloaded.lang));
                                dist = Math.sqrt(Math.pow((center.x - screenLocation.x), 2) + Math.pow((center.y - screenLocation.y), 2));
                                if (dist < displayMetrics.widthPixels / 2.0) {
                                    places.add(preloaded);
                                }
                            }

                        }

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
                            for(int i = 0; i < places.size(); i++){
                                if (places.get(i).id.equals(cursor.getString(cursor.getColumnIndex(BlacklistDBHelper.COLUMN_ID)))) {
                                    Log.i(TAG, "Removed: " + places.get(i).name + " from selection");
                                    places.remove(i);
                                }
                            }
                            //Log.i(TAG, "Reading: " + ids.get(ids.size() - 1));
                            cursor.moveToNext();
                        }
                        cursor.close();

                        if (places.size() > 0) {

                            Place randomPlace = places.get((int) (Math.random() * ((places.size() - 1) + 1)));
                            for (int j = 0; j < randomPlace.types.length; j++) {
                                Log.i("Place", randomPlace.types[j]);
                            }
                            currentPlace = randomPlace;

                            Location camera = new Location("");
                            camera.setLatitude(mMap.getCameraPosition().target.latitude);
                            camera.setLongitude(mMap.getCameraPosition().target.longitude);
                            Location place = new Location("");
                            place.setLatitude(currentPlace.lat);
                            place.setLongitude(currentPlace.lang);
                            float angle = camera.bearingTo(place) - mMap.getCameraPosition().bearing;
                            result.complete(angle);
                        } else {
                            currentPlace=null;
                            result.complete(new Float(0));
                            Toast.makeText(this, "No Place in Range Found", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        Log.e("JSON stuff", e.toString());
                    }
                    if (results != null) {
                    } else {
                        Log.i("calling places????", "uh oh");
                    }

                }, error -> {
            //textView.setText("That didn't work!");
        });

        Toast.makeText(this, "Loading places...", Toast.LENGTH_SHORT).show();
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return result;
    }

    @Override
    public void spinEnd() {
        Toast.makeText(this, currentPlace == null ? "No place in range found" : currentPlace.name, Toast.LENGTH_SHORT).show();
        if (currentPlace != null) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentPlace.lat, currentPlace.lang)).title(currentPlace.name));
            marker.showInfoWindow();

            //get image
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=3000&photoreference=" + currentPlace.photo_reference + "&key=AIzaSyBo0Z7WWYJ6CzTUNm5t6gfqPpzwST8d2ME";
            RequestQueue queue = Volley.newRequestQueue(this);
            final Bitmap image;
            ImageRequest imageRequest = new ImageRequest(url, response ->
            { // Bitmap listener
                setImage(response);
            },
                    0, // Image width
                    0, // Image height
                    ImageView.ScaleType.CENTER_CROP, // Image scale type
                    Bitmap.Config.RGB_565, //Image decode configuration
                    new Response.ErrorListener() { // Error listener
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Do something with error response
                            error.printStackTrace();
                            //Snackbar.make(mCLayout,"Error", Snackbar.LENGTH_LONG).show();
                        }
                    }
            );
            queue.add(imageRequest);

            // Get the layout inflater
            LayoutInflater inflater = this.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setView(inflater.inflate(R.layout.result_dialog, null));
            dialog = builder.create();


            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.show();

            Button btnDirections = (Button) dialog.findViewById(R.id.directions);

            Button btnBlacklist = (Button) dialog.findViewById(R.id.blacklist);

            Button btnSpinAgain = (Button) dialog.findViewById(R.id.spin_again);

            btnDirections.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + currentPlace.lat + "," + currentPlace.lang);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }
            });

            btnBlacklist.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.hide();
                    ContentValues values = new ContentValues();
                    values.put(BlacklistDBHelper.COLUMN_ID, currentPlace.id);
                    values.put(BlacklistDBHelper.COLUMN_NAME, currentPlace.name);
                    values.put(BlacklistDBHelper.COLUMN_LAT, currentPlace.lat);
                    values.put(BlacklistDBHelper.COLUMN_LANG, currentPlace.lang);
                    database.insert(BlacklistDBHelper.TABLE_MESSAGES, null, values);

                }
            });

            btnSpinAgain.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.hide();
                    // btnAdd2 has been clicked

                }
            });

            TextView name = (TextView) dialog.findViewById(R.id.textName);
            name.setText(currentPlace.name);
            double m = distanceLatLngMeters(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), currentPlace.lat, currentPlace.lang);
            TextView distance = (TextView) dialog.findViewById(R.id.textDistance);
            if (m >= 1000) {
                distance.setText((new Double(Math.round(m / 100))) / 10.0 + " km");
            } else {
                distance.setText(Math.round(m) + " m");
            }

            TextView stars = (TextView) dialog.findViewById(R.id.textStars);
            stars.setText(currentPlace.rating + " Stars    " + new String(new char[currentPlace.price_level]).replace("\0", "$"));

        }
    }

    private void setImage(Bitmap image) {
        ImageView dialog_image = (ImageView) dialog.findViewById(R.id.dialog_image);
        dialog_image.setImageBitmap(image);
    }

    private double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    private double distanceLatLngMeters(double lat1, double lng1, double lat2, double lng2) {
        int earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLng = degreesToRadians(lng2 - lng1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c * 1000;
    }

    private void preloadPlaces(String url, String type) {
        this.queryType=type;
        String localType=this.queryType;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    //Log.i("Response:", response);
                    JSONObject results = null;
                    try {
                        results = new JSONObject(response);
                        Log.i("results", results.toString());
                        JSONArray placesJSON = results.getJSONArray("results");

                        JSONObject placeJSON;
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        Point center = new Point(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2);
                        for (int i = 0; i < placesJSON.length(); i++) {
                            placeJSON = placesJSON.getJSONObject(i);
                            //place object has all details for place
                            Place place = new Place(placeJSON);
                            Point screenLocation = mMap.getProjection().toScreenLocation(new LatLng(place.lat, place.lang));
                            double dist = Math.sqrt(Math.pow((center.x - screenLocation.x), 2) + Math.pow((center.y - screenLocation.y), 2));

                            preloadedPlaces.add(place);


                        }
                        Toast.makeText(this, "preloaded places: " + preloadedPlaces.size(), Toast.LENGTH_SHORT).show();
                        try {
                            String token = results.getString("next_page_token");
                            String tokenUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?pagetoken=" + token + "&key=AIzaSyBo0Z7WWYJ6CzTUNm5t6gfqPpzwST8d2ME";
                            final Handler handler2 = new Handler();
                            handler2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("calling places????", "bout to call, waited 2 seconds");
                                    if (localType.equals(queryType)) {
                                        preloadPlaces(tokenUrl, localType);//we love recursion
                                    }
                                }
                            }, 2001);//need to wait 2 seconds before requesting next page token
                        } catch (JSONException e) {

                        }


                    } catch (JSONException e) {
                        Log.e("JSON stuff", e.toString());
                    }
                    if (results != null) {
                    } else {
                        Log.i("calling places????", "uh oh");
                    }

                }, error -> {
            //textView.setText("That didn't work!");
        });
        queue.add(stringRequest);
        return;
    }

    @Override
    public void onResume() {
        super.onResume();
        StaticMapFragment mapFragment = (StaticMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String colour_pref = prefs.getString("pref_colour_scheme", "");

        mapFragment.getMapAsync(map -> {
            mMap = map;

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            if (colour_pref.equals("Light")) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.light_style));
            } else if (colour_pref.equals("Dark")) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.dark_style));
            } else{
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
            }

            getLocationPermission();

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
        });
    }
}

