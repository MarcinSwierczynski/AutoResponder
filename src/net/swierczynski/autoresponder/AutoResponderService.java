package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.calls.UnreceivedCallsService;
import net.swierczynski.autoresponder.preferences.UserPreferences;
import net.swierczynski.autoresponder.texts.IncomingMsgsService;
import android.app.Service;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.*;

public class AutoResponderService extends Service implements OnSharedPreferenceChangeListener {
	private final IBinder mBinder = new LocalBinder();

	private UnreceivedCallsService callsService;
	private IncomingMsgsService msgsService;
	private NotificationArea notificationArea;
	
	@Override
	public void onCreate() {
		if(!isAnyRespondingServiceActive()) {
			initializeServiceFirstStart();
		}
	}

	private void initializeServiceFirstStart() {
		notificationArea = new NotificationArea(this);
		callsService = new UnreceivedCallsService(this, notificationArea);
		msgsService = new IncomingMsgsService(this, notificationArea);
		UserPreferences.registerPreferencesChangeListener(getApplicationContext(), this);
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
				String mode = extras.getString("mode");
				if (mode.equals("reset")) {
					notificationArea.resetRepliesCounter();
				} else {
					boolean isEnabled = extras.getBoolean("isEnabled");
					changeServiceModeAndPropagateItsState(isEnabled, mode);
				}
			}
		}
	}

	private void changeServiceModeAndPropagateItsState(boolean isEnabled, String mode) {
		changeServiceMode(mode, isEnabled);
		propagateStateToNotificationArea();
		stopIfNoRespondingServicesAreActive();
	}

	private void propagateStateToNotificationArea() {
		if(UserPreferences.isIconInTaskbarSelected(this) && isAnyRespondingServiceActive()) {
			notificationArea.showNotificationIcon();			
		} else {
			notificationArea.hideNotificationIcon();
		}
	}

	private void stopIfNoRespondingServicesAreActive() {
		if (!isAnyRespondingServiceActive()) {
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
	
	private static boolean isAnyRespondingServiceActive() {
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

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("ICON_IN_TASKBAR")) {
			propagateStateToNotificationArea();
		}
	}

}
