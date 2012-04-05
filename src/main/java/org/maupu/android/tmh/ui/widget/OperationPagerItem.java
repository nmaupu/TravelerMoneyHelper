package org.maupu.android.tmh.ui.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.maupu.android.tmh.AddOrEditActivity;
import org.maupu.android.tmh.AddOrEditOperationActivity;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ViewPagerOperationActivity;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.filter.AFilter;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.AccountBalance;
import org.maupu.android.tmh.ui.ICallback;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.util.NumberUtil;

import android.app.Dialog;
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

public class OperationPagerItem implements OnClickListener, NumberCheckedListener {
	private ViewPagerOperationActivity viewPagerOperationActivity;
	private View view;
	private Date date;
	private Button editButton;
	private Button deleteButton;
	private ListView listView;
	private LayoutInflater inflater;
	private ImageView imageViewIcon;
	private TextView textViewAccountName;
	private TextView textViewTitle;
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
		textViewTitle = (TextView)headerContent.findViewById(R.id.title);
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

	public void refreshDisplay() {
		if(listView == null)
			listView = (ListView)view.findViewById(R.id.list);

		// Getting current account
		Account currentAccount = StaticData.getCurrentAccount();
		if(currentAccount != null && currentAccount.getId() != null) {
			// Process list
			Operation dummy = new Operation();
			dummy.getFilter().addFilter(AFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(currentAccount.getId()));
			Cursor c = dummy.fetchByMonth(date);
			OperationCheckableCursorAdapter cca = new OperationCheckableCursorAdapter(
					viewPagerOperationActivity, 
					R.layout.operation_item, 
					c, 
					new String[]{"icon", "account", "category", "dateString", "amountString", "euroAmount"},
					new int[]{R.id.icon, R.id.account, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
			cca.setOnNumberCheckedListener(this);
			listView.setAdapter(cca);


			c = dummy.sumOperationsByMonth(currentAccount, date, null);
			String symbolCurrency = java.util.Currency.getInstance("EUR").getSymbol();

			// Process balance
			AccountBalance balance = currentAccount.getBalance();
			StringBuilder sBuilder = new StringBuilder();
			if(balance.size() == 1) {
				Set<Integer> s = balance.keySet();
				Iterator<Integer> it = s.iterator();
				Integer curId = it.next();

				Double b = balance.get(curId);

				Currency cur = new Currency();
				Cursor cursor = cur.fetch(curId);
				cur.toDTO(cursor);

				sBuilder.append(NumberUtil.formatDecimalLocale(b))
				.append(" ")
				.append(cur.getShortName())
				.append(" / ");
			}

			sBuilder.append(NumberUtil.formatDecimalLocale(balance.getBalanceRate()))
			.append(" ")
			.append(symbolCurrency);

			textViewBalance.setText(sBuilder.toString());


			Double total = 0d;
			int nbRes = c.getCount();
			boolean sameCurrency = (nbRes == 1);
			for(int i=0; i<nbRes; i++) {
				int idxSum = c.getColumnIndexOrThrow(Operation.KEY_SUM);
				int idxRate = c.getColumnIndexOrThrow(CurrencyData.KEY_TAUX_EURO);
				int idxCurrencyShortName = c.getColumnIndexOrThrow(CurrencyData.KEY_SHORT_NAME);

				float amount = c.getFloat(idxSum);
				float rate = c.getFloat(idxRate);

				// If not sameCurrency, convert to euro
				if(! sameCurrency) {
					total += amount/rate;
				}
				else {
					total += amount;
					symbolCurrency = c.getString(idxCurrencyShortName);
				}

				c.moveToNext();
			} //for

			StringBuffer strTotal = new StringBuffer(NumberUtil.formatDecimalLocale(total));
			strTotal.append(" ");
			strTotal.append(symbolCurrency);

			textViewTotal.setText(strTotal);
		}

		// refresh header if needed
		refreshHeader();

		// Disable buttons if needed
		initButtons();
	}

	public View getView() {
		return view;
	}

	@Override
	public void onClick(View v) {
		final Operation obj = new Operation();
		Intent intent = null;
		final Integer[] posChecked = ((CheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();

		switch(v.getId()) {
		case R.id.account_icon:
			Log.d("OperationPagerItem", "Icon clicked");

			final Dialog dialog = new Dialog(viewPagerOperationActivity);

			dialog.setContentView(R.layout.dialog_choose_account);
			dialog.setTitle(viewPagerOperationActivity.getString(R.string.pick_account));

			ListView listAccount = (ListView)dialog.findViewById(R.id.list);
			Account dummyAccount = new Account();
			final Cursor cursorAllAccounts = dummyAccount.fetchAll();

			final IconCursorAdapter adapter = new IconCursorAdapter(viewPagerOperationActivity, 
					R.layout.icon_name_item_no_checkbox, 
					cursorAllAccounts,
					new String[]{AccountData.KEY_ICON, AccountData.KEY_NAME}, 
					new int[]{R.id.icon, R.id.name}, new ICallback<View>() {
				@Override
				public View callback(Object item) {
					int position = (Integer)((View)item).getTag();
					int oldPosition = cursorAllAccounts.getPosition();
					cursorAllAccounts.moveToPosition(position);

					Account account = new Account();
					account.toDTO(cursorAllAccounts);

					cursorAllAccounts.moveToPosition(oldPosition);


					// Replacing preferences account
					StaticData.setCurrentAccount(account);
					viewPagerOperationActivity.refreshDisplay();
					Log.d("OperationPagerItem", "Callback called");
					dialog.dismiss();

					return (View)item;
				}
			});
			listAccount.setAdapter(adapter);

			dialog.show();
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
		if(this.deleteButton != null)
			this.deleteButton.setEnabled(enabled);
	}

	private void setEnabledEditButton(boolean enabled) {
		if(this.editButton != null)
			this.editButton.setEnabled(enabled);
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

	private void setVisibilityButtonsBar(int anim, boolean visible) {
		View v = (View)view.findViewById(R.id.layout_root_footer);

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

		SimpleDateFormat sdf = new SimpleDateFormat("MMMMM yyyy");
		String dateString = sdf.format(date);
		textViewTitle.setText(dateString);
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
			deleteButton.setEnabled(true);
			editButton.setEnabled(false);
		}
	}
}
