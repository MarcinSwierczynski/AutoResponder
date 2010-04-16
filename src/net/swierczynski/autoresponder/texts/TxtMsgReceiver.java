package net.swierczynski.autoresponder.texts;

import java.util.ArrayList;

import net.swierczynski.autoresponder.TxtMsgSender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class TxtMsgReceiver extends BroadcastReceiver {

	private Context mCtx;

	public TxtMsgReceiver(Context ctx) {
		this.mCtx = ctx;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			answerToIncomingMessages(intent);
		}
	}

	private void answerToIncomingMessages(Intent intent) {
		SmsMessage[] msgs = getSmsMessagesFromIntent(intent);
		String[] numbers = getAuthorsNumber(msgs);
		sendMessagesToAuthors(numbers);
	}

	private SmsMessage[] getSmsMessagesFromIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for(int i = 0; i < msgs.length; i++) {
				byte[] pdu = (byte[]) pdus[i];
				SmsMessage msg = SmsMessage.createFromPdu(pdu);
				msgs[i] = msg;
			}
		}
		
		return msgs;
	}

	private String[] getAuthorsNumber(SmsMessage[] msgs) {
		ArrayList<String> phoneNumbers = new ArrayList<String>();
		for (SmsMessage message : msgs) {
			if(!message.isEmail()) {
				String number = message.getDisplayOriginatingAddress();
				phoneNumbers.add(number);
			}
		}
		return phoneNumbers.toArray(new String[] {});
	}
	
	private void sendMessagesToAuthors(String[] numbers) {
		TxtMsgSender msgSender = TxtMsgSender.getNewInstance(mCtx);
		for (String number : numbers) {
			msgSender.sendTextMessage(number);
		}
	}
}
