package net.swierczynski.autoresponder;

import android.content.Context;
import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private AutoResponderDbAdapter dbAdapter;
	private NotificationArea notificationArea;

	private static String profile = "Main";
	
	private TxtMsgSender(AutoResponderDbAdapter dbAdapter, NotificationArea notificationArea) {
		this.dbAdapter = dbAdapter;
		this.notificationArea = notificationArea;
	}

	public void sendTextMessage(String telNumber) {
		boolean telNumberExists = telNumber != null && telNumber.length() > 0;
		if (telNumberExists) {
			String messageBody = dbAdapter.fetchMessageBody(profile);
			
			SmsManager smsMgr = SmsManager.getDefault();
			smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
			
			notificationArea.incrementRepliesCounter();
		}
	}

	public static void setProfile(String profile) {
		TxtMsgSender.profile = profile;
	}
	
	public static String getProfile() {
		return profile;
	}
	
	public static TxtMsgSender createAndSetUp(Context ctx, NotificationArea notificationArea) {
		AutoResponderDbAdapter dbAdapter = AutoResponderDbAdapter.initializeDatabase(ctx);
		TxtMsgSender txtMsgSender = new TxtMsgSender(dbAdapter, notificationArea);
		return txtMsgSender;
	}
	
}
