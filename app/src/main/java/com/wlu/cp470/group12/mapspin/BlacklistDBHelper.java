package com.wlu.cp470.group12.mapspin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlacklistDBHelper extends SQLiteOpenHelper {

    //@Override
    //static String DATABASE_NAME;
    //static int VERSION_NUM;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LANG = "lang";

    private static final String DATABASE_NAME = "Places.db";
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_MESSAGES + " (" +
            COLUMN_ID + " text not null, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_LAT + " double not null, " +
            COLUMN_LANG + " double not null);";

    private static final String DATABASE_UPGRADE = "DROP TABLE IF EXISTS "+ TABLE_MESSAGES;

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
       // Log.i(ACTIVITY_NAME, "Called onCreate");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DATABASE_UPGRADE);
        onCreate(sqLiteDatabase);
       // Log.i(ACTIVITY_NAME, "Called onUpgrade");
    }

    public BlacklistDBHelper(Context ctx) {

        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

}
