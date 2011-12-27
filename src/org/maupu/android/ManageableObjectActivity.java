package org.maupu.android;

import org.maupu.android.database.APersistedData;
import org.maupu.android.ui.CustomTitleBar;
import org.maupu.android.ui.SimpleDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ManageableObjectActivity extends Activity implements NumberCheckedListener, OnClickListener {
	private static final int ACTIVITY_ADD = 0;
	private static final int ACTIVITY_EDIT = 1;
	private ListView listView;
	private TextView tvEmpty;
	private Button addButton;
	private Button editButton;
	private Button deleteButton;
	private String title;
	private Integer drawableIcon;
	private CustomCheckableCursorAdapter adapter;
	private Class<?> addOrEditActivity;
	private Integer layoutList;

	public ManageableObjectActivity(String title, Integer drawableIcon, Class<?> addOrEditActivity) {
		this(title, drawableIcon, addOrEditActivity, R.layout.manageable_object);
	}
	
	public ManageableObjectActivity(String title, Integer drawableIcon, Class<?> addOrEditActivity, Integer layoutList) {
		this.title = title;
		this.drawableIcon = drawableIcon;
		this.addOrEditActivity = addOrEditActivity;
		this.layoutList = layoutList;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(layoutList);
		super.onCreate(savedInstanceState);
		customTB.setName(title);
		customTB.setIcon(drawableIcon);

		this.tvEmpty = (TextView) findViewById(R.id.empty);
		
		this.addButton = (Button) findViewById(R.id.button_add);
		this.addButton.setOnClickListener(this);
		
		this.listView = (ListView) findViewById(R.id.list);
		this.listView.setItemsCanFocus(false);
		//this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		this.listView.setEmptyView(tvEmpty);
		
		this.editButton = (Button)findViewById(R.id.button_edit);
		this.deleteButton = (Button)findViewById(R.id.button_delete);

		if(this.editButton != null)
			this.editButton.setOnClickListener(this);

		if(this.deleteButton != null)
			this.deleteButton.setOnClickListener(this);

		initButtons();
	}

	public void setAdapter(int layout, Cursor data, String[] from, int[] to) {
		adapter = new CustomCheckableCursorAdapter(this, layout, data, from, to);
		adapter.setOnNumberCheckedListener(this);
		listView.setAdapter(adapter);
		
		initButtons();
	}

	@Override
	public void onCheckedItem(int numberItemsChecked) {
		// Change state of buttonEdit and buttonDelete
		switch(numberItemsChecked) {
		case 0:
			initButtons();
			break;
		case 1:
			setEnabledDeleteButton(true);
			setEnabledEditButton(true);
			break;
		default:
			deleteButton.setEnabled(true);
			editButton.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch(v.getId()) {
		case R.id.button_edit:
			Integer itemId = null;

			for(int i=0; i<listView.getCount(); i++) {
				CheckBox cb = (CheckBox)((View)listView.getChildAt(i)).findViewById(R.id.checkbox);
				if(cb.isChecked()) {
					Cursor cursor = (Cursor)listView.getItemAtPosition(i);
					itemId = cursor.getInt(cursor.getColumnIndex(APersistedData.KEY_ID));
				}
			}

			intent = new Intent(this, addOrEditActivity);
			intent.putExtra(APersistedData.KEY_ID, itemId);
			startActivityForResult(intent, ACTIVITY_EDIT);
			break;
		case R.id.button_delete:
			final Context finalContext = this;

			SimpleDialog.confirmDialog(this, "Are you sure you want to delete these objects ?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String errMessage = "One or more objects could not be deleted";
					boolean err = false;

					//List<Integer> l = ((CustomCheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();
					Integer[] positions = ((CustomCheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();
					for(int i=0; i<positions.length; i++) {
						Integer pos = positions[i];
						//Request deletion
						Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
						int itemId = cursor.getInt(cursor.getColumnIndex(APersistedData.KEY_ID));
						if(! delete(itemId)) {
							err = true;
						}
					}

					if(err)
						SimpleDialog.errorDialog(finalContext, "Error", errMessage).show();

					dialog.dismiss();
					refreshListView();
				}
			}).show();
			break;
		case R.id.button_add:
			intent = new Intent(this, addOrEditActivity);
			startActivityForResult(intent, ACTIVITY_ADD);
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
	}

	public ListView getListView() {
		return this.listView;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshListView();
	}

	protected abstract boolean delete(int itemId);
	protected abstract void refreshListView();
}
