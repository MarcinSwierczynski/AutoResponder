package net.swierczynski.autoresponder;

import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private AutoResponderDbAdapter dbAdapter;
	private static String profile = "Main";
	private SmsManager smsMgr = SmsManager.getDefault();
	
	public TxtMsgSender(AutoResponderDbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public void sendTextMessage(String telNumber) {
		String messageBody = dbAdapter.fetchMessageBody(profile);
		smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
	}

	public static void setProfile(String profile) {
		TxtMsgSender.profile = profile;
	}
	
	public static String getProfile() {
		return profile;
	}
	
}
