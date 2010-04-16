package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.calls.UnreceivedCallListener;
import net.swierczynski.autoresponder.texts.TxtMsgReceiver;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AutoResponderService extends Service {
	private final IBinder mBinder = new LocalBinder();

	private UnreceivedCallListener unreceivedCallListener;
	private TxtMsgReceiver txtReceiver;
	private NotificationArea notificationArea;
	private TxtMsgSender msgSender;

	public static boolean responseToCalls = false;
	public static boolean responseToTexts = false;
	
	@Override
	public void onCreate() {
		if(!isIconVisible()) {
			firstServiceStart();
		}
	}

	private void firstServiceStart() {
		notificationArea = new NotificationArea(this);
		msgSender = TxtMsgSender.createAndSetUp(this, notificationArea);
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
			registerUnreceivedCallListener();
		} else {
			unregisterUnreceivedCallListener();
		}
	}
	
	private void changeTextsModeState(boolean isEnabled) {
		if(isEnabled) {
			registerTextsListener();
		} else {
			unregisterTextsListener();
		}
	}

	private void registerUnreceivedCallListener() {
		unreceivedCallListener = new UnreceivedCallListener(msgSender);
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
		responseToCalls = true;
		Log.i("AutoResponder", "Calls listener registered");
	}

	private void unregisterUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_NONE);
		responseToCalls = false;
		Log.i("AutoResponder", "Calls listener unregistered");
	}
	
	private void registerTextsListener() {
		txtReceiver = new TxtMsgReceiver(msgSender);
		IntentFilter incomingTxtFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(txtReceiver, incomingTxtFilter);
		responseToTexts = true;
		Log.i("AutoResponder", "Texts listener registered");
	}
	
	private void unregisterTextsListener() {
		unregisterReceiver(txtReceiver);
		responseToTexts = false;
		Log.i("AutoResponder", "Texts listener unregistered");
	}
	
	public static boolean isIconVisible() {
		return responseToCalls || responseToTexts;
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
