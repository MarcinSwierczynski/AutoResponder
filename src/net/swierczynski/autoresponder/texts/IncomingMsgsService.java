package net.swierczynski.autoresponder.texts;

import net.swierczynski.autoresponder.NotificationArea;
import net.swierczynski.autoresponder.TxtMsgSender;
import android.content.Context;
import android.content.IntentFilter;

public class IncomingMsgsService {
	private Context mCtx;
	
	private NotificationArea notificationArea;
	private TxtMsgReceiver txtReceiver;
	
	public static boolean isActive = false;
	
	public IncomingMsgsService(Context mCtx, NotificationArea notificationArea) {
		this.mCtx = mCtx;
		this.notificationArea = notificationArea;
	}

	public void register() {
		TxtMsgSender msgSender = TxtMsgSender.createAndSetUp(mCtx, notificationArea);
		txtReceiver = new TxtMsgReceiver(msgSender);
		IntentFilter incomingTxtFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		mCtx.registerReceiver(txtReceiver, incomingTxtFilter);
		isActive = true;
	}
	
	public void unregister() {
		try {
			mCtx.unregisterReceiver(txtReceiver);
		} catch (IllegalArgumentException e) { 
			// Do nothing
		} finally { 
			isActive = false;
		}
	}
}
