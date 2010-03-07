package net.swierczynski.autoresponder;

import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private AutoResponderDbAdapter dbAdapter;
	private String profile;
	private SmsManager smsMgr = SmsManager.getDefault();
	
	private static TxtMsgSender instance;
	
	private TxtMsgSender(AutoResponderDbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}
	
	public static TxtMsgSender getInstance(AutoResponderDbAdapter dbAdapter) {
		if(instance == null) {
			instance = new TxtMsgSender(dbAdapter);
		}
		return instance;
	}

	public void sendTextMessage(String telNumber) {
		String messageBody = dbAdapter.fetchMessageBody(profile);
		smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}
	
}
