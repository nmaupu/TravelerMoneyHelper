package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionGrid;

import java.util.Map;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Category;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.widget.ViewPagerOperationAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class ViewPagerOperationActivity extends TmhActivity implements OnPageChangeListener, View.OnClickListener {
	private ViewPagerOperationAdapter adapter;
	private int currentPosition;
	private QuickActionGrid quickActionGrid;

    private Button goButton;
    private Class<?> addOrEditActivity;
    private ImageView imageCategory;
    private ImageView imageCurrency;
    private ImageView imageAccount;

    private boolean categoryIsOk = false;
    private boolean currencyIsOk = false;
    private boolean accountIsOk = false;

    private static final String CATEGORY_IS_OK = "categoryIsOk";
    private static final String CURRENCY_IS_OK = "currencyIsOk";
    private static final String ACCOUNT_IS_OK = "accountIsOk";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        retrieveBoolsFromDbs();
        categoryIsOk = StaticData.getPreferenceValueBoolean(CATEGORY_IS_OK);
        currencyIsOk = StaticData.getPreferenceValueBoolean(CURRENCY_IS_OK);
        accountIsOk = StaticData.getPreferenceValueBoolean(ACCOUNT_IS_OK);

        if(! categoryIsOk || ! currencyIsOk || ! accountIsOk) {
            setActionBarContentView(R.layout.welcome_activity);
            isApplicationInit();
        } else {
            setActionBarContentView(R.layout.viewpager_operation);
            setTitle("Operations");
        }

        // Force portrait
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        quickActionGrid = createQuickActionGridEdition();

        // Action bar items
        addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
        addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Withdrawal), TmhApplication.ACTION_BAR_ADD_WITHDRAWAL);
        addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getGDActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);

        adapter = new ViewPagerOperationAdapter(this);
        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(adapter);
        currentPosition = adapter.getCount() / 2;
        vp.setCurrentItem(currentPosition);

        vp.setOnPageChangeListener(this);
	}

    @Override
    protected void onDestroy() {
        TmhApplication.getDatabaseHelper().close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // Set booleans inside prefs
        retrieveBoolsFromDbs();

        if (isApplicationInit() && goButton != null) {
            goButton.setText(R.string.welcome_letsgo_button);
        }

        super.onResume();
    }

    @Override
    public void refreshDisplay() {
        retrieveBoolsFromDbs();
        if(!isApplicationInit())
            return;

        adapter.refreshItemView(currentPosition);
    }
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_ADD:
			// Add operation
			startActivityForResult(new Intent(this, AddOrEditOperationActivity.class), 0);
			break;
		case TmhApplication.ACTION_BAR_EDIT:
			quickActionGrid.show(item.getItemView());
			break;
		case TmhApplication.ACTION_BAR_ADD_WITHDRAWAL:
			startActivityFromMenu(WithdrawalActivity.class);
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);	
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("onActivityResult", "message="+resultCode);
		refreshDisplay();
	}

	@Override
	public void onPageScrollStateChanged(int position) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
	}

	// Not used
	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		return null;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> c) {
	}

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch(v.getId()) {
            case R.id.button:
                if(! isApplicationInit()) {
                    intent = new Intent(ViewPagerOperationActivity.this, addOrEditActivity);
                    intent.putExtra(AddOrEditActivity.EXTRA_APP_INIT, true);
                } else {
                    startActivity(new Intent(ViewPagerOperationActivity.this, TmhApplication.HOME_ACTIVITY_CLASS));
                    finish();
                }
                break;
        }

        if(intent != null)
            startActivity(intent);
    }

    private void retrieveBoolsFromDbs() {
        //Toast.makeText(this, "This is onResume method", Toast.LENGTH_LONG).show();
        Category category = new Category();
        StaticData.setPreferenceValueBoolean(CATEGORY_IS_OK, category.fetchAll().getCount() > 0);

        // There is already a currency (default one, used as a main currency for rate conversion)
        Currency currency = new Currency();
        StaticData.setPreferenceValueBoolean(CURRENCY_IS_OK, currency.fetchAll().getCount()>0);

        Account account = new Account();
        StaticData.setPreferenceValueBoolean(ACCOUNT_IS_OK, account.fetchAll().getCount()>0);
    }

    private boolean isApplicationInit() {
        categoryIsOk = StaticData.getPreferenceValueBoolean(CATEGORY_IS_OK);
        currencyIsOk = StaticData.getPreferenceValueBoolean(CURRENCY_IS_OK);
        accountIsOk = StaticData.getPreferenceValueBoolean(ACCOUNT_IS_OK);

        //categoryIsOk = currencyIsOk = accountIsOk = true;

        if(! categoryIsOk || ! currencyIsOk || ! accountIsOk) {
            goButton = (Button) findViewById(R.id.button);

            if(goButton == null) {
                // Not the good activity, restart it
                startActivity(new Intent(this, TmhApplication.HOME_ACTIVITY_CLASS));
                finish();
                return false;
            }

            goButton.setOnClickListener(this);

            imageCategory = (ImageView)findViewById(R.id.category_image);
            imageCurrency = (ImageView)findViewById(R.id.currency_image);
            imageAccount  = (ImageView)findViewById(R.id.account_image);

            if(! categoryIsOk) {
                imageCategory.setVisibility(View.VISIBLE);
                imageCategory.setImageResource(R.drawable.puce);
                addOrEditActivity = AddOrEditCategoryActivity.class;
            } else {
                imageCategory.setVisibility(View.VISIBLE);
                imageCategory.setImageResource(R.drawable.validate);
            }

            if(! currencyIsOk) {
                if(categoryIsOk) {
                    imageCurrency.setVisibility(View.VISIBLE);
                    imageCurrency.setImageResource(R.drawable.puce);
                    addOrEditActivity = AddOrEditCurrencyActivity.class;
                } else {
                    imageCurrency.setVisibility(View.INVISIBLE);
                }
            } else {
                imageCurrency.setVisibility(View.VISIBLE);
                imageCurrency.setImageResource(R.drawable.validate);
            }

            if(! accountIsOk) {
                if(categoryIsOk && currencyIsOk) {
                    imageAccount.setVisibility(View.VISIBLE);
                    imageAccount.setImageResource(R.drawable.puce);
                    addOrEditActivity = AddOrEditAccountActivity.class;
                } else {
                    imageAccount.setVisibility(View.INVISIBLE);
                }
            } else {
                imageAccount.setVisibility(View.VISIBLE);
                imageAccount.setImageResource(R.drawable.validate);
            }
        } else if (imageCategory != null) {
            // Account created !
            Account account = new Account();
            Cursor c = account.fetchAll();
            c.moveToFirst();
            account.toDTO(c);
            c.close();
            StaticData.setCurrentAccount(account);

            // Setting main currency to the one created earlier
            if(StaticData.getMainCurrency() == null) {
                Currency cur = new Currency();
                c = cur.fetchAll();
                c.moveToFirst();
                cur.toDTO(c);
                StaticData.setMainCurrency(cur.getId());
                c.close();
            }

            imageCategory.setImageResource(R.drawable.validate);
            imageCurrency.setImageResource(R.drawable.validate);
            imageAccount.setImageResource(R.drawable.validate);
        }

        return categoryIsOk && currencyIsOk && accountIsOk;
    }
}
