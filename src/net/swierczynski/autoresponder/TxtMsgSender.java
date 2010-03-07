package net.swierczynski.autoresponder;

import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private static final String MESSAGE_BODY = "Thanks for your call. Unfortunately I couldn't answer it. I'll call you back as soon as possible.";
	
	private SmsManager smsMgr = SmsManager.getDefault();
	
	public void sendTextMessage(String telNumber) {
		smsMgr.sendTextMessage(telNumber, null, MESSAGE_BODY, null, null);
	}
}
