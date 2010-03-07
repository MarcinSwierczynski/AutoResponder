package net.swierczynski.autoresponder;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class AutoResponderDbAdapter {
	public static final String KEY_MSG_ID = "_id";
	public static final String KEY_MSG_BODY = "msg_body";
	
	private static final String TABLE_CREATE = 
		"create table autoresp_messages (_id integer primary key, "
		+ "msg_body text not null);";
	
	private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "autoresp_messages";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = AutoResponderDbAdapter.class.getName();
    
    private final Context mCtx;
	private DatabaseHelper mDbHelper;
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
			db.execSQL("drop table if exists autoresp_messages");
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
    
    public long createMessage(String body) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_MSG_BODY, body);
    	return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * 
     * @param id - will need this if we decide to support many different messages
     * @return
     */
    public Cursor fetchMessage(long id) {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_MSG_BODY}, null, null, null, null, null);
    	c.moveToFirst();
    	return c;
    }
    
    public boolean updateNote(long id, String body) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_MSG_BODY, body);
    	return mDb.update(DATABASE_TABLE, args, KEY_MSG_ID + "=" + id, null) > 0;
    }

}
