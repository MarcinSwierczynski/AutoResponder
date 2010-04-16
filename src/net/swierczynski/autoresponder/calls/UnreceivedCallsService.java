package net.swierczynski.autoresponder.calls;

import net.swierczynski.autoresponder.NotificationArea;
import net.swierczynski.autoresponder.TxtMsgSender;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class UnreceivedCallsService {
	private Context mCtx;
	
	private NotificationArea notificationArea;
	private UnreceivedCallListener unreceivedCallListener;
	
	public static boolean isActive = false;
	
	public UnreceivedCallsService(Context mCtx, NotificationArea notificationArea) {
		this.mCtx = mCtx;
		this.notificationArea = notificationArea;
	}

	public void register() {
		TxtMsgSender msgSender = TxtMsgSender.createAndSetUp(mCtx, notificationArea);
		unreceivedCallListener = new UnreceivedCallListener(msgSender);
		TelephonyManager telephonyManager = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
		isActive = true;
	}

	public void unregister() {
		TelephonyManager telephonyManager = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_NONE);
		isActive = false;
	}
}
