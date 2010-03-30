package net.swierczynski.autoresponder;

import android.app.Activity;
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
import android.widget.AdapterView.OnItemSelectedListener;

public class AutoResponder extends Activity {
	private static final String TAG = AutoResponder.class.getName();
	private String profile = "Main";

	private AutoResponderDbAdapter dbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		dbAdapter = AutoResponderDbAdapter.initializeDatabase(this);
		registerCheckboxListener();
		displayProfilesSpinner();
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

	private void displayProfilesSpinner() {
		Spinner profilesSpinner = (Spinner) findViewById(R.id.profile);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.profiles_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    profilesSpinner.setAdapter(adapter);
	    
	    registerProfilesSpinnerListener(profilesSpinner);
	}

	private void registerProfilesSpinnerListener(Spinner profilesSpinner) {
		profilesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String choosenProfile = parent.getItemAtPosition(pos).toString();
				setProfile(choosenProfile);
				fillMessageBodyField();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
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
		String text = dbAdapter.fetchMessageBody(profile);
		msgBodyField.setText(text);
	}

	private void registerConfirmButtonListener() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText msgBodyField = (EditText) findViewById(R.id.body);
				String msgBody = msgBodyField.getText().toString();
				dbAdapter.saveMessage(profile, msgBody);
			}
		});
	}
	
	public void setProfile(String profile) {
		this.profile = profile;
	}

}