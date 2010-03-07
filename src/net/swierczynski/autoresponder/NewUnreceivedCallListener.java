package net.swierczynski.autoresponder;

import android.telephony.PhoneStateListener;
import android.util.Log;
import static android.telephony.TelephonyManager.*;

public class NewUnreceivedCallListener extends PhoneStateListener {
	private static final String TAG = NewUnreceivedCallListener.class.getName();
	private boolean callWasUnreceived = false;
		
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		switch(state) {
			case CALL_STATE_RINGING:
				checkCallAsUnreceived();
				break;
			case CALL_STATE_OFFHOOK:	//call was received
				checkCallAsReceived();
				break;
			case CALL_STATE_IDLE:		//no active calls
				sendMsgIfCallWasntReceived();
				break;
		}
	}

	private void checkCallAsUnreceived() {
		Log.d(TAG, "Telephone is ringing...");
		callWasUnreceived = true;
	}

	private void checkCallAsReceived() {
		Log.d(TAG, "Call received.");
		callWasUnreceived = false;
	}

	private void sendMsgIfCallWasntReceived() {
		if(callWasUnreceived) {
			Log.d(TAG, "Unreceived call. Sending txt msg!");
			callWasUnreceived = false;
			//TODO: send text msg
		}
	}
}
