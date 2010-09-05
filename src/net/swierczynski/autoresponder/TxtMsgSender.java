package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.history.SentSmsLogger;
import android.content.*;
import android.telephony.gsm.SmsManager;

public class TxtMsgSender {
	private static Context ctx;
	private static String profile = "Main";

	private AutoResponderDbAdapter dbAdapter;
	private NotificationArea notificationArea;
	
	private TxtMsgSender(AutoResponderDbAdapter dbAdapter, NotificationArea notificationArea) {
		this.dbAdapter = dbAdapter;
		this.notificationArea = notificationArea;
	}

	public void sendTextMessageIfPossible(String telNumber) {
		if (shouldSendMessage(telNumber)) {
			String messageBody = dbAdapter.fetchMessageBody(profile);
			
			SmsManager smsMgr = SmsManager.getDefault();
			smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
			saveMessageToHistory(telNumber, messageBody);
			notificationArea.incrementRepliesCounter();
		}
	}
	
	private boolean shouldSendMessage(String telNumber) {
		return telNumber != null && telNumber.length() > 0;
	}

	private void saveMessageToHistory(String telNumber, String messageBody) {
		Intent sentSmsLogger = new Intent(TxtMsgSender.ctx, SentSmsLogger.class);
		sentSmsLogger.putExtra("telNumber", telNumber);
		sentSmsLogger.putExtra("messageBody", messageBody);
		TxtMsgSender.ctx.startService(sentSmsLogger);
	}
	
	public static void setProfile(String profile) {
		TxtMsgSender.profile = profile;
	}
	
	public static String getProfile() {
		return profile;
	}
	
	public static TxtMsgSender createAndSetUp(Context ctx, NotificationArea notificationArea) {
		TxtMsgSender.ctx = ctx;
		
		AutoResponderDbAdapter dbAdapter = AutoResponderDbAdapter.initializeDatabase(ctx);
		TxtMsgSender txtMsgSender = new TxtMsgSender(dbAdapter, notificationArea);
		return txtMsgSender;
	}
	
}
