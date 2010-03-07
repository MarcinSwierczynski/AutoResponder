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
	private NewUnreceivedCallListener unreceivedCallListener;
	private TxtMsgSender txtMsgSender;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    
        initalizeDatabase();
        initializeApplicationState(savedInstanceState);
        registerUnreceivedCallListener();
        registerCheckboxListener();
        fillMessageBodyField();
        registerConfirmButtonListener();
        createIconInNotificationArea();        
    }

	private void fillMessageBodyField() {
		EditText msgBodyField= (EditText) findViewById(R.id.body);
        String text = dbAdapter.fetchMessageBody(PROFILE);
        msgBodyField.setText(text);
	}

	private void registerConfirmButtonListener() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText msgBodyField= (EditText) findViewById(R.id.body);
				String msgBody = msgBodyField.getText().toString();
				dbAdapter.saveMessage(PROFILE, msgBody);
			}
		});
	}

	private void registerCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enabled);
		enabledCheckbox.setChecked(unreceivedCallListener.isEnabled());
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				unreceivedCallListener.setEnabled(enabled);
			}
		});
	}
	
	private void initializeApplicationState(Bundle savedInstanceState) {
		txtMsgSender = TxtMsgSender.getInstance(dbAdapter);
		txtMsgSender.setProfile(PROFILE);
		unreceivedCallListener = NewUnreceivedCallListener.getInstance(txtMsgSender);
		
		boolean firstApplicationRun = savedInstanceState == null;
		if(firstApplicationRun) {
			unreceivedCallListener.setEnabled(true);
		} else {
			unreceivedCallListener.setEnabled(savedInstanceState.getBoolean("enabled"));
		}
	}

	private void registerUnreceivedCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(unreceivedCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void initalizeDatabase() {
		dbAdapter = new AutoResponderDbAdapter(this);
        dbAdapter.open();
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("enabled", unreceivedCallListener.isEnabled());
	}

	private void createIconInNotificationArea() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.icon;
		String text = "AutoResponder";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, text, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = "Missed calls checking is active!";
		Intent notificationIntent = new Intent(this, AutoResponderConfig.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notificationManager.notify(1, notification);
	}
}