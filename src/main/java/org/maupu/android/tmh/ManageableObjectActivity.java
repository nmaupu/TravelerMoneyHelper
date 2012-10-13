package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionGrid;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.NumberCheckedListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ManageableObjectActivity<T extends BaseObject> extends TmhActivity implements NumberCheckedListener, OnClickListener {
	private static final int ACTIVITY_ADD = 0;
	private static final int ACTIVITY_EDIT = 1;
	private ListView listView;
	private TextView tvEmpty;
	private Button editButton;
	private Button deleteButton;
	private Button updateButton;
	private int title;
	private Class<?> addOrEditActivity;
	private Integer layoutList;
	private T obj;
	private QuickActionGrid quickActionGrid;
	private boolean animList = false;

	public ManageableObjectActivity(int title, Class<?> addOrEditActivity, T obj, boolean animList) {
		this(title, addOrEditActivity, obj, R.layout.manageable_object, animList);
	}

	public ManageableObjectActivity(int title, Class<?> addOrEditActivity, T obj, Integer layoutList, boolean animList) {
		this.title = title;
		this.addOrEditActivity = addOrEditActivity;
		this.layoutList = layoutList;
		this.obj = obj;
		this.animList = animList;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(layoutList);
		super.setTitle(title);


		// actionbar items
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Edit), TmhApplication.ACTION_BAR_EDIT);
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Add), TmhApplication.ACTION_BAR_ADD);

		quickActionGrid = createQuickActionGridEdition();


		this.tvEmpty = (TextView) findViewById(R.id.empty);

		this.listView = (ListView) findViewById(R.id.list);
		//this.listView.setItemsCanFocus(false);
		//this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		this.listView.setEmptyView(tvEmpty);

		this.editButton = (Button)findViewById(R.id.button_edit);
		this.deleteButton = (Button)findViewById(R.id.button_delete);
		this.updateButton = (Button)findViewById(R.id.button_update);

		if(this.editButton != null)
			this.editButton.setOnClickListener(this);

		if(this.deleteButton != null)
			this.deleteButton.setOnClickListener(this);
		
		if(this.updateButton != null)
			this.updateButton.setOnClickListener(this);

		if(animList)
			setListViewAnimation(listView);
		
        //
		initButtons();
		refreshDisplay();
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_ADD:
			onAddClicked();
			break;
		case TmhApplication.ACTION_BAR_EDIT:
			quickActionGrid.show(item.getItemView());
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}

		return true;
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
			buttonsBarGone();
			break;
		case 1:
			setEnabledDeleteButton(true);
			setEnabledEditButton(true);
			setEnabledUpdateButton(true);
			buttonsBarVisible();
			break;
		default:
			deleteButton.setEnabled(true);
			editButton.setEnabled(false);
			updateButton.setEnabled(true);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		final Context finalContext = this;
		final Integer[] posChecked = ((CheckableCursorAdapter)listView.getAdapter()).getCheckedPositions();

		switch(v.getId()) {
		case R.id.button_update:
			Integer[] objsIds = new Integer[posChecked.length];
			for(int i=0; i<posChecked.length; i++) {
				Integer pos = posChecked[i];
				Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
				obj.toDTO(cursor);
				objsIds[i] = obj.getId();
			}
			onClickUpdate(objsIds);
			break;
		case R.id.button_edit:
			if(posChecked.length == 1) {
				int p = posChecked[0];
				Cursor cursor = (Cursor)listView.getItemAtPosition(p);
				obj.toDTO(cursor);

				intent = new Intent(this, addOrEditActivity);
				intent.putExtra(AddOrEditActivity.EXTRA_OBJECT_ID, obj);
				startActivityForResult(intent, ACTIVITY_EDIT);
			}

			break;
		case R.id.button_delete:
			SimpleDialog.confirmDialog(this, 
					getString(R.string.manageable_obj_del_confirm_question), 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String errMessage = getString(R.string.manageable_obj_del_error);
					boolean err = false;

					for(int i=0; i<posChecked.length; i++) {
						Integer pos = posChecked[i];
						//Request deletion
						Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
						obj.toDTO(cursor);
						if(validateConstraintsForDeletion(obj))
							obj.delete();
						else
							err = true;
					}

					if(err)
						SimpleDialog.errorDialog(finalContext, getString(R.string.error), errMessage).show();

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
	
	private void setEnabledUpdateButton(boolean enabled) {
		if(this.updateButton != null)
			this.updateButton.setEnabled(enabled);
	}
	
	public void activateUpdateButton() {
		if(this.updateButton != null)
			this.updateButton.setVisibility(View.VISIBLE);
	}

	private void initButtons() {
		setEnabledDeleteButton(false);
		setEnabledEditButton(false);
		setEnabledUpdateButton(false);
		buttonsBarGone();
	}

	private void buttonsBarVisible() {
		setVisibilityButtonsBar(R.anim.pushup, true);
	}

	private void buttonsBarGone() {
		setVisibilityButtonsBar(R.anim.pushdown, false);
	}

	private void setVisibilityButtonsBar(int anim, boolean visible) {
		View v = (View)findViewById(R.id.layout_root_footer);

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

	public ListView getListView() {
		return this.listView;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshDisplay();
	}

	/**
	 * Validate an object before deletion
	 * @param obj
	 * @return true if object can be deleted, false otherwise
	 */
	protected abstract boolean validateConstraintsForDeletion(final T obj);
	
	/**
	 * Callback when clicking a button
	 * @param the button clicked
	 */
	protected abstract void onClickUpdate(Integer[] objs);

	protected Intent onAddClicked() {
		Intent intent = new Intent(this, addOrEditActivity);
		startActivityForResult(intent, ACTIVITY_ADD);

		return intent;
	}
}
