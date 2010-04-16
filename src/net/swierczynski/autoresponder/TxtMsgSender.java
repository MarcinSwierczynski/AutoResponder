package net.swierczynski.autoresponder;

import android.content.Context;
import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private AutoResponderDbAdapter dbAdapter;
	private static String profile = "Main";
	private SmsManager smsMgr = SmsManager.getDefault();
	
	public TxtMsgSender(AutoResponderDbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public void sendTextMessage(String telNumber) {
		boolean telNumberExists = telNumber != null && telNumber.length() > 0;
		if (telNumberExists) {
			String messageBody = dbAdapter.fetchMessageBody(profile);
			smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
		}
	}

	public static void setProfile(String profile) {
		TxtMsgSender.profile = profile;
	}
	
	public static String getProfile() {
		return profile;
	}
	
	public static TxtMsgSender getNewInstance(Context ctx) {
		AutoResponderDbAdapter dbAdapter = AutoResponderDbAdapter.initializeDatabase(ctx);
		TxtMsgSender txtMsgSender = new TxtMsgSender(dbAdapter);
		return txtMsgSender;
	}
	
}
