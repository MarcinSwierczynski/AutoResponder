package net.swierczynski.autoresponder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class UnreceivedCallsHandlerService extends Service {
	private final IBinder mBinder = new LocalBinder();
	private NotificationManager notificationManager;
	private NewUnreceivedCallListener unreceivedCallListener;
	private static TxtMsgSender msgSender;
	
	private static String profile = "Main";

	public static boolean is_running = false;
	
	@Override
	public void onCreate() {
		registerUnreceivedCallListener();
		showNotificationIcon();
		is_running = true;
	}

	@Override
	public void onDestroy() {
		unregisterUnreceivedCallListener();
		hideNotificationIcon();
		is_running = false;
	}
	
	private TxtMsgSender initalizeMsgSender() {
		AutoResponderDbAdapter dbAdapter = AutoResponderDbAdapter.initializeDatabase(this);
		TxtMsgSender txtMsgSender = new TxtMsgSender(dbAdapter);
		txtMsgSender.setProfile(profile);
		return txtMsgSender;
	}
	
	private void registerUnreceivedCallListener() {
		msgSender = initalizeMsgSender();
		unreceivedCallListener = new NewUnreceivedCallListener(msgSender);
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private void unregisterUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_NONE);
	}
	
	private void showNotificationIcon() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		createIconInNotificationArea();
	}
	
	private void hideNotificationIcon() {
		notificationManager.cancel(R.string.app_name);
	}

	private void createIconInNotificationArea() {
		int icon = R.drawable.icon;
		CharSequence text = getText(R.string.app_name);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, text, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = "Missed calls checking is active!";
		Intent notificationIntent = new Intent(this, AutoResponder.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notificationManager.notify(R.string.app_name, notification);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		UnreceivedCallsHandlerService getService() {
			return UnreceivedCallsHandlerService.this;
		}
	}
	
	public static String getProfile() {
		return profile;
	}

	public static void setProfile(String profile) {
		UnreceivedCallsHandlerService.profile = profile;
		if(UnreceivedCallsHandlerService.msgSender != null) {
			UnreceivedCallsHandlerService.msgSender.setProfile(profile);
		}
	}

}
