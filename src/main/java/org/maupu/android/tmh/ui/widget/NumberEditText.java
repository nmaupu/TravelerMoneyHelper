package org.maupu.android.tmh.ui.widget;

import org.maupu.android.tmh.util.NumberUtil;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class NumberEditText extends EditText implements TextWatcher {
	private boolean mEditing = true;

	public NumberEditText(Context context) {
		super(context);
		super.addTextChangedListener(this);
	}

	public NumberEditText(Context context, AttributeSet set) {
		super(context, set);
		super.addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		Log.d(NumberEditText.class.getCanonicalName(), "beforeTextChanged");
	}

	@Override
	public void afterTextChanged(Editable s) {
		Log.d(NumberEditText.class.getCanonicalName(), "afterTextChanged");
		mEditing = true;
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
		String currentNumberString = this.getText().toString();
		
		try {
			if(mEditing) {
				mEditing = false;
				
				String s = currentNumberString.replaceAll("\\s", "")
						.replaceAll(",", ".");
				Double currentNumberDouble = Double.parseDouble(s);

				//Log.d(NumberEditText.class.getCanonicalName(), "onTextChanged "+currentNumberString+" : "+start+" | "+lengthBefore+" | "+lengthAfter);
				
				// Setting formatted text
				if(currentNumberString.endsWith(".")) {
					this.setText(currentNumberString);
				} else {
					this.setText(NumberUtil.formatDecimalLocale(currentNumberDouble).replace(",", "."));
				}
			}
		} catch(NumberFormatException nfe) {
			// not a number yet - ignore that
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		
		// set cursor position to the end of EditText
		int pos = this.length();
		Editable ed = this.getText();
		Selection.setSelection(ed, pos);
	}
}
