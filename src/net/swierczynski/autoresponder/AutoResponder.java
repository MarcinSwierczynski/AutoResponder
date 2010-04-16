package net.swierczynski.autoresponder;

import net.swierczynski.autoresponder.calls.UnreceivedCallsService;
import net.swierczynski.autoresponder.texts.IncomingMsgsService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class AutoResponder extends Activity {
	private AutoResponderDbAdapter dbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
		
		dbAdapter = AutoResponderDbAdapter.initializeDatabase(this);
		registerCallsCheckboxListener();
		registerTextsCheckboxListener();
		displayProfilesSpinner();
		registerConfirmButtonListener();
	}

	private void registerCallsCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enable_calls);
		enabledCheckbox.setChecked(UnreceivedCallsService.isActive);
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				setServiceState(enabled, "calls");
			}

		});
	}
	
	private void registerTextsCheckboxListener() {
		final CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.enable_texts);
		enabledCheckbox.setChecked(IncomingMsgsService.isActive);
		enabledCheckbox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean enabled = cb.isChecked();
				setServiceState(enabled, "texts");
			}
		});
	}

	private void displayProfilesSpinner() {
		Spinner profilesSpinner = (Spinner) findViewById(R.id.profile);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.profiles_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    profilesSpinner.setAdapter(adapter);
	    profilesSpinner.setSelection(adapter.getPosition(TxtMsgSender.getProfile()));
	    
	    registerProfilesSpinnerListener(profilesSpinner);
	}

	private void registerProfilesSpinnerListener(Spinner profilesSpinner) {
		profilesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String choosenProfile = parent.getItemAtPosition(pos).toString();
				TxtMsgSender.setProfile(choosenProfile);
				fillMessageBodyField();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}
	
	private void setServiceState(boolean enabled, String mode) {
		Intent service = new Intent(this, AutoResponderService.class);
		service.putExtra("isEnabled", enabled);
		service.putExtra("mode", mode);
		startService(service);
	}
	
	private void fillMessageBodyField() {
		String text = dbAdapter.fetchMessageBody(TxtMsgSender.getProfile());
		setMessageContent(text);
	}

	private void registerConfirmButtonListener() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String msgBody = getMessageContent();
				persistMessageContent(msgBody);
				showConfirmation();
			}

			private void showConfirmation() {
				Context context = getApplicationContext();
				String text = getText(R.string.message_saved) + " " + TxtMsgSender.getProfile();
				int duration = Toast.LENGTH_LONG;
				Toast confirmationMessage = Toast.makeText(context, text, duration);
				confirmationMessage.show();
			}
		});
	}
	
	private EditText getMessageBodyField() {
		return (EditText) findViewById(R.id.body);
	}
	
	private void setMessageContent(String content) {
		getMessageBodyField().setText(content);
	}
	
	private String getMessageContent() {
		return getMessageBodyField().getText().toString();
	}
	
	private void persistMessageContent(String content) {
		dbAdapter.saveMessage(TxtMsgSender.getProfile(), content);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String content = getMessageContent();
		persistMessageContent(content);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}

}