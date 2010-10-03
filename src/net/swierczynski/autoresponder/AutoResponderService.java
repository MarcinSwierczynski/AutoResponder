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

	private BroadcastReceiver notificationAreaReceiver;
	
	@Override
	public void onCreate() {
		if(!isAnyRespondingServiceActive()) {
			initializeServiceFirstStart();
		}
	}

	private void initializeServiceFirstStart() {
		registerNotificationAreaReceiver();
		
		callsService = new UnreceivedCallsService(this);
		msgsService = new IncomingMsgsService(this);
		
		UserPreferences.registerPreferencesChangeListener(getApplicationContext(), this);
	}
	
	private void registerNotificationAreaReceiver() {
		notificationAreaReceiver = new NotificationArea(this).new Receiver();
		
		IntentFilter incrementFilter = new IntentFilter(NotificationArea.INCREMENT);
		registerReceiver(notificationAreaReceiver, incrementFilter);
		
		IntentFilter resetFilter = new IntentFilter(NotificationArea.RESET);
		registerReceiver(notificationAreaReceiver, resetFilter);
		
		IntentFilter showIconFilter = new IntentFilter(NotificationArea.SHOW_ICON);
		registerReceiver(notificationAreaReceiver, showIconFilter);
		
		IntentFilter hideIconFilter = new IntentFilter(NotificationArea.HIDE_ICON);
		registerReceiver(notificationAreaReceiver, hideIconFilter);
	}

	@Override
	public void onDestroy() {
		hideNotificationIcon();
		unregisterReceiver(notificationAreaReceiver);
	}

	private void hideNotificationIcon() {
		Intent intent = new Intent(NotificationArea.HIDE_ICON);
		sendBroadcast(intent);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent != null) {
			Bundle extras = intent.getExtras();
			if(extras != null) {
				String mode = extras.getString("mode");
				boolean isEnabled = extras.getBoolean("isEnabled");
				changeServiceModeAndPropagateItsState(isEnabled, mode);
			}
		}
	}
	
	private void changeServiceModeAndPropagateItsState(boolean isEnabled, String mode) {
		changeServiceMode(mode, isEnabled);
		propagateStateToNotificationArea();
		stopIfNoRespondingServicesAreActive();
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
	
	private void propagateStateToNotificationArea() {
		String iconState = getNewIconState();
		Intent intent = new Intent(iconState);
		sendBroadcast(intent);
	}

	private String getNewIconState() {
		if(UserPreferences.isIconInTaskbarSelected(this) && isAnyRespondingServiceActive()) {
			return NotificationArea.SHOW_ICON;
		} else {
			return NotificationArea.HIDE_ICON;
		}
	}

	private void stopIfNoRespondingServicesAreActive() {
		if (!isAnyRespondingServiceActive()) {
			stopSelf();
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
		propagateStateToNotificationArea();
	}

}
