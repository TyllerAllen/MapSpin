package com.wlu.cp470.group12.mapspin;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Place implements Parcelable {
    String name;
    String address;
    String id;
    String place_id;
    String icon;
    String photo_link;
    String photo_reference;
    String reference;
    int photo_height;
    int photo_width;
    int price_level;
    int user_ratings_total;
    Double lat;
    Double lang;
    Double rating;
    Boolean open_now;
    String[] types;


    public Place(String nameIn, String addressIn, String idIn, String place_idIn, String iconIn, String photo_linkIn, String photo_referenceIn, String referenceIn, int photo_heightIn, int photo_widthIn, int price_levelIn, int user_ratings_totalIn, Double latIn, Double langIn, Double ratingIn, Boolean open_nowIn, String[] typesIn) {
        name = nameIn;
        address = addressIn;
        id = idIn;
        place_id = place_idIn;
        icon = iconIn;
        photo_link = photo_linkIn;
        photo_reference = photo_referenceIn;
        reference = referenceIn;
        photo_height = photo_heightIn;
        photo_width = photo_widthIn;
        price_level = price_levelIn;
        user_ratings_total = user_ratings_totalIn;
        lat = latIn;
        lang = langIn;
        rating = ratingIn;
        open_now = open_nowIn;
        types = typesIn;
    }

    public Place(JSONObject placeJSON) {
        try {
            name = placeJSON.getString("name");
            address = placeJSON.getString("formatted_address");
            id = placeJSON.getString("id");
            place_id = placeJSON.getString("place_id");
            icon = placeJSON.getString("icon");
            photo_link = "";
            photo_reference = "";
            photo_height = 0;
            photo_width = 0;
            try {
                JSONArray photosJSON = placeJSON.getJSONArray("photos");
                JSONObject photoJSON = photosJSON.getJSONObject(0);
                JSONArray htmlJSON = photoJSON.getJSONArray("html_attributions");
                photo_link = htmlJSON.getString(0);
                photo_reference = photoJSON.getString("photo_reference");
                photo_height = photoJSON.getInt("height");
                photo_width = photoJSON.getInt("width");
            } catch (JSONException e) {

            }
            reference = placeJSON.getString("reference");
            price_level = 0;
            try {
                price_level = placeJSON.getInt("price_level");
            } catch (Exception e) {

            }
            user_ratings_total = placeJSON.getInt("user_ratings_total");
            JSONObject geometryJSON = placeJSON.getJSONObject("geometry");
            JSONObject locationJSON = geometryJSON.getJSONObject("location");
            lat = locationJSON.getDouble("lat");
            lang = locationJSON.getDouble("lng");
            rating = placeJSON.getDouble("rating");
            open_now = false;
            try {
                JSONObject openJSON = placeJSON.getJSONObject("opening_hours");
                open_now = openJSON.getBoolean("open_now");
            } catch (JSONException e) {

            }
            JSONArray typesJSON = placeJSON.getJSONArray("types");
            types = new String[typesJSON.length()];
            for (int j = 0; j < typesJSON.length(); j++) {
                types[j] = typesJSON.getString(j);
            }
        } catch (JSONException e) {

        }

    }

    public Place(Parcel source) {
        id = source.readString();
        name = source.readString();
        lat = source.readDouble();
        lang = source.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lang);

    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel source) {
            return new Place(source);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public static Map<String, String> createTryingToDoMap() {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Eat Something", "eat_options");
        myMap.put("Do Something", "do_something_options");
        myMap.put("Party Somewhere", "party_options");
        myMap.put("Relax", "relax_options");
        return myMap;
    }

    public static Map<String, String> createEatOptionsMap() {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Any", "any");
        myMap.put("Bakery", "bakery");
        myMap.put("Cafe", "cafe");
        myMap.put("Convenience Store", "convenience_store");
        myMap.put("Grocery Store", "grocery_or_supermarket");
        myMap.put("Delivery", "meal_delivery");
        myMap.put("Takeout", "meal_takeway");
        myMap.put("Restaurant", "restaurant");
        return myMap;
    }

    public static Map<String, String> createDoSomethingMap() {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Any", "any");
        myMap.put("Amusement Park", "amusement_park");
        myMap.put("Aquarium", "aquarium");
        myMap.put("Art Gallery", "art_gallery");
        myMap.put("Bowling", "bowling_alley");
        myMap.put("Movie Theatre", "movie_theater");
        myMap.put("Museum", "museum");
        myMap.put("Mall", "shopping_mall");
        myMap.put("Stadium", "stadium");
        myMap.put("Tour", "tourist_attraction");
        myMap.put("Zoo", "zoo");
        return myMap;
    }

    public static Map<String, String> createPartyMap() {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Any", "any");
        myMap.put("Bar", "bar");
        myMap.put("Casino", "casino");
        myMap.put("Liquor Store", "liquor_store");
        myMap.put("Night Club", "night_club");
        return myMap;
    }

    public static Map<String, String> createRelaxMap() {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Any", "any");
        myMap.put("Beauty Salon", "beauty_salon");
        myMap.put("Park", "park");
        myMap.put("Spa", "spa");
        return myMap;
    }

    public int compareTo(Place place) {
        return this.name.compareTo(place.name);
    }


}