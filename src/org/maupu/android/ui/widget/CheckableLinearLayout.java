package org.maupu.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private CheckBox checkbox;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// find checked text view
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckBox) {
				checkbox = (CheckBox)v;
			}
		} 
	}

	@Override
	public boolean isChecked() {
		return checkbox != null ? checkbox.isChecked() : false;
	}

	@Override
	public void setChecked(boolean checked) {
		if(checkbox != null)
			checkbox.setChecked(checked);
	}

	@Override
	public void toggle() {
		if(checkbox != null)
			setChecked(!checkbox.isChecked());
	}

}
