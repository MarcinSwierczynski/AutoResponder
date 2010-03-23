package net.swierczynski.autoresponder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AutoResponder extends Activity {
	private static final String TAG = AutoResponder.class.getName();
	protected static final String PROFILE = "Main";

	private AutoResponderDbAdapter dbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		dbAdapter = AutoResponderDbAdapter.initializeDatabase(this);
		registerCheckboxListener();
		fillMessageBodyField();
		registerConfirmButtonListener();
	}

	private void registerCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enabled);
		enabledCheckbox.setChecked(UnreceivedCallsHandlerService.IS_RUNNING);
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				setServiceState(enabled);
			}

		});
	}

	private void setServiceState(boolean enabled) {
		Intent service = new Intent(this, UnreceivedCallsHandlerService.class);
		if (enabled) {
			startService(service);
		} else {
			stopService(service);
		}
	}

	private void fillMessageBodyField() {
		EditText msgBodyField = (EditText) findViewById(R.id.body);
		String text = dbAdapter.fetchMessageBody(PROFILE);
		msgBodyField.setText(text);
	}

	private void registerConfirmButtonListener() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText msgBodyField = (EditText) findViewById(R.id.body);
				String msgBody = msgBodyField.getText().toString();
				dbAdapter.saveMessage(PROFILE, msgBody);
			}
		});
	}

}