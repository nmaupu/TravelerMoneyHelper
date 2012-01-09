package org.maupu.android.tmh;

import org.maupu.android.tmh.database.DatabaseHelper;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.CustomTitleBar;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.NumberCheckedListener;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ManageableObjectActivity<T extends BaseObject> extends Activity implements NumberCheckedListener, OnClickListener {
	protected DatabaseHelper dbHelper = new DatabaseHelper(this);
	private static final int ACTIVITY_ADD = 0;
	private static final int ACTIVITY_EDIT = 1;
	private ListView listView;
	private TextView tvEmpty;
	private Button addButton;
	private Button editButton;
	private Button deleteButton;
	private String title;
	private Integer drawableIcon;
	private Class<?> addOrEditActivity;
	private Integer layoutList;
	private T obj;

	public ManageableObjectActivity(String title, Integer drawableIcon, Class<?> addOrEditActivity, T obj) {
		this(title, drawableIcon, addOrEditActivity, obj, R.layout.manageable_object);
	}
	
	public ManageableObjectActivity(String title, Integer drawableIcon, Class<?> addOrEditActivity, T obj, Integer layoutList) {
		this.title = title;
		this.drawableIcon = drawableIcon;
		this.addOrEditActivity = addOrEditActivity;
		this.layoutList = layoutList;
		this.obj = obj;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CustomTitleBar customTB = new CustomTitleBar(this);
		setContentView(layoutList);
		super.onCreate(savedInstanceState);
		customTB.setName(title);
		customTB.setIcon(drawableIcon);
		
		dbHelper.openWritable();

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
		refreshListView(dbHelper);
	}

	public void setAdapter(int layout, Cursor data, String[] from, int[] to) {
		setAdapter(new CheckableCursorAdapter(this, layout, data, from, to));
	}
	
	public void setAdapter(CheckableCursorAdapter adapter) {
		if(adapter == null)
			return;
		
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
		final Context finalContext = this;
		final Integer[] posChecked = ((CheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();
		
		switch(v.getId()) {
		case R.id.button_edit:
			if(posChecked.length == 1) {
				int p = posChecked[0];
				Cursor cursor = (Cursor)listView.getItemAtPosition(p);
				obj.toDTO(dbHelper, cursor);
				
				intent = new Intent(this, addOrEditActivity);
				intent.putExtra(AddOrEditActivity.EXTRA_OBJECT_ID, obj);
				startActivityForResult(intent, ACTIVITY_EDIT);
			}
			
			break;
		case R.id.button_delete:
			SimpleDialog.confirmDialog(this, "Are you sure you want to delete these objects ?", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String errMessage = "One or more objects could not be deleted";
					boolean err = false;

					for(int i=0; i<posChecked.length; i++) {
						Integer pos = posChecked[i];
						//Request deletion
						Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
						obj.toDTO(dbHelper, cursor);
						if(validateConstraintsForDeletion(dbHelper, obj))
							obj.delete(dbHelper);
						else
							err = true;
					}

					if(err)
						SimpleDialog.errorDialog(finalContext, "Error", errMessage).show();

					dialog.dismiss();
					refreshListView(dbHelper);
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
		refreshListView(dbHelper);
	}

	/**
	 * Validate an object before deletion
	 * @param obj
	 * @return true if object can be deleted, false otherwise
	 */
	protected abstract boolean validateConstraintsForDeletion(final DatabaseHelper dbHelper, final T obj);
	/**
	 * Method refreshing list view from adapter
	 */
	protected abstract void refreshListView(final DatabaseHelper dbHelper);
}
