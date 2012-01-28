package org.maupu.android.tmh.ui.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.maupu.android.tmh.AddOrEditActivity;
import org.maupu.android.tmh.AddOrEditOperationActivity;
import org.maupu.android.tmh.R;
import org.maupu.android.tmh.ViewPagerTestActivity;
import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.Operation;
import org.maupu.android.tmh.ui.SimpleDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class OperationPagerItem implements OnClickListener, NumberCheckedListener {
	private ViewPagerTestActivity ctx;
	private View view;
	private Date date;
	private DatabaseHelper dbHelper;
	private Button editButton;
	private Button deleteButton;
	private ListView listView;
	private Operation obj = new Operation();

	public OperationPagerItem(ViewPagerTestActivity ctx, LayoutInflater inflater, DatabaseHelper dbHelper, Date date) {
		this.ctx = ctx;
		this.date = date;
		this.dbHelper = dbHelper;

		view = inflater.inflate(R.layout.manageable_object, null);

		init();
		initButtons();
		refreshDisplay();
	}

	private void init() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMMM yyyy");
		String dateString = sdf.format(date);

		LinearLayout header = (LinearLayout)view.findViewById(R.id.header);
		TextView text = new TextView(ctx);
		text.setText(dateString);
		text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		header.setVisibility(View.VISIBLE);
		header.addView(text);

		this.editButton = (Button)view.findViewById(R.id.button_edit);
		this.deleteButton = (Button)view.findViewById(R.id.button_delete);

		if(this.editButton != null)
			this.editButton.setOnClickListener(this);

		if(this.deleteButton != null)
			this.deleteButton.setOnClickListener(this);
	}

	public void refreshDisplay() {
		if(listView == null)
			listView = (ListView)view.findViewById(R.id.list);
		Operation dummy = new Operation();
		Cursor c = dummy.fetchByMonth(dbHelper, date, 1);
		CheckableCursorAdapter cca = new CheckableCursorAdapter(ctx, R.layout.operation_item, 
				c, 
				new String[]{"icon", "account", "category", "dateString", "amountString", "euroAmount"},
				new int[]{R.id.icon, R.id.account, R.id.category, R.id.date, R.id.amount, R.id.euroAmount});
		cca.setOnNumberCheckedListener(this);
		listView.setAdapter(cca);
		// Disable buttons if needed
		initButtons();
	}

	public View getView() {
		return view;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		final Integer[] posChecked = ((CheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();

		switch(v.getId()) {
		case R.id.button_edit:
			if(posChecked.length == 1) {
				int p = posChecked[0];
				Cursor cursor = (Cursor)listView.getItemAtPosition(p);
				obj.toDTO(dbHelper, cursor);

				intent = new Intent(ctx, AddOrEditOperationActivity.class);
				intent.putExtra(AddOrEditActivity.EXTRA_OBJECT_ID, obj);
				ctx.startActivityForResult(intent, 0);
			}

			break;
		case R.id.button_delete:
			SimpleDialog.confirmDialog(ctx, 
					ctx.getString(R.string.manageable_obj_del_confirm_question), 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String errMessage = ctx.getString(R.string.manageable_obj_del_error);
					boolean err = false;

					for(int i=0; i<posChecked.length; i++) {
						Integer pos = posChecked[i];
						//Request deletion
						Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
						obj.toDTO(dbHelper, cursor);
						obj.delete(dbHelper);
					}

					if(err)
						SimpleDialog.errorDialog(ctx, ctx.getString(R.string.error), errMessage).show();

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
		View v = (View)view.findViewById(R.id.layout_root);

		// Already ok
		if((!visible && v.getVisibility() == View.GONE) || (visible && v.getVisibility() == View.VISIBLE))
			return;

		Animation animation  = AnimationUtils.loadAnimation(v.getContext(), anim);
		v.startAnimation(animation);

		if(visible) {
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
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
			deleteButton.setEnabled(true);
			editButton.setEnabled(false);
		}
	}
}
