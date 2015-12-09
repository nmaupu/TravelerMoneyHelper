package org.maupu.android.tmh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;

import java.util.Map;

/**
 * Activity displayed at first activity in order to get ready first app launch
 * or first database init.
 */
public class FirstActivity extends TmhActivity implements View.OnClickListener {
    private ImageView categoryImg, currencyImg, accountImg;
    private Button goBtn;
    private int nbCat = 0;
    private int nbAcc = 0;
    private int nbCur = 0;

    public FirstActivity() {
        super(R.layout.first, R.string.welcome_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Widgets **/
        goBtn = (Button)findViewById(R.id.button);
        accountImg = (ImageView)findViewById(R.id.account_image);
        categoryImg = (ImageView)findViewById(R.id.category_image);
        currencyImg = (ImageView)findViewById(R.id.currency_image);

        /** Events **/
        goBtn.setOnClickListener(this);
    }

    @Override
    public void refreshDisplay() {
        /** Database checks **/
        nbCat = new Category().getCount();
        nbCur = new Currency().getCount();
        nbAcc = new Account().getCount();

        Log.d(FirstActivity.class.getName(), "Init :");
        Log.d(FirstActivity.class.getName(), "  nb cat = "+nbCat);
        Log.d(FirstActivity.class.getName(), "  nb cur = "+nbCur);
        Log.d(FirstActivity.class.getName(), "  nb acc = "+nbAcc);

        if(nbCat > 0 && nbCur > 0 && nbAcc > 0) {
            startActivity(new Intent(this, ViewPagerOperationActivity.class));
            this.finish();
        }

        if(nbCat > 0) {
            categoryImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            categoryImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

        if(nbAcc > 0) {
            accountImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            accountImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

        if(nbCur > 0) {
            currencyImg.setImageDrawable(getResources().getDrawable(R.drawable.validate));
        } else {
            currencyImg.setImageDrawable(getResources().getDrawable(R.drawable.puce));
        }

        super.refreshDisplay();
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        if(nbCat == 0) {
            intent = new Intent(this, AddOrEditCategoryActivity.class);
        } else if(nbCur == 0) {
            intent = new Intent(this, AddOrEditCurrencyActivity.class);
        } else if(nbAcc == 0) {
            intent = new Intent(this, AddOrEditAccountActivity.class);
        } else {
            intent = new Intent(this, ViewPagerOperationActivity.class);
        }

        startActivity(intent);
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() { return null; }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {}
}
