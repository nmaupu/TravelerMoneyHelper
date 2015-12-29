package org.maupu.android.tmh.ui.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.maupu.android.tmh.AddOrEditActivity;
import org.maupu.android.tmh.AddOrEditOperationActivity;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ViewPagerOperationActivity;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.AFilter;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.AccountBalance;
import org.maupu.android.tmh.ui.DialogHelper;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;
import org.maupu.android.tmh.util.NumberUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class OperationPagerItem implements OnClickListener, NumberCheckedListener, IAsyncActivityRefresher {
	private ViewPagerOperationActivity viewPagerOperationActivity;
	private View view;
	private Date date;
	private Button editButton;
	private Button deleteButton;
	private ListView listView;
	private LayoutInflater inflater;
	private ImageView imageViewIcon;
	private TextView textViewAccountName;
	private TextView textViewTitleMonth;
	private TextView textViewTitleYear;
	private View footerOperationBalance;
	private TextView textViewTotal;
	private TextView textViewBalance;

	public OperationPagerItem(ViewPagerOperationActivity ctx, LayoutInflater inflater, Date date) {
		this.viewPagerOperationActivity = ctx;
		this.date = date;
		this.inflater = inflater;

		view = inflater.inflate(R.layout.manageable_object, null);

		init();
		initButtons();
		refreshDisplay();
	}

	private void init() {
		createHeader();
		createFooterTotal();

        // Avoid having toolbar twice
        view.findViewById(R.id.tmh_toolbar).setVisibility(View.GONE);

		this.editButton = (Button)view.findViewById(R.id.button_edit);
		this.deleteButton = (Button)view.findViewById(R.id.button_delete);

		if(this.editButton != null)
			this.editButton.setOnClickListener(this);

		if(this.deleteButton != null)
			this.deleteButton.setOnClickListener(this);
	}

	private void createHeader() {
		// Getting header
		View headerContent = inflater.inflate(R.layout.viewpager_operation_header, null);
		LinearLayout header = (LinearLayout)view.findViewById(R.id.header);

		// Getting resources
		textViewTitleMonth = (TextView)headerContent.findViewById(R.id.title_month);
		textViewTitleYear = (TextView)headerContent.findViewById(R.id.title_year);
		textViewAccountName = (TextView)headerContent.findViewById(R.id.account_name); 
		imageViewIcon = (ImageView)headerContent.findViewById(R.id.account_icon);
		imageViewIcon.setOnClickListener(this);

		// Adding content to viewpager header
		header.setVisibility(View.VISIBLE);
		header.addView(headerContent, 0);
	}

	private void createFooterTotal() {
		footerOperationBalance = inflater.inflate(R.layout.manageable_operation_total, null);
		LinearLayout content = (LinearLayout)view.findViewById(R.id.layout_root);

		textViewTotal = (TextView)footerOperationBalance.findViewById(R.id.total);
		textViewBalance = (TextView)footerOperationBalance.findViewById(R.id.balance);
		content.addView(footerOperationBalance, content.getChildCount()-2);
	}

	public View getView() {
		return view;
	}

	@Override
	public void onClick(View v) {
		final Operation obj = new Operation();
		Intent intent = null;
        CheckableCursorAdapter checkableCursorAdapter = null;
		// Bug fix : when changing account name, listview is null
		if(listView == null)
			refreshDisplay();
        else
		    checkableCursorAdapter = (CheckableCursorAdapter)listView.getAdapter();

        final Integer[] posChecked;
		if(checkableCursorAdapter != null)
			posChecked = checkableCursorAdapter.getCheckedPositions();
		else
			return;

		switch(v.getId()) {
            case R.id.account_icon:
                Log.d(OperationPagerItem.class.getName(), "Icon clicked");
                DialogHelper.popupDialogAccountChooser(viewPagerOperationActivity);
                break;
            case R.id.button_edit:
                if(posChecked.length == 1) {
                    int p = posChecked[0];
                    Cursor cursor = (Cursor)listView.getItemAtPosition(p);
                    obj.toDTO(cursor);

                    intent = new Intent(viewPagerOperationActivity, AddOrEditOperationActivity.class);
                    intent.putExtra(AddOrEditActivity.EXTRA_OBJECT_ID, obj);
                    viewPagerOperationActivity.startActivityForResult(intent, 0);
                }

                break;
            case R.id.button_delete:
                SimpleDialog.confirmDialog(viewPagerOperationActivity,
                        viewPagerOperationActivity.getString(R.string.manageable_obj_del_confirm_question),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String errMessage = viewPagerOperationActivity.getString(R.string.manageable_obj_del_error);
                        boolean err = false;

                        for(int i=0; i<posChecked.length; i++) {
                            Integer pos = posChecked[i];
                            //Request deletion
                            Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
                            obj.toDTO(cursor);
                            obj.delete();
                        }

                        if(err)
                            SimpleDialog.errorDialog(viewPagerOperationActivity, viewPagerOperationActivity.getString(R.string.error), errMessage).show();

                        dialog.dismiss();
                        refreshDisplay();
                    }
                }).show();
                break;
		}
	}

	private void setEnabledDeleteButton(boolean enabled) {
		if(this.deleteButton != null) {
            this.deleteButton.setEnabled(enabled);
            this.deleteButton.setAlpha(enabled ? 1f : 0.4f);
        }
	}

	private void setEnabledEditButton(boolean enabled) {
		if(this.editButton != null) {
            this.editButton.setEnabled(enabled);
            this.editButton.setAlpha(enabled ? 1f : 0.4f);
        }
	}

	private void initButtons() {
		setEnabledDeleteButton(false);
		setEnabledEditButton(false);
		buttonsBarGone();
	}

	private void buttonsBarVisible() {
		setVisibilityButtonsBar(R.anim.pushup, true);
	}

	private void buttonsBarGone() {
		setVisibilityButtonsBar(R.anim.pushdown, false);
	}

	public void closeCursors() {
		try {
			((CheckableCursorAdapter)listView.getAdapter()).getCursor().close();
		} catch(NullPointerException npe) {
			// Nothing to close
		}
	}

	private void setVisibilityButtonsBar(int anim, boolean visible) {
		View v = view.findViewById(R.id.layout_root_footer);

		// Already ok
		if((!visible && v.getVisibility() == View.GONE) || (visible && v.getVisibility() == View.VISIBLE))
			return;

		if(visible) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
		}

		Animation animation = AnimationUtils.loadAnimation(v.getContext(), anim);
		if(anim == R.anim.pushup)
			animation.setInterpolator(new DecelerateInterpolator());
		else
			animation.setInterpolator(new AccelerateInterpolator());

		v.startAnimation(animation);
	}

	public void refreshHeader() {
		Account account = StaticData.getCurrentAccount();

		// Setting parameters - account should not be null
		if(account != null) {
			ImageViewHelper.setIcon(viewPagerOperationActivity, imageViewIcon, account.getIcon());
			textViewAccountName.setText(account.getName());
		}

        if(this.date != null) {
            SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM");
            String dateString = sdfMonth.format(this.date);
            Log.d("OperationPagerItem", "month displayed : " + dateString);
            textViewTitleMonth.setText(dateString);

            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            dateString = sdfYear.format(this.date);
            textViewTitleYear.setText(dateString);
        } else {
            // No date specified, displaying all
            textViewTitleMonth.setText(getView().getResources().getString(R.string.operation_all_main_title));
            textViewTitleYear.setText(getView().getResources().getString(R.string.operation_all_sub_title));
        }
	}

	@Override
	public void onCheckedItem(int numberItemsChecked) {
		// Change state of buttonEdit and buttonDelete
		switch(numberItemsChecked) {
		case 0:
			buttonsBarGone();
			break;
		case 1:
			setEnabledDeleteButton(true);
			setEnabledEditButton(true);
			buttonsBarVisible();
			break;
		default:
            setEnabledDeleteButton(true);
            setEnabledEditButton(false);
		}
	}

	public void refreshDisplay() {
		// Getting current account
		Account currentAccount = StaticData.getCurrentAccount();
		
		// TODO - maybe, invalidate "static current account" to force reload instead of this shit
	/*	if(currentAccount == null || currentAccount.getId() == null) {
			// no current account, set default one
			Account acc = new Account();
			Cursor c = acc.fetchAll();
			c.moveToFirst();
			acc.toDTO(c);
			StaticData.setCurrentAccount(acc);
			currentAccount = acc;
		}*/

		// Process list
		Operation dummy = new Operation();
		dummy.getFilter().addFilter(AFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(currentAccount.getId()));

        Cursor cAllOp, cSumOp;
        if(this.date != null) {
            cAllOp = dummy.fetchByMonth(date);
            cSumOp = dummy.sumOperationsByMonth(currentAccount, date, null);
        } else {
            cAllOp = dummy.fetchAll();
            cSumOp = dummy.sumOperations(currentAccount, null);
        }

        if(cSumOp == null || cAllOp == null) {
            /* An error occured */
            return;
        }


		// Process balance
		Currency mainCur = StaticData.getMainCurrency();
		String symbolCurrency;
		try {
			symbolCurrency = java.util.Currency.getInstance(mainCur.getIsoCode()).getSymbol();
		} catch(NullPointerException npe) {
			symbolCurrency = java.util.Currency.getInstance(new Locale("fr", "FR")).getSymbol();
		}

		AccountBalance balance = currentAccount.getComputedBalance();
		StringBuilder sbBalance = new StringBuilder();
		if(balance.size() == 1) {
			Set<Integer> s = balance.keySet();
			Iterator<Integer> it = s.iterator();
			Integer curId = it.next();

			Double b = balance.get(curId);

			Currency cur = new Currency();
			Cursor cursor = cur.fetch(curId);
			cur.toDTO(cursor);
			cursor.close();

			sbBalance.append(NumberUtil.formatDecimal(b))
			.append(" ")
			.append(cur.getShortName())
			.append(" / ");
		}

		sbBalance.append(NumberUtil.formatDecimal(balance.getBalanceRate()))
		.append(" ")
		.append(symbolCurrency);


		Double total = 0d;
		int nbRes = cSumOp.getCount();
		boolean sameCurrency = (nbRes == 1);
		for(int i=0; i<nbRes; i++) {
			int idxSum = cSumOp.getColumnIndexOrThrow(Operation.KEY_SUM);
			int idxRate = cSumOp.getColumnIndexOrThrow(CurrencyData.KEY_CURRENCY_LINKED);
			int idxCurrencyShortName = cSumOp.getColumnIndexOrThrow(CurrencyData.KEY_SHORT_NAME);

			float amount = cSumOp.getFloat(idxSum);
			float rate = cSumOp.getFloat(idxRate);

			// If not sameCurrency, convert it from rate
			if(! sameCurrency) {
				total += amount/rate;
			}
			else {
				total += amount;
				symbolCurrency = cSumOp.getString(idxCurrencyShortName);
			}

			cSumOp.moveToNext();
		} //for

		// closing useless cursor
		cSumOp.close();

		StringBuilder sbTotal = new StringBuilder(NumberUtil.formatDecimal(total));
		sbTotal.append(" ");
		sbTotal.append(symbolCurrency);


		// Set and refresh view
		if(listView == null)
			listView = (ListView)view.findViewById(R.id.list);

		if(cAllOp != null) {
			// Close previous cursor before new assignation
			try {
				((CheckableCursorAdapter)listView.getAdapter()).getCursor().close();
			} catch(NullPointerException npe) {
				// listview not yet initialized - do nothing
			}

			OperationCheckableCursorAdapter cca = new OperationCheckableCursorAdapter(
					viewPagerOperationActivity, 
					R.layout.operation_item, 
					cAllOp,
					new String[]{"icon", "category", "dateStringHours", "amountString", "convertedAmount"},
					new int[]{R.id.icon, R.id.category, R.id.date, R.id.amount, R.id.convAmount});
			cca.setOnNumberCheckedListener(this);
			listView.setAdapter(cca);

			textViewBalance.setText(sbBalance.toString());
			textViewTotal.setText(sbTotal.toString());
		}

		// refresh header if needed
		refreshHeader();

		// Disable buttons if needed
		initButtons();
	}



	@Override
	public Map<Integer, Object> handleRefreshBackground() {
		return null;
	}

	@Override
	public void handleRefreshEnding(Map<Integer, Object> results) {}
}
