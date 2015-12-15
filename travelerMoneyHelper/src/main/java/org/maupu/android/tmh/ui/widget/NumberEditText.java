package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.rengwuxian.materialedittext.MaterialEditText;

public class NumberEditText extends MaterialEditText implements TextWatcher {

	public NumberEditText(Context context) {
		super(context);
		super.addTextChangedListener(this);
	}

	public NumberEditText(Context context, AttributeSet set) {
		super(context, set);
		super.addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {
        super.removeTextChangedListener(this);

        String currentNumberString = this.getStringText();
        this.setText(currentNumberString);

        // set cursor position to the end of EditText
        int pos = this.length();
        Editable ed = this.getText();
        Selection.setSelection(ed, pos);

        super.addTextChangedListener(this);
    }

	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {}
	
	public String getStringText() {
		return super.getText().toString().replaceAll(",", ".").replaceAll("\\s*", "");
	}
}
