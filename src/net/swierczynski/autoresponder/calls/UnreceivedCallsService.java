package net.swierczynski.autoresponder.calls;

import net.swierczynski.autoresponder.TxtMsgSender;
import android.content.*;
import android.telephony.*;

public class UnreceivedCallsService {
	private Context mCtx;
	
	private UnreceivedCallListener unreceivedCallListener;
	
	public static boolean isActive = false;
	public static final String REGISTER = "net.swierczynski.autoresponder.REGISTER";
	public static final String UNREGISTER = "net.swierczynski.autoresponder.UNREGISTER";
	
	public UnreceivedCallsService(Context mCtx) {
		this.mCtx = mCtx;
	}

	public void register() {
		TxtMsgSender msgSender = TxtMsgSender.createAndSetUp(mCtx);
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
	
	public class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(REGISTER)) {
				register();
			} else if (action.equals(UNREGISTER)) {
				unregister();
			}
		}
		
	}
}
