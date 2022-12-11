package org.maupu.android.tmh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.databinding.ActivityFirstBinding;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.util.TmhLogger;

/**
 * Activity displayed as first activity in order to get stuff ready and configured.
 */
public class FirstActivity extends AppCompatActivity implements View.OnClickListener {
    private static final Class<FirstActivity> TAG = FirstActivity.class;
    private ImageView categoryImg, currencyImg, accountImg;
    private int nbCat = 0;
    private int nbAcc = 0;
    private int nbCur = 0;

    public FirstActivity() {
        super(R.layout.activity_first);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isRequiredStuffOk())
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

    public boolean isRequiredStuffOk() {
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
        if (isRequiredStuffOk()) {
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
        Intent intent;
        boolean finish = false;

        // TODO use fragments
        /*if (nbCat == 0) {
            intent = new Intent(this, AddOrEditCategoryActivity.class);
        } else if (nbCur == 0) {
            intent = new Intent(this, AddOrEditCurrencyActivity.class);
        } else if (nbAcc == 0) {
            intent = new Intent(this, AddOrEditAccountActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
            finish = true;
        }

        startActivity(intent);
        */
        if (finish)
            this.finish();
    }
}
