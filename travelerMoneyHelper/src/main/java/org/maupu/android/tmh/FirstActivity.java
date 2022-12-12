package org.maupu.android.tmh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.databinding.ActivityFirstBinding;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.TmhLogger;

/**
 * Activity displayed as first activity in order to get stuff ready and configured.
 */
public class FirstActivity extends TmhActivity implements View.OnClickListener {
    private static final Class<FirstActivity> TAG = FirstActivity.class;

    private ImageView categoryImg, currencyImg, accountImg;
    private int nbCat = 0;
    private int nbAcc = 0;
    private int nbCur = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkRequiredStuff())
            return;

        setTitle(R.string.app_name);

        ActivityFirstBinding binding = ActivityFirstBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Widgets
        Button goBtn = findViewById(R.id.button);
        accountImg = findViewById(R.id.account_image);
        categoryImg = findViewById(R.id.category_image);
        currencyImg = findViewById(R.id.currency_image);
        goBtn.setOnClickListener(this);

        // Ensure keyboard is down
        SoftKeyboardHelper.hide(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (nbCat == 0) {
            // Create default category
            String catName = getString(R.string.default_category_withdrawal_name);
            Category defaultCategory = new Category();
            defaultCategory.setName(catName);
            if (defaultCategory.insert()) {
                StaticData.setWithdrawalCategory(defaultCategory);
                nbCat++;
            }
        }

        if (nbCur == 0 || nbAcc == 0) {
            String countryCode = TmhApplication.getCountryCode(this);
            String countryName = TmhApplication.getCountryNameFromCountryCode(countryCode);
            CurrencyISO4217 currency = CurrencyISO4217.getCurrencyISO4217FromCountryCode(countryCode);
            TmhLogger.d(TAG, "Country code: " + countryCode + ", country name: " + countryName + ", country currency: " + currency.toString());

            // Creating the primary Currency
            Currency cur = new Currency();
            cur.setIsoCode(currency.getCode());
            cur.setLongName(currency.getName());
            cur.setShortName(currency.getCurrencySymbol());
            cur.insert();

            // Creating the primary Account
            Account acc = new Account();
            acc.setName(countryName);
            acc.setCurrency(cur);
            acc.insert();
        }

        refreshDisplay();
    }

    public boolean checkRequiredStuff() {
        // Database checks
        nbCat = new Category().getCount();
        nbCur = new Currency().getCount();
        nbAcc = new Account().getCount();

        TmhLogger.d(TAG, "Init :");
        TmhLogger.d(TAG, "  nb cat = " + nbCat);
        TmhLogger.d(TAG, "  nb cur = " + nbCur);
        TmhLogger.d(TAG, "  nb acc = " + nbAcc);

        if (nbCat > 0 && nbCur > 0 && nbAcc > 0) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
            return true;
        }
        return false;
    }

    public void refreshDisplay() {
        if (checkRequiredStuff()) {
            return;
        }

        if (nbCat > 0) {
            categoryImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            categoryImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

        if (nbAcc > 0) {
            accountImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            accountImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

        if (nbCur > 0) {
            currencyImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            currencyImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

    }

    @Override
    public void onClick(View v) {
        if (nbCur == 0) {
            changeFragment(AddOrEditCategoryFragment.class, true, null);
        } else if (nbAcc == 0) {
            changeFragment(AddOrEditAccountFragment.class, true, null);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
