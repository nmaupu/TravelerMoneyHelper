package org.maupu.android.tmh.ui;

import org.maupu.android.tmh.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public abstract class SimpleDialog {
	public static Builder errorDialog(Context context, String title, String content) {
		return errorDialog(context, title, content, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
	}
	
	public static Builder errorDialog(Context context, String title, String content, OnClickListener listenerOk) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(content)
		.setTitle(title)
		.setCancelable(false)
		.setPositiveButton(android.R.string.ok, listenerOk);
		
		builder.create();
		return builder;
	}

	public static Builder confirmDialog(Context context, String confirmQuestion, OnClickListener listenerOk) {
		Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(confirmQuestion)
		.setCancelable(false)
		.setPositiveButton(R.string.yes, listenerOk)
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		builder.create();
		return builder;
	}
}
