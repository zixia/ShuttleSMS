package com.akamobi.shuttlesms.db;


import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.akamobi.shuttlesms.engine.ChallengeResponse;

public class ChallengeResponseDb {
    public static final String KEY_ROWID 	= "_id";
    public static final String KEY_NUMBER	= "number";
    public static final String KEY_TIME 	= "timestamp";
    public static final String KEY_MESSAGE	= "message";
    public static final String KEY_RESULT	= "result";
    
    public static final String KEY_CHALLENGE1	= "challenge1"; //请验证
    public static final String KEY_CHALLENGE2	= "challenge2";	//请正确验证
    public static final String KEY_CHALLENGE3	= "challenge3";	//验证失败
    public static final String KEY_CCOUNTER		= "ccounter";

    public static final String KEY_RESPONSE1	= "response1";	//新年好！
    public static final String KEY_RESPONSE2	= "response2";	//李卓桓
    public static final String KEY_RESPONSE3	= "response3";	//短信门神#李卓桓
    public static final String KEY_RCOUNTER		= "rcounter";
    
    
    static final String TAG = "ChallengeResponseDb";
    
    DatabaseHelper mDbHelper;
    SQLiteDatabase mDb;

    static final String DATABASE_NAME = "ChallengeResponse_DB";
    static final String DATABASE_TABLE = "challengeresponse";
    static final int DATABASE_VERSION = 1;

    /**
     * Database creation sql statement
     */
    static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement "
        + ",number		TEXT 	NOT NULL "
        + ",timestamp	LONG	DEFAULT 0 "
        + ",message		TEXT 	DEFAULT '' "
        + ",result		LONG 	DEFAULT " + ChallengeResponse.Result.PENDING
        
        + ",challenge1	TEXT 	DEFAULT '' "
        + ",challenge2	TEXT 	DEFAULT '' "
        + ",challenge3	TEXT 	DEFAULT '' "
        + ",ccounter	LONG 	DEFAULT 0 "
        
        + ",response1	TEXT 	DEFAULT '' "
        + ",response2	TEXT 	DEFAULT '' "
        + ",response3	TEXT 	DEFAULT '' "
        + ",rcounter	LONG 	DEFAULT 0 "

        + "); ";

    Context mCtx;

    /*
     * 最长有效的 ChallengeResponse 间隔时间（毫秒）
     * 超出这段时间就需要从头重新CR
     */
    final	long	mMaxValidTime = 24*60*60*1000;	// 24小时
    
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
    public ChallengeResponseDb(Context ctx) {
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
    public ChallengeResponseDb open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
    	mDb.close();
    	mDb = null;

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
/*        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_WORD, keyWord);
        initialValues.put(KEY_NOTE, note);
        initialValues.put(KEY_ENABLED, 1);
        initialValues.put(KEY_TIME, new Date().getTime());
*/
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
    			, KEY_TIME
    	};
    	
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, columns
            		, KEY_ROWID + "=" + rowId
            		, null, null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
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
  
        args.put(KEY_TIME, new Date().getTime());

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor queryNumber(String phoneNumber) throws SQLException {
    	String[] columns = {
    			KEY_ROWID
    	};
    	
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, columns
            		, KEY_NUMBER + "=" + phoneNumber
            		, null, null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public int getResponseCounter(long rowId) {
    	String sql = "SELECT " + KEY_RCOUNTER 
    				+ " FROM " + DATABASE_TABLE
    				+ " WHERE " + KEY_ROWID + "=? "
    				+ "  AND " + KEY_TIME + ">?"
    				;
    	String selectionArgs[] = {
    		String.valueOf(rowId)
    		,String.valueOf(new Date().getTime()-mMaxValidTime)
    	};
    	
        Cursor cur = mDb.rawQuery(sql, selectionArgs);

        if (null == cur )
        	return 0;
        
        if ( 0>=cur.getCount() )
        	return 0;
        
        if ( !cur.moveToFirst() )
        	return 0;

        return cur.getInt(cur.getColumnIndex(KEY_RCOUNTER));
    }

    public int getChallengeCounter(long rowId) {
    	String sql = "SELECT " + KEY_CCOUNTER 
    				+ " FROM " + DATABASE_TABLE
    				+ " WHERE " + KEY_ROWID + "=? "
    				// + "  AND " + KEY_TIME + ">?"
    				;
    	String selectionArgs[] = {
    		String.valueOf(rowId)
//    		,String.valueOf(new Date().getTime()-mMaxValidTime)
    	};
    	
        Cursor cur = mDb.rawQuery(sql, selectionArgs);

        if ( null == cur 
       		|| 0>=cur.getCount()
       		|| !cur.moveToFirst() 
       	)
        	return 0;

        return cur.getInt(cur.getColumnIndex(KEY_CCOUNTER));
    }

    public long getRowId(String phoneNumber) {
    	String sql = "SELECT " + KEY_ROWID 
    				+ " FROM " + DATABASE_TABLE 
    				+ " WHERE " + KEY_NUMBER + "=? "
    				+ "  AND " + KEY_TIME + ">?"
    				+ "  ORDER BY " + KEY_ROWID + " DESC "
    				+ "  LIMIT 1 "
    				;
    	long since = new Date().getTime() - mMaxValidTime;
    	
        Cursor cur = mDb.rawQuery(sql,  new String[] {
        		phoneNumber
        		, Long.toString(since)} );
        
        if (null == cur )
        	return -1;
                
        int n = cur.getCount();
        
        if ( 0>=n )
        	return -1;

        if ( !cur.moveToFirst() )
        	return -1; 

        int l = cur.getColumnIndex(KEY_ROWID);
        
        return cur.getLong(l);
    }

	public void setResult(long rowId, ChallengeResponse.Result result) {
		mDb.execSQL("UPDATE " + DATABASE_TABLE 
				+ " SET result='" + result 
				+"' WHERE " + KEY_ROWID + "=" + rowId );
	}

	public long createCR(String phoneNumber, String textMessage) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NUMBER, 	phoneNumber);
        initialValues.put(KEY_MESSAGE, 	textMessage);
        initialValues.put(KEY_TIME, new Date().getTime());

        return mDb.insert(DATABASE_TABLE, null, initialValues);		
	}

	public void setChallenge(long rowId, int num, String challengeMessage) {
		String cKey;
		
		switch ( num ){
		case 1:
			cKey = KEY_CHALLENGE1;
			break;
		case 2:
			cKey = KEY_CHALLENGE2;
			break;
		case 3: default:
			cKey = KEY_CHALLENGE3;
			break;
		}
		
		mDb.execSQL("UPDATE " + DATABASE_TABLE 
				+ " SET " + cKey + "='" + challengeMessage + "'"
				+ "," + KEY_CCOUNTER + "=" + num
				+" WHERE " + KEY_ROWID + "=" + rowId );
	}

	public void setResponse(long rowId, int num, String responseMessage) {
		String respDbKey;
		
		switch ( num ){
		case 1:
			respDbKey = KEY_RESPONSE1;
			break;
		case 2: 
			respDbKey = KEY_RESPONSE2;
			break;
		case 3: default:
			respDbKey = KEY_RESPONSE3;
			break;
		}
		//TODO 增加 KEY_RESPONSE3
		ContentValues values = new ContentValues();
		String		where;
		String[]	args;
		
		values.put(respDbKey, responseMessage);
		values.put(KEY_RCOUNTER, num);
		where 	= KEY_ROWID + "=?";
		args	= new String[] {Long.toString(rowId)};
		
		mDb.update(DATABASE_TABLE, values, where, args);
		
		/*
		mDb.execSQL("UPDATE " + DATABASE_TABLE 
				+ " SET " + respDbKey + "='" + responseMessage 
				+"' WHERE " + KEY_ROWID + "=" + rowId );
				*/
	}

} 