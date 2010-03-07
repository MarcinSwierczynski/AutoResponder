package net.swierczynski.autoresponder;

import android.telephony.PhoneStateListener;
import android.util.Log;
import static android.telephony.TelephonyManager.*;

public class NewUnreceivedCallListener extends PhoneStateListener {
	private static final String TAG = NewUnreceivedCallListener.class.getName();
	private boolean callWasUnreceived = false;
	private TxtMsgSender txtMsgSender;
	private String phoneNumber;
	private boolean enabled;
	
	private static NewUnreceivedCallListener instance;
	
	private NewUnreceivedCallListener(TxtMsgSender txtMsgSender) {
		this.txtMsgSender = txtMsgSender;
	}
	
	public static NewUnreceivedCallListener getInstance(TxtMsgSender txtMsgSender) {
		if(instance == null) {
			instance = new NewUnreceivedCallListener(txtMsgSender);
		}
		return instance;
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		if(enabled) {
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
	}

	private void checkCallAsUnreceived(String incomingNumber) {
		Log.d(TAG, "Telephone is ringing...");
		phoneNumber = incomingNumber;
		callWasUnreceived = true;
	}

	private void checkCallAsReceived() {
		Log.d(TAG, "Call received.");
		phoneNumber = null;
		callWasUnreceived = false;
	}

	private void sendMsgIfCallWasntReceived() {
		if(callWasUnreceived && phoneNumber != null) {
			Log.d(TAG, "Unreceived call. Sending txt msg!");
			callWasUnreceived = false;
			txtMsgSender.sendTextMessage(phoneNumber);
			phoneNumber = null;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		Log.d(TAG, "Application enabled: " + enabled);
	}

	public boolean isEnabled() {
		return enabled;
	}

}
