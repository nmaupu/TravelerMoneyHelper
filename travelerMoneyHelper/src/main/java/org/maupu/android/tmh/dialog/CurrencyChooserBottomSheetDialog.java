package org.maupu.android.tmh.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;

public class CurrencyChooserBottomSheetDialog extends BottomSheetDialogFragment {
    private final ArrayAdapter<CurrencyISO4217> mAdapter;
    private CurrencyISO4217 mCurrency;
    private MaterialAutoCompleteTextView textView;
    private View.OnClickListener listener;

    public CurrencyChooserBottomSheetDialog(ArrayAdapter<CurrencyISO4217> adapter) {
        this.mAdapter = adapter;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public CurrencyISO4217 getCurrency() {
        return mCurrency;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_currency_chooser, container, false);

        textView = v.findViewById(R.id.edit);
        textView.setAdapter(mAdapter);
        textView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        textView.setOnItemClickListener((parent, view, position, id) -> {
            mCurrency = (CurrencyISO4217) parent.getAdapter().getItem(position);
            listener.onClick(v);
            dismiss();
        });

        textView.setDropDownAnchor(R.id.layout_root);
        textView.setDropDownVerticalOffset(-2);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView.requestFocus();
        SoftKeyboardHelper.hide(requireActivity());
        SoftKeyboardHelper.forceShowUp(requireActivity());
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }
}
