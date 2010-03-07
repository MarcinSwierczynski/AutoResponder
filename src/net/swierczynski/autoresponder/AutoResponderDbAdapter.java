package net.swierczynski.autoresponder;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class AutoResponderDbAdapter {
	public static final String KEY_CALL_ID = "_id";
	public static final String KEY_PHONE_NUMBER = "number";
	public static final String KEY_ANSWERED = "answered";
	
	private static final String TABLE_CREATE = 
		"create table calls (_id integer primary key, "
		+ "number text not null, "
		+ "answered integer not null);";
	
	private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "calls";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = AutoResponderDbAdapter.class.getName();
    
    private final Context mCtx;
	private DatabaseHelper mDbHelper ;
	private SQLiteDatabase mDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {

    	

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists calls");
			onCreate(db);
		}
    	
    }
    
    public AutoResponderDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}
    
    public AutoResponderDbAdapter open() throws SQLException {
    	mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
    	return this;
    }
    
    public void close() {
    	mDbHelper.close();
    }
    
    public boolean isPhoneCallPresent(long id) {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] { KEY_CALL_ID }, KEY_CALL_ID + "=" + id, null, null, null, null);
    	return c.getCount() > 0;
    }
    
    public void addPhoneCall(long id, String number) {
    	try {
    		if(!isPhoneCallPresent(id)) {
				ContentValues values = new ContentValues();
				values.put(KEY_CALL_ID, id);
				values.put(KEY_PHONE_NUMBER, number);
				values.put(KEY_ANSWERED, 0);
				mDb.insert(DATABASE_TABLE, null, values);
    		}
    	} catch (SQLiteConstraintException e) {
			Log.w(TAG, "The number " + number + " is allready in a database");
		}
    }
    
    public boolean markCallAsAnswered(long id) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_ANSWERED, 1);
    	return mDb.update(DATABASE_TABLE, values, KEY_CALL_ID + "=" + id, null) > 0;
    }
    
    public Cursor getUnansweredCalls() {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] { KEY_CALL_ID, KEY_PHONE_NUMBER }, KEY_ANSWERED + "=" + 0, null, null, null, null);
    	if(c != null) {
    		c.moveToFirst();
    	}
    	return c;
    }

}
