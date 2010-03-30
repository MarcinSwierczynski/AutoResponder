package net.swierczynski.autoresponder;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

public class AutoResponderDbAdapter {
	public static final String KEY_MSG_PROFILE = "_profile";
	public static final String KEY_MSG_BODY = "msg_body";
	private static final String DEFAULT_MSG = "Thanks for your call. Unfortunately I couldn't answer it. I'll call you back as soon as possible.";
	
	private static final String TABLE_CREATE = 
		"create table autoresp_messages (_profile text primary key, "
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
			createInitialMessages(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists autoresp_messages");
			onCreate(db);
		}

		private void createInitialMessages(SQLiteDatabase db) {
			createMessageForProfile(db, "Main", DEFAULT_MSG);
			createMessageForProfile(db, "Home", "Thanks for your call. I'm resting with my family. I'll call you back tomorrow morning.");
			createMessageForProfile(db, "Work", "Thank you for your call. I'm at the meeting now. I'll call you back as soon as possible.");
			createMessageForProfile(db, "Hanging out", "I'm hanging out with my friends now. I'll call you back... sooner or later ;)");
		}

		private void createMessageForProfile(SQLiteDatabase db, String profile, String message) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_MSG_PROFILE, profile);
			initialValues.put(KEY_MSG_BODY, message);
			db.insert(DATABASE_TABLE, null, initialValues);
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
    
    public void saveMessage(String profile, String body) {
    	boolean messageExists = fetchMessage(profile).getCount() > 0;
		if(messageExists) {
    		updateMessage(profile, body);
    	} else {
	    	createMessage(profile, body);
    	}
    }

	public void createMessage(String profile, String body) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_MSG_PROFILE, profile);
		initialValues.put(KEY_MSG_BODY, body);
		mDb.insert(DATABASE_TABLE, null, initialValues);
	}
    
    public Cursor fetchMessage(String profile) {
    	Cursor c = mDb.query(DATABASE_TABLE, new String[] {KEY_MSG_PROFILE, KEY_MSG_BODY}, 
    				KEY_MSG_PROFILE + "= '" + profile + "'", null, null, null, null);
    	if (c != null) {
			c.moveToFirst();
		}
		return c;
    }
    
    public String fetchMessageBody(String profile) {
    	Cursor message = fetchMessage(profile);
    	boolean isMessageForGivenProfile = message.getCount() > 0;
		if(isMessageForGivenProfile) {
    		return message.getString(message.getColumnIndexOrThrow(KEY_MSG_BODY));
    	} else {
    		return DEFAULT_MSG;
    	}
    }
    
    public boolean updateMessage(String profile, String body) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_MSG_BODY, body);
    	return mDb.update(DATABASE_TABLE, args, KEY_MSG_PROFILE + "= '" + profile + "'", null) > 0;
    }
    
    public static AutoResponderDbAdapter initializeDatabase(Context ctx) {
    	AutoResponderDbAdapter dbAdapter = new AutoResponderDbAdapter(ctx);
    	dbAdapter.open();
    	return dbAdapter;
    }

}
