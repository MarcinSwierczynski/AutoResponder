package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.preferences.UserPreferences;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationArea {
	
	private final Context mCtx;
	private NotificationManager notificationManager;
	private Notification notification;
	private int repliesCounter;
	
	public NotificationArea(Context context) {
		this.mCtx = context;
		this.notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
		this.repliesCounter = 0;
	}
	
	public void showNotificationIcon() {
		createNotification();
		updateNotification();
	}
	
	public void hideNotificationIcon() {
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

	public void incrementRepliesCounter() {
		repliesCounter++;
		updateCounterIfIconIsDisplayed();
	}
	
	public void resetRepliesCounter() {
		repliesCounter = 0;
		updateCounterIfIconIsDisplayed();
	}

	private void updateCounterIfIconIsDisplayed() {
		if (UserPreferences.isIconInTaskbarSelected(mCtx)) {
			updateNotification();
		}
	}
}
