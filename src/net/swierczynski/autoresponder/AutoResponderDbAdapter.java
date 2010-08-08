package net.swierczynski.autoresponder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    
    private final Context mCtx;
	private DatabaseHelper mDbHelper;

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

	private void createMessage(String profile, String body) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_MSG_PROFILE, profile);
		initialValues.put(KEY_MSG_BODY, body);
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.insert(DATABASE_TABLE, null, initialValues);
	}
    
    private Cursor fetchMessage(String profile) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Cursor c = db.query(DATABASE_TABLE, new String[] {KEY_MSG_PROFILE, KEY_MSG_BODY}, 
    				KEY_MSG_PROFILE + "= '" + profile + "'", null, null, null, null);
    	if (c != null) {
			c.moveToFirst();
		}
		return c;
    }
    
    public String fetchMessageBody(String profile) {
    	Cursor messageCursor = fetchMessage(profile);
    	boolean isMessageForGivenProfile = messageCursor.getCount() > 0;
		String messageBody;
    	if(isMessageForGivenProfile) {
    		messageBody = messageCursor.getString(messageCursor.getColumnIndexOrThrow(KEY_MSG_BODY));
    	} else {
    		messageBody = DEFAULT_MSG;
    	}
    	messageCursor.close();
    	return messageBody;
    }
    
    private boolean updateMessage(String profile, String body) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_MSG_BODY, body);
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.update(DATABASE_TABLE, args, KEY_MSG_PROFILE + "= '" + profile + "'", null) > 0;
    }
    
    public static AutoResponderDbAdapter initializeDatabase(Context ctx) {
    	AutoResponderDbAdapter dbAdapter = new AutoResponderDbAdapter(ctx);
    	dbAdapter.open();
    	return dbAdapter;
    }

}
