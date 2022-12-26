package org.maupu.android.tmh.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import org.maupu.android.tmh.R;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.NumberUtil;
import org.maupu.android.tmh.util.TmhLogger;

import java.util.Currency;

public class OperationCursorAdapter extends SimpleCursorAdapter {
    private static final Class TAG = OperationCursorAdapter.class;

    public OperationCursorAdapter(Context context, int layout,
                                  Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        TextView tvAmount = view.findViewById(R.id.amount);
        int idxAmount = cursor.getColumnIndex(OperationData.KEY_AMOUNT);
        Double amount = cursor.getDouble(idxAmount);

        int idxCurrencySymbol = cursor.getColumnIndex(CurrencyData.KEY_SHORT_NAME);
        String currencySymbol = cursor.getString(idxCurrencySymbol);

        TextView tvConvAmount = view.findViewById(R.id.convAmount);
        int idxConvAmount = cursor.getColumnIndex("convertedAmount");
        String convAmountStr = cursor.getString(idxConvAmount);
        if (convAmountStr == null)
            convAmountStr = "0";
        Double convAmount = Double.parseDouble(convAmountStr);

        try {
            // format amount
            tvAmount.setText(NumberUtil.formatDecimal(amount) + " " + currencySymbol);
            tvConvAmount.setText(NumberUtil.formatDecimal(convAmount) + " " +
                    Currency.getInstance(StaticData.getMainCurrency().getIsoCode()).getSymbol());
        } catch (NullPointerException npe) {
            // Nothing done
            TmhLogger.e(TAG, "Main currency is null or not set yet !");
        }

        // Set color for amount
        if (amount >= 0d)
            tvAmount.setTextColor(Operation.COLOR_POSITIVE_AMOUNT);
        else
            tvAmount.setTextColor(Operation.COLOR_NEGATIVE_AMOUNT);

        // Specify if this operation is credit/debit card or not
        ImageView creditCardView = view.findViewById(R.id.imageViewIsCard);
        int idxIsCash = cursor.getColumnIndexOrThrow(OperationData.KEY_IS_CASH);
        boolean isCash = cursor.getInt(idxIsCash) > 0;
        if (!isCash) {
            creditCardView.setImageResource(R.drawable.ic_baseline_credit_card_black_24);
            creditCardView.setColorFilter(view.getResources().getColor(R.color.black_overlay));
            creditCardView.setScaleX(.75f);
            creditCardView.setScaleY(.75f);
        }
    }

}
