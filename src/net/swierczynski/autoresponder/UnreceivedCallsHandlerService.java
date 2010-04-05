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
	private Notification notification;
	
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
		unreceivedCallListener = new NewUnreceivedCallListener(msgSender, this);
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private void unregisterUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_NONE);
	}
	
	private void showNotificationIcon() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		createNotification();
		updateNotification(0);
	}
	
	private void hideNotificationIcon() {
		notificationManager.cancel(R.string.app_name);
	}

	public void updateNotification(int repliesCounter) {
		CharSequence description = String.format(getText(R.string.notification_text).toString(), repliesCounter);
		PendingIntent contentIntent = getNotificationIntent();
		
		notification.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name), description, contentIntent);
		
		notificationManager.notify(R.string.app_name, notification);
	}

	private PendingIntent getNotificationIntent() {
		Intent notificationIntent = new Intent(this, AutoResponder.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		return contentIntent;
	}

	private void createNotification() {
		int icon = R.drawable.icon;
		CharSequence text = getText(R.string.app_name);
		long when = System.currentTimeMillis();
		notification = new Notification(icon, text, when);
		notification.flags |= Notification.FLAG_NO_CLEAR;
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
