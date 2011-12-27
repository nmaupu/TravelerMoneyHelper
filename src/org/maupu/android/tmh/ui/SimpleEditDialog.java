package org.maupu.android.tmh.ui;

import org.maupu.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SimpleEditDialog extends AlertDialog {
	private Button buttonOk;
	private Button buttonCancel;
	private EditText editText;
	
	public SimpleEditDialog(Context context, String titleDialog, String fieldHeader, String editTextContent, ViewGroup rootWindow) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_simple_edit, rootWindow);

		super.setTitle(titleDialog);
		super.setView(layout);

		buttonOk = (Button)layout.findViewById(R.id.simple_dialog_button_ok);
		buttonCancel = (Button)layout.findViewById(R.id.simple_dialog_button_cancel);
		editText = (EditText)layout.findViewById(R.id.simple_dialog_edit_text);
		editText.setText(editTextContent);
	}
	
	public void setOnClickListener(View.OnClickListener listener) {
		buttonOk.setOnClickListener(listener);
		buttonCancel.setOnClickListener(listener);	
	}
	
	public int getButtonOkId() {
		return buttonOk.getId();
	}
	
	public int getButtonCancelId() {
		return buttonCancel.getId();
	}
	
	public String getText() {
		return editText.getText().toString();
	}
}
