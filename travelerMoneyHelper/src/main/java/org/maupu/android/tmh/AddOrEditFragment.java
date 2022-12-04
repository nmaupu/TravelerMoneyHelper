package org.maupu.android.tmh;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.async.AsyncActivityRefresher;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;

import java.util.Map;

public abstract class AddOrEditFragment<T extends BaseObject> extends Fragment implements IAsyncActivityRefresher {
    public static final String EXTRA_OBJECT_ID = "base_object";
    public static final String EXTRA_APP_INIT = "app_init";
    private T obj;
    private boolean appInit = false;
    private int title;

    public AddOrEditFragment(int title, int contentView, T obj) {
        super(contentView);
        this.title = title;
        this.obj = obj;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(title);

        setHasOptionsMenu(true);

        // Retrieve extra parameter
        retrieveItemFromExtra();

        // Init all widgets
        View v = initResources(getView());
        if (v != null && !isEditing()) {
            v.requestFocus();
            SoftKeyboardHelper.forceShowUp(this);
        }

        // Fill form fields
        baseObjectToFields(obj);

        refreshDisplay();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_or_edit_menu, menu);
        if (isEditing() || appInit)
            menu.findItem(R.id.action_add).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onContinue(true);
                break;
            case R.id.action_add:
                if (onContinue(false)) {
                    Toast.makeText(getContext(), getString(R.string.toast_success), Toast.LENGTH_SHORT).show();
                    obj.reset();
                    refreshDisplay();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void retrieveItemFromExtra() {
        try {
            Bundle bundle = getArguments();
            T objnew = (T) bundle.getSerializable(AddOrEditFragment.EXTRA_OBJECT_ID);
            if (objnew != null) {
                //buttonContinueAndAdd.setEnabled(false);
                //CustomActionBarItem.setEnableItem(false, saveAndAddItem);
                obj = objnew;
            }

            // Set this when initializing app from WelcomeActivity (we deactivate 'save and add' button)
            appInit = (Boolean) bundle.get(AddOrEditFragment.EXTRA_APP_INIT);

        } catch (NullPointerException e) {
            // Here, nothing is allocated, we keep default obj
        } catch (ClassCastException cce) {
            // This exception should not be thrown
            throw cce;
        }
    }

    protected boolean onContinue(final boolean disposeActivity) {
        if (validate()) {
            fieldsToBaseObject(obj);

            if (obj.getId() != null) {
                // Show a confirm dialog when updating
                SimpleDialog.confirmDialog(getContext(), getString(R.string.confirm_modification), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        obj.update();

                        dialog.dismiss();

                        // Dispose this activity
                        if (disposeActivity) {
                            SoftKeyboardHelper.hide(getActivity());
                            getActivity().finish();
                        }
                    }
                }).show();
            } else {
                obj.insert();

                if (disposeActivity) {
                    SoftKeyboardHelper.hide(getActivity());
                    //getActivity().finish();
                }
            }

            return true;
        } else {
            SimpleDialog.errorDialog(getActivity(), getString(R.string.error), getString(R.string.error_add_object)).show();
            return false;
        }
    }

    /**
     * Method called to validate all field before updating or adding a baseObject
     *
     * @return
     */
    protected abstract boolean validate();

    /**
     * Method called just after ui creation to finish initialization.
     *
     * @return A View which will receive the focus (soft keyboard will also pop). Return Null for no focus.
     */
    protected abstract View initResources(View view);

    /**
     * Method to know edition mode
     *
     * @return true if we are editing an obj or false otherwise (i.e. adding an obj)
     */
    public boolean isEditing() {
        return obj != null && obj.getId() != null;
    }

    /**
     * Method to fill all fields from a given object
     * If parameter is null, all fields must be reset
     *
     * @param obj
     */
    protected abstract void baseObjectToFields(T obj);

    /**
     * Method to fill a baseObject from fields's content
     *
     * @param obj
     */
    protected abstract void fieldsToBaseObject(T obj);

    protected T getObj() {
        return obj;
    }

    public void refreshDisplay() {
        AsyncActivityRefresher refresher = new AsyncActivityRefresher(getActivity(), this, false);
        try {
            // Execute background task implemented by client class
            refresher.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground() {
        return null;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        // Restore from beginning
        baseObjectToFields(obj);
    }
}
