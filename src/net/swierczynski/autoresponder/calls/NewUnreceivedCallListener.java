package net.swierczynski.autoresponder.calls;

import net.swierczynski.autoresponder.NotificationArea;
import net.swierczynski.autoresponder.TxtMsgSender;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.util.Log;
import static android.telephony.TelephonyManager.*;

public class NewUnreceivedCallListener extends PhoneStateListener {
	private static final String TAG = NewUnreceivedCallListener.class.getName();

	private boolean callWasUnreceived = false;
	private String phoneNumber;

	private Context mCtx;
	private TxtMsgSender txtMsgSender;
	private NotificationArea notificationArea;
	
	public NewUnreceivedCallListener(Context ctx, NotificationArea notificationArea) {
		this.mCtx = ctx;
		this.notificationArea = notificationArea;
		this.txtMsgSender = TxtMsgSender.getNewInstance(mCtx);
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		switch(state) {
			case CALL_STATE_RINGING:
				checkCallAsUnreceived(incomingNumber);
				break;
			case CALL_STATE_OFFHOOK:	//call was received
				checkCallAsReceived();
				break;
			case CALL_STATE_IDLE:		//no active calls
				sendMsgIfCallWasntReceived();
				break;
		}
	}

	private void checkCallAsUnreceived(String incomingNumber) {
//		Log.d(TAG, "Telephone is ringing...");
		phoneNumber = incomingNumber;
		callWasUnreceived = true;
	}

	private void checkCallAsReceived() {
//		Log.d(TAG, "Call received.");
		phoneNumber = null;
		callWasUnreceived = false;
	}

	private void sendMsgIfCallWasntReceived() {
		if(callWasUnreceived && phoneNumber != null) {
//			Log.d(TAG, "Unreceived call. Sending txt msg!");
			txtMsgSender.sendTextMessage(phoneNumber);
			notificationArea.incrementRepliesCounter();

			callWasUnreceived = false;
			phoneNumber = null;
		}
	}
}
