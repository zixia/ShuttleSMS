package com.akamobi.shuttlesms.db;


import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CreditDb {
    public static final String KEY_ROWID 	= "_id";
    public static final String KEY_TYPE		= "type";
    public static final String KEY_WORD 	= "keyword";
    public static final String KEY_NOTE 	= "note";
    public static final String KEY_ENABLED 	= "enabled";
    public static final String KEY_COUNTER 	= "counter";
    public static final String KEY_TIME 	= "timestamp";
    
    public static final int TYPE_WHITENUM	= 1;
    public static final int TYPE_BLACKNUM	= 2;
    public static final int TYPE_WHITEKEY	= 3;
    public static final int TYPE_BLACKKEY	= 4;

    public enum CreditResult { BAD, GOOD };
    
    private static final String TAG = "CreditDbAdapter";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table credit (_id integer primary key autoincrement, "
        + "type INTEGER default " + TYPE_WHITENUM + ", "
        + "keyword TEXT NOT NULL, " + " "
        + "enabled INTEGER default 1, "
        + "note TEXT default '', "
        + "timestamp INTEGER DEFAULT 0, "
        + "counter INTEGER DEFAULT 0 "
        + "); ";

    private static final String DATABASE_NAME = "ShuttleSMS_DB";
    private static final String DATABASE_TABLE = "credit";
    private static final int DATABASE_VERSION = 1;

    private Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public CreditDb(Context ctx) {
        this.mCtx = ctx;
		this.open();
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public CreditDb open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param type the title of the note
     * @param keyWord the body of the note
     * @param note
     * @param enabled
     * @return rowId or -1 if failed
     */
    public long createCredit(int type, String keyWord, String note) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_WORD, keyWord);
        initialValues.put(KEY_NOTE, note);
        initialValues.put(KEY_ENABLED, 1);
        initialValues.put(KEY_TIME, new Date().getTime());

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCredit(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllCredits() {
    	String[] columns = new String[] {
    			KEY_ROWID
    			, KEY_TYPE
    			, KEY_WORD
    	};
    	
        return mDb.query(DATABASE_TABLE, columns, null, null, null, null, KEY_TIME + " desc");
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCredit(long rowId) throws SQLException {
    	String[] columns = {
    			KEY_ROWID
    			, KEY_TYPE
    			, KEY_WORD
    			, KEY_ENABLED
    			, KEY_NOTE
    			, KEY_TIME
    			, KEY_COUNTER
    	};
    	
        Cursor cursor =
            mDb.query(true, DATABASE_TABLE, columns
            		, KEY_ROWID + "=" + rowId
            		, null, null, null, null, null);
        
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCredit(long rowId, int type, String keyWord, String note) {
        ContentValues args = new ContentValues();
        args.put(KEY_TYPE, type);
        args.put(KEY_WORD, keyWord);
        args.put(KEY_NOTE, note);

        args.put(KEY_TIME, new Date().getTime());

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor queryNumber(String phoneNumber) throws SQLException {
    	String[] columns = {
    			KEY_ROWID
    			, KEY_TYPE
    			, KEY_ENABLED
    	};
    	
        Cursor cursor =
            mDb.query(true, DATABASE_TABLE, columns
            		, KEY_WORD + "='" + phoneNumber + "'"
            		, null, null, null, null, null);
        
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
} 