package net.swierczynski.autoresponder.calls;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import net.swierczynski.autoresponder.TxtMsgSender;
import android.telephony.PhoneStateListener;

public class UnreceivedCallListener extends PhoneStateListener {
	private boolean callWasUnreceived = false;
	private String phoneNumber;

	private TxtMsgSender msgSender;
	
	public UnreceivedCallListener(TxtMsgSender msgSender) {
		this.msgSender = msgSender;
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
		phoneNumber = incomingNumber;
		callWasUnreceived = true;
	}

	private void checkCallAsReceived() {
		phoneNumber = null;
		callWasUnreceived = false;
	}

	private void sendMsgIfCallWasntReceived() {
		if(callWasUnreceived && phoneNumber != null) {
			msgSender.sendTextMessage(phoneNumber);

			callWasUnreceived = false;
			phoneNumber = null;
		}
	}
}
