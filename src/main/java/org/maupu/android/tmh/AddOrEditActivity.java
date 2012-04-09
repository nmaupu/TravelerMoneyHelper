package org.maupu.android.tmh;

import greendroid.widget.ActionBarItem;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.CustomActionBarItem;
import org.maupu.android.tmh.ui.CustomActionBarItem.CustomType;
import org.maupu.android.tmh.ui.SimpleDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Class representing an Activity to add or edit objects
 * @author nmaupu
 *
 */
public abstract class AddOrEditActivity<T extends BaseObject> extends TmhActivity {
	public static final String EXTRA_OBJECT_ID = "base_object";
	//private Button buttonContinue;
	//private Button buttonContinueAndAdd;
	//private Button buttonReset;
	//private TextView textInfo;
	private int contentView;
	private T obj;
	private int title;
	private ActionBarItem saveAndAddItem;

	public AddOrEditActivity(int title, int contentView, T obj) {
		this.contentView = contentView;
		this.obj = obj;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(contentView);
		super.setTitle(getString(title));
		
		// actionbar items
		addActionBarItem(CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.Save), TmhApplication.ACTION_BAR_SAVE);
		
		saveAndAddItem = CustomActionBarItem.createActionBarItemFromType(getActionBar(), CustomType.SaveAndAdd);
		addActionBarItem(saveAndAddItem, TmhApplication.ACTION_BAR_SAVE_AND_ADD);
		
		/*
		buttonContinue = (Button)findViewById(R.id.button_continue);
		buttonContinueAndAdd = (Button)findViewById(R.id.button_continue_and_add);
		buttonReset = (Button)findViewById(R.id.button_reset);
		buttonContinue.setOnClickListener(this);
		buttonContinueAndAdd.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
		*/
		/*textInfo = (TextView)findViewById(R.id.text_info);
		if(textInfo != null) {
			textInfo.setText("Please fill this form");
			textInfo.setVisibility(View.VISIBLE);
		}*/

		// Retrieve extra parameter
		retrieveItemFromExtra();
		// Init all widgets
		initResources();
		
		if(isEditing())
			CustomActionBarItem.setEnableItem(false, saveAndAddItem);
		
		// Fill form fields
		baseObjectToFields(obj);
	}

	@SuppressWarnings("unchecked")
	private void retrieveItemFromExtra() {
		try {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			T objnew = (T) bundle.get(AddOrEditActivity.EXTRA_OBJECT_ID);
			if(objnew != null) {
				//buttonContinueAndAdd.setEnabled(false);
				CustomActionBarItem.setEnableItem(false, saveAndAddItem);
				obj = objnew;
			}
		} catch (NullPointerException e) {
			// Here, nothing is allocated, we keep default obj
		} catch (ClassCastException cce) {
			// This exception should not be thrown
			throw cce;
		}
	}
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch(item.getItemId()) {
		case TmhApplication.ACTION_BAR_SAVE:
			onContinue(true);
			break;
		case TmhApplication.ACTION_BAR_SAVE_AND_ADD:
			if(onContinue(false)) {
				Toast.makeText(this, getString(R.string.toast_success), Toast.LENGTH_SHORT).show();
				obj.reset();
				refreshDisplay();
			}
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
		
		return true;
	}

	private boolean onContinue(final boolean disposeActivity) {
		if(validate()) {
			fieldsToBaseObject(obj);
			
			if(obj.getId() != null) {
				// Show a confirm dialog when updating
				final TmhActivity current = this; // WAT the heck I have just done ?
				SimpleDialog.confirmDialog(this, getString(R.string.confirm_modification), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						obj.update();
						
						// Dispose this activity
						if(disposeActivity)
							current.finish();
					}
				}).show();
			} else {
				obj.insert();
				
				if(disposeActivity)
					super.finish();
			}
			
			return true;
		} else {
			SimpleDialog.errorDialog(this, getString(R.string.error), getString(R.string.error_add_object)).show();
			return false;
		}
	}
	
	/**
	 * Method called just after ui creation to finish initialization
	 */
	protected abstract void initResources();
	/**
	 * Method called to validate all field before updating or adding a baseObject
	 * @return
	 */
	protected abstract boolean validate();
	/**
	 * Method to fill all fields from a given object
	 * If parameter is null, all fields must be reset
	 * @param obj
	 */
	protected abstract void baseObjectToFields(T obj);
	/**
	 * Method to fill a baseObject from fields's content
	 * @param obj
	 */
	protected abstract void fieldsToBaseObject(T obj);

	public void refreshDisplay() {
		// Restore from begining
		baseObjectToFields(obj);
	}

	// Menu item is disabled, so do nothing here
	protected Intent onAddClicked() {
		return null;
	}
	
	protected T getObj() {
		return obj;
	}
	
	/**
	 * Method to know edition mode
	 * @return true if we are editing an obj or false otherwise (i.e. adding an obj)
	 */
	public boolean isEditing() {
		return obj != null && obj.getId() != null;
	}
}
