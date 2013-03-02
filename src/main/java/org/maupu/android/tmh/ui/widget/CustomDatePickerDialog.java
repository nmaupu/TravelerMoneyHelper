package org.maupu.android.tmh.ui.widget;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

public class CustomDatePickerDialog extends DatePickerDialog {
	public CustomDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		updateTitle(year, monthOfYear, dayOfMonth);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		super.onDateChanged(view, year, month, day);
		updateTitle(year, month, day);
	}
	
	private void updateTitle(int year, int month, int day) {
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.set(Calendar.YEAR, year);
		mCalendar.set(Calendar.MONTH, month);
		mCalendar.set(Calendar.DAY_OF_MONTH, day);
		setTitle(getFormat().format(mCalendar.getTime()));
	} 
	
	public DateFormat getFormat(){
		return DateFormat.getDateInstance();
	}
}
