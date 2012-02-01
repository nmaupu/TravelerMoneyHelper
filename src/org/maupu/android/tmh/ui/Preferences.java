package org.maupu.android.tmh.ui;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Account;

import android.database.Cursor;


public abstract class Preferences {
	public static Account currentAccount = new Account();
	
	public static void init(final DatabaseHelper dbHelper) {
		// TODO Get from SD before
		Cursor c = currentAccount.fetch(dbHelper, 1);
		currentAccount.toDTO(dbHelper, c);
	}
	
	public static void replaceCurrentAccount(final DatabaseHelper dbHelper, Account account) {
		if(account == null || account.getId() == null)
			return;
		
		// Fetch and fill currentAccount
		Cursor c = currentAccount.fetch(dbHelper, account.getId());
		currentAccount.toDTO(dbHelper, c);
	}
}
