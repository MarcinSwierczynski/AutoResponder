package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.calls.UnreceivedCallsService;
import net.swierczynski.autoresponder.texts.IncomingMsgsService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class AutoResponderService extends Service {
	private final IBinder mBinder = new LocalBinder();

	private UnreceivedCallsService callsService;
	private IncomingMsgsService msgsService;
	private NotificationArea notificationArea;
	
	@Override
	public void onCreate() {
		if(!isIconVisible()) {
			initializeServiceFirstStart();
		}
	}

	private void initializeServiceFirstStart() {
		notificationArea = new NotificationArea(this);
		callsService = new UnreceivedCallsService(this, notificationArea);
		msgsService = new IncomingMsgsService(this, notificationArea);
	}
	
	@Override
	public void onDestroy() {
		notificationArea.hideNotificationIcon();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent != null) {
			Bundle extras = intent.getExtras();
			if(extras != null) {
				boolean isEnabled = extras.getBoolean("isEnabled");
				String mode = extras.getString("mode");
				changeServiceMode(mode, isEnabled);
			}
		}
		propagateStateToNotificationArea();
	}

	private void propagateStateToNotificationArea() {
		if(isIconVisible()) {
			notificationArea.showNotificationIcon();			
		} else {
			stopSelf();
		}
	}

	private void changeServiceMode(String mode, boolean isEnabled) {
		if(mode.equals("calls")) {
			changeCallsModeState(isEnabled);
		} else {
			changeTextsModeState(isEnabled);
		}
	}

	private void changeCallsModeState(boolean isEnabled) {
		if(isEnabled) {
			callsService.register();
		} else {
			callsService.unregister();
		}
	}
	
	private void changeTextsModeState(boolean isEnabled) {
		if(isEnabled) {
			msgsService.register();
		} else {
			msgsService.unregister();
		}
	}
	
	public static boolean isIconVisible() {
		return UnreceivedCallsService.isActive || IncomingMsgsService.isActive;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		AutoResponderService getService() {
			return AutoResponderService.this;
		}
	}

}
