package net.swierczynski.autoresponder;

import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private AutoResponderDbAdapter dbAdapter;
	private String profile;
	private SmsManager smsMgr = SmsManager.getDefault();
	
	public TxtMsgSender(AutoResponderDbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public void sendTextMessage(String telNumber) {
		String messageBody = dbAdapter.fetchMessageBody(profile);
		smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}
	
}
