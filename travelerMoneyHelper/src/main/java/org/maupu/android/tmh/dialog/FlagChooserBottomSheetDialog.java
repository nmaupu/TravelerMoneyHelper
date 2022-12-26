package org.maupu.android.tmh.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ui.Flag;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.widget.AutoCompleteTextViewIcon;
import org.maupu.android.tmh.ui.widget.SimpleIconAdapter;

public class FlagChooserBottomSheetDialog extends BottomSheetDialogFragment {
    private final SimpleIconAdapter mAdapter;
    private BottomSheetDialogListener<Flag> onValidate;
    private AutoCompleteTextViewIcon textView;

    public FlagChooserBottomSheetDialog(SimpleIconAdapter adapter, BottomSheetDialogListener<Flag> onValidate) {
        this.mAdapter = adapter;
        this.onValidate = onValidate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_flags_icon, container, false);

        textView = (AutoCompleteTextViewIcon) v.findViewById(R.id.edit);
        textView.setOnUpdateListener(item -> {
            // Text field is updated so we are called and we can now set the flag icon
            Flag flag = Flag.getFlagFromCountry(requireContext(), (String) item);
            if (flag != null) {
                onValidate.onValidateEvent(
                        v,
                        this,
                        Flag.getFlagFromCountry(v.getContext(), textView.getText().toString()));
            }
            return null;
        });
        textView.setAdapter(mAdapter);
        textView.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
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
