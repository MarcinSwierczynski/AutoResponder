package net.swierczynski.autoresponder.texts;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TextResponderService extends Service {
	private final IBinder mBinder = new LocalBinder();
	
	private BroadcastReceiver txtReceiver;
	
	public static boolean is_running = false;
	
	@Override
	public void onCreate() {
		txtReceiver = new TxtMsgReceiver(this);
		IntentFilter incomingTxtFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(txtReceiver, incomingTxtFilter);
		is_running = true;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(txtReceiver);
		is_running = false;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		TextResponderService getService() {
			return TextResponderService.this;
		}
	}

}
