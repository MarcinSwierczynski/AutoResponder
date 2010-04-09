package net.swierczynski.autoresponder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class UnreceivedCallsHandlerService extends Service {
	private final IBinder mBinder = new LocalBinder();

	private NewUnreceivedCallListener unreceivedCallListener;
	private NotificationArea notificationArea;
	
	public static boolean is_running = false;
	
	@Override
	public void onCreate() {
		showNotificationIcon();
		registerUnreceivedCallListener();
		is_running = true;
	}

	@Override
	public void onDestroy() {
		notificationArea.hideNotificationIcon();
		unregisterUnreceivedCallListener();
		is_running = false;
	}
		
	private void showNotificationIcon() {		
		notificationArea = new NotificationArea(this);
		notificationArea.showNotificationIcon();
	}
		
	private void registerUnreceivedCallListener() {
		unreceivedCallListener = new NewUnreceivedCallListener(this, notificationArea);
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void unregisterUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_NONE);
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

}
