package com.example.user.projectfinale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


//this class handles all functions relating to the database
public class userDBhandler extends SQLiteOpenHelper {

    //database variables with descriptions
    private static final int DATABASE_VERSION = 4; //the version the database is on
    private static final String DATABASE_NAME = "details.db"; //the name of the database where all details are stored
    public static final String TABLE_DETAILS = "details"; //the table name used to store details
    public static final String COLUMN_ID = "_id"; //the column for the ID for all entries in the database
    public static final String COLUMN_userDetails = "detailName"; //the column for storing user details
    public static final String COLUMN_currentSteps = "currSteps"; //the column used for storing the initial steps

    public userDBhandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating the table with columns
        String query = "CREATE TABLE " + TABLE_DETAILS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_userDetails + " TEXT, " +
                COLUMN_currentSteps + " TEXT);";
        db.execSQL(query);

        //setting up the currentSteps column with a default value, so that this can be updated easily
        ContentValues values = new ContentValues();
        values.put(COLUMN_currentSteps, "0");
        db.insert(TABLE_DETAILS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //if the db version is updated then delete table and run onCreate() again
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);
        onCreate(db);
    }

    //is used to add user details into the database
    public void addDetails(userDetails details) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_userDetails, details.getDetails());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_DETAILS, null, values);
        db.close();
    }

    //is used to update initial steps in the database
    public void addSteps(userDetails details) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_currentSteps, details.getDetails());
        SQLiteDatabase db = getWritableDatabase();
        String where = "rowid=(SELECT MIN(rowid) FROM " + TABLE_DETAILS + ")";
        db.update(TABLE_DETAILS, values, where, null);
        db.close();
    }

    //Deletes all details from database
    public void deleteDetails(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+TABLE_DETAILS);
        db.close();
    }

    //printing user details as a string array
    public String[] databaseToString(){
        String[] dbString = {"","","","","",""};
        int counter =0;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DETAILS + " WHERE 1";

        //cursor is going to point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //move to first row in results
        c.moveToFirst();

        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("detailName")) != null){
                dbString[counter] += c.getString(c.getColumnIndex("detailName"));
            }
            counter+=1;
            c.moveToNext();
        }
        db.close();
        return dbString;
    }

    //printing initial steps as a string array
    public String[] databaseToStringSteps(){
        String[] dbString = {"","","","","",""};
        int counter =0;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DETAILS + " WHERE 1";

        //cursor is going to point to a location in your results
        Cursor c = db.rawQuery(query, null);
        //move to first row in results
        c.moveToFirst();

        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("currSteps")) != null){
                dbString[counter] += c.getString(c.getColumnIndex("currSteps"));
            }
            counter+=1;
            c.moveToNext();
        }
        db.close();
        return dbString;
    }
}
