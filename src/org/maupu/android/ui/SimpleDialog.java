package org.maupu.android.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public abstract class SimpleDialog {
	public static Builder errorDialog(Context context, String title, String content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(content)
		.setTitle(title)
		.setCancelable(false)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		
		builder.create();
		return builder;
	}

	public static Builder confirmDialog(Context context, String confirmQuestion, OnClickListener listenerOk) {
		Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(confirmQuestion)
		.setCancelable(false)
		.setPositiveButton("Yes", listenerOk)
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		builder.create();
		return builder;
	}
}
