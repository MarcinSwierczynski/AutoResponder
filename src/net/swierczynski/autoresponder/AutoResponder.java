package net.swierczynski.autoresponder;

import java.util.Date;
import java.util.concurrent.*;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.gsm.*;
import android.util.Log;
import android.widget.*;

public class AutoResponder extends Activity {
	private static final int CHECK_INTERVAL = 20;
	private static final String MESSAGE_BODY = "Thanks for your call. Unfortunately I couldn't answer it. I'll call you back as soon as possible.";
	private static final int SECONDS_BEFORE = CHECK_INTERVAL + 60;
	private static final String TAG = AutoResponder.class.getName();
	
	private SmsManager smsMgr = SmsManager.getDefault();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> servantHandler;
	
	private AutoResponderDbAdapter dbAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initalizeDatabase();
        initializeIntervalSpinner();
        turnOnChecking();
        createIconInNotificationArea();
    }

	private void initalizeDatabase() {
		dbAdapter = new AutoResponderDbAdapter(this);
        dbAdapter.open();
	}

	private void initializeIntervalSpinner() {
		Spinner intervalSpinner = (Spinner) findViewById(R.id.interval);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.intervals_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(arrayAdapter);
	}
    
//    @Override
//    protected void onDestroy() {
//    	super.onDestroy();
//    	turnOffChecking();
//    }

	public void turnOnChecking() {
		final Runnable missedCallsServant = new Runnable() {
			public void run() {
				Log.i(TAG, "Serving recent missed calls...");
				serveRecentMissedCalls();
			}
		};
		servantHandler = scheduler.scheduleAtFixedRate(missedCallsServant, CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
	}
    
    public boolean turnOffChecking() {
    	Log.i(TAG, "Turning off...");
    	return servantHandler.cancel(true);
    }

	private void serveRecentMissedCalls() {
		saveRecentMissedCallsToDb(SECONDS_BEFORE);
        Cursor c = dbAdapter.getUnansweredCalls();
        sendAutoResponderMessages(c);
	}

	private void sendAutoResponderMessages(Cursor c) {
		for(int i = 0; i < c.getCount(); i++) {
			c.moveToPosition(i);
			long id = c.getLong(c.getColumnIndexOrThrow(AutoResponderDbAdapter.KEY_CALL_ID));
			String telNumber = c.getString(c.getColumnIndexOrThrow(AutoResponderDbAdapter.KEY_PHONE_NUMBER));
			sendTextMessage(telNumber);
			dbAdapter.markCallAsAnswered(id);
			Log.i(TAG, "Message has been sent to " + telNumber);
		}
	}

	private void sendTextMessage(String telNumber) {
		smsMgr.sendTextMessage(telNumber, null, MESSAGE_BODY, null, null);
	}

	private void saveRecentMissedCallsToDb(int secondsBefore) {
        long timeBeforeSeconds = getTimeBeforeSeconds(secondsBefore);
		Cursor c = getRecentMissedCalls(timeBeforeSeconds);
		savePhoneCalls(c);
	}

	private Cursor getRecentMissedCalls(long timeBeforeSeconds) {
		String[] projection = new String[] {CallLog.Calls._ID, CallLog.Calls.NUMBER};
		String selection = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
							+ " AND " + CallLog.Calls.DATE + ">=" + timeBeforeSeconds;
		String orderBy = CallLog.Calls.DATE;
		Cursor c = managedQuery(CallLog.Calls.CONTENT_URI, projection, selection, null, orderBy);
		return c;
	}

	private void savePhoneCalls(Cursor c) {
		for(int i = 0; i < c.getCount(); i++) {
			c.moveToPosition(i);
			long id = c.getLong(c.getColumnIndexOrThrow(CallLog.Calls._ID));
			String number = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
			dbAdapter.addPhoneCall(id, number);
		}
	}

	private long getTimeBeforeSeconds(int scondsBefore) {
		long timeBeforeSeconds = new Date().getTime() - scondsBefore * 1000;
		return timeBeforeSeconds;
	}
	
	private void createIconInNotificationArea() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.icon;
		String text = "AutoResponder";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, text, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = "Missed calls checking is active!";
		Intent notificationIntent = new Intent(this, AutoResponderConfig.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notificationManager.notify(1, notification);
	}
}