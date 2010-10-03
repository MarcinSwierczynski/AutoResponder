package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.preferences.UserPreferences;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;

public class NotificationArea {
	
	public static final String SHOW_ICON = "net.swierczynski.autoresponder.SHOW_ICON";
	public static final String HIDE_ICON = "net.swierczynski.autoresponder.HIDE_ICON";
	public static final String INCREMENT = "net.swierczynski.autoresponder.INCREMENT_COUNTER";
	public static final String RESET = "net.swierczynski.autoresponder.RESET_COUNTER";
	
	private final Context mCtx;
	private NotificationManager notificationManager;
	private Notification notification;
	private int repliesCounter;
	
	public NotificationArea(Context context) {
		this.mCtx = context;
		this.notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
		this.repliesCounter = 0;
	}
	
	private void showNotificationIcon() {
		createNotification();
		updateNotification();
	}
	
	private void hideNotificationIcon() {
		notificationManager.cancel(R.string.app_name);
	}

	private void updateNotification() {
		CharSequence description = String.format(mCtx.getText(R.string.notification_text).toString(), repliesCounter);
		PendingIntent contentIntent = getNotificationIntent();
		
		notification.setLatestEventInfo(mCtx, mCtx.getText(R.string.app_name), description, contentIntent);
		
		notificationManager.notify(R.string.app_name, notification);
	}

	private PendingIntent getNotificationIntent() {
		Intent notificationIntent = new Intent(mCtx, AutoResponder.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, notificationIntent, 0);
		return contentIntent;
	}

	private void createNotification() {
		int icon = R.drawable.icon;
		CharSequence text = mCtx.getText(R.string.app_name);
		long when = System.currentTimeMillis();
		notification = new Notification(icon, text, when);
		notification.flags |= Notification.FLAG_NO_CLEAR;
	}

	private void incrementRepliesCounter() {
		repliesCounter++;
		updateCounterIfIconIsDisplayed();
	}
	
	private void resetRepliesCounter() {
		repliesCounter = 0;
		updateCounterIfIconIsDisplayed();
	}

	private void updateCounterIfIconIsDisplayed() {
		if (UserPreferences.isIconInTaskbarSelected(mCtx) && notification != null) {
			updateNotification();
		}
	}
	
	public class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(INCREMENT)) {
				incrementRepliesCounter();
			} else if (action.equals(RESET)) {
				resetRepliesCounter();
			} else if (action.equals(SHOW_ICON)) {
				showNotificationIcon();
			} else if (action.equals(HIDE_ICON)) {
				hideNotificationIcon();
			}
		}
	}
}
