package net.swierczynski.autoresponder;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.Phones;


public class NumberInPhonebookChecker {

	public static boolean isInPhonebook(Context ctx, String number) {
		Uri phonebook = Phones.CONTENT_URI;
		String[] projection = new String[] { Phones.NAME };
		
		Cursor c = null;
		try {
			ContentResolver resolver = ctx.getContentResolver();
			c = resolver.query(phonebook, projection, Phones.NUMBER + "= '" + number + "'", null, null);
			return checkIfNumberIsInResults(c);
		} finally {
			closeCursor(c);
		}
 	}

	private static boolean checkIfNumberIsInResults(Cursor c) {
		if (c != null && c.getCount() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private static void closeCursor(Cursor c) {
		if (c != null) {
			c.close();
		}
	}
}
