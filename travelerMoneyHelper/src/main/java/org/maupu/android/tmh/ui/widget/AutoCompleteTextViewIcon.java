package org.maupu.android.tmh.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import org.maupu.android.tmh.ui.ICallback;

import java.util.Map;

/**
 * Custom AutoCompleteTextView to have icon in autocompleted items
 * Use it with an adapter containing a list of Map item, each map must contained :
 *   "name" -> item name to display (String)
 *   "icon" -> item resource id (String)
 */
public class AutoCompleteTextViewIcon extends AutoCompleteTextView {
    private ICallback<?> onUpdateListener;

    public AutoCompleteTextViewIcon(Context context) {
        super(context);
    }

    public AutoCompleteTextViewIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteTextViewIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnUpdateListener(ICallback listener) {
        this.onUpdateListener = listener;
    }

    public ICallback<?> getOnUpdateListener() {
        return onUpdateListener;
    }

    @Override
    protected void replaceText(CharSequence text) {
        super.replaceText(text);
        if(onUpdateListener != null)
            onUpdateListener.callback(text);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        Map<String, ?> item = (Map<String, ?>) selectedItem;
        return (String)item.get("name");
    }
}
