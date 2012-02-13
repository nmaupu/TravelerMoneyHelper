package org.maupu.android.tmh.ui.widget;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import org.maupu.android.tmh.AddOrEditActivity;
import org.maupu.android.tmh.AddOrEditOperationActivity;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ViewPagerOperationActivity;
import org.maupu.android.tmh.database.AccountData;
import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.OperationData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.database.util.filter.AFilter;
import org.maupu.android.tmh.ui.ICallback;
import org.maupu.android.tmh.ui.ImageViewHelper;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;

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
	private DatabaseHelper dbHelper;
	private Button editButton;
	private Button deleteButton;
	private ListView listView;
	private LayoutInflater inflater;
	private ImageView imageViewIcon;
	private TextView textViewAccountName;
	private TextView textViewTitle;
	private View footerOperationTotal;
	private TextView textViewTotal;

	public OperationPagerItem(ViewPagerOperationActivity ctx, LayoutInflater inflater, DatabaseHelper dbHelper, Date date) {
		this.viewPagerOperationActivity = ctx;
		this.date = date;
		this.dbHelper = dbHelper;
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
		footerOperationTotal = inflater.inflate(R.layout.manageable_operation_total, null);
		LinearLayout content = (LinearLayout)view.findViewById(R.id.layout_root);
		
		textViewTotal = (TextView)footerOperationTotal.findViewById(R.id.total);
		content.addView(footerOperationTotal, content.getChildCount()-2);
	}

	public void refreshDisplay() {
		if(listView == null)
			listView = (ListView)view.findViewById(R.id.list);
		
		// Getting current account
		Account currentAccount = StaticData.getCurrentAccount(viewPagerOperationActivity, dbHelper);
		if(currentAccount != null && currentAccount.getId() != null) {
			// Process list
			Operation dummy = new Operation();
			dummy.getFilter().addFilter(AFilter.FUNCTION_EQUAL, OperationData.KEY_ID_ACCOUNT, String.valueOf(currentAccount.getId()));
			Cursor c = dummy.fetchByMonth(dbHelper, date);
			OperationCheckableCursorAdapter cca = new OperationCheckableCursorAdapter(
					viewPagerOperationActivity, 
					R.layout.operation_item, 
					c, 
					new String[]{"icon", "account", "category", "dateString", "amountString", "euroAmount"},
					new int[]{R.id.icon, R.id.account, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
			cca.setOnNumberCheckedListener(this);
			listView.setAdapter(cca);
			
			// Process total
			c = dummy.sumOperationsByMonth(dbHelper, currentAccount, date);
			float total = 0f;
			int nbRes = c.getCount();
			boolean sameCurrency = (nbRes == 1);
			String symbolCurrency = Currency.getInstance("EUR").getSymbol();
			
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
			
			StringBuffer strTotal = new StringBuffer(String.valueOf(total));
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
			final Cursor cursorAllAccounts = dummyAccount.fetchAll(dbHelper);
			
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
							account.toDTO(dbHelper, cursorAllAccounts);
							
							cursorAllAccounts.moveToPosition(oldPosition);
							
							
							// Replacing preferences account
							StaticData.setCurrentAccount(viewPagerOperationActivity, dbHelper, account);
							viewPagerOperationActivity.refreshDisplay(dbHelper);
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
				obj.toDTO(dbHelper, cursor);

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
						obj.toDTO(dbHelper, cursor);
						obj.delete(dbHelper);
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
		Account account = StaticData.getCurrentAccount(viewPagerOperationActivity, dbHelper);

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
