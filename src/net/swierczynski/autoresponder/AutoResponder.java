package net.swierczynski.autoresponder;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AutoResponder extends Activity {
	private static final String TAG = AutoResponder.class.getName();
	
	private AutoResponderDbAdapter dbAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initalizeDatabase();
        initializeUnreceivedCallListener();
        createIconInNotificationArea();
    }

	private void initializeUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		TxtMsgSender txtMsgSender = new TxtMsgSender();
		telephonyManager.listen(new NewUnreceivedCallListener(txtMsgSender), PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void initalizeDatabase() {
		dbAdapter = new AutoResponderDbAdapter(this);
        dbAdapter.open();
	}
    
//    @Override
//    protected void onDestroy() {
//    	super.onDestroy();
//    	turnOffChecking();
//    }

    
    public boolean turnOffChecking() {
    	Log.i(TAG, "Turning off...");
    	return true;
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