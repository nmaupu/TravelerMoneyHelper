package org.maupu.android.tmh;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;

import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.SoftKeyboardHelper;
import org.maupu.android.tmh.ui.async.IAsyncActivityRefresher;

import java.util.Map;

public abstract class AddOrEditFragment<T extends BaseObject> extends TmhFragment implements IAsyncActivityRefresher {
    public static final String EXTRA_OBJECT_ID = "base_object";
    public static final String EXTRA_APP_INIT = "app_init";
    private T obj;
    private int title;

    private MenuProvider menuProvider;

    public AddOrEditFragment(int title, int contentView, T obj) {
        super(contentView);
        this.title = title;
        this.obj = obj;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(title);

        retrieveItemFromBundle();
        setupMenu(obj != null && obj.getId() != null); // obj != null alone is not sufficient (generic type cannot be tested against null ?)

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onAccountChange() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init all widgets
        View v = initResources(getView());
        if (v != null && !isEditing())
            v.requestFocus();

        // Fill form fields
        baseObjectToFields(obj);

        refreshDisplay();
    }

    @Override
    public void onPause() {
        SoftKeyboardHelper.hide(requireActivity());
        super.onPause();
    }

    public void setupMenu(boolean editMode) {
        if (menuProvider == null) {
            menuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.add_or_edit_menu, menu);
                    menu.findItem(R.id.action_add).setVisible(!editMode);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_save) {
                        saveOrEdit(true);
                    } else if (menuItem.getItemId() == R.id.action_add) {
                        if (saveOrEdit(false)) {
                            Snackbar.make(
                                    requireView(),
                                    getString(R.string.toast_success),
                                    Snackbar.LENGTH_SHORT).show();
                            obj.reset();
                            refreshDisplay();
                        }
                    }
                    return true;
                }
            };
        }
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void retrieveItemFromBundle() {
        try {
            Bundle bundle = getArguments();
            if (bundle != null) {
                T objnew = (T) bundle.getSerializable(AddOrEditFragment.EXTRA_OBJECT_ID);
                if (objnew != null) {
                    obj = objnew;
                }
            }
        } catch (ClassCastException cce) {
            // This exception should not be thrown
            throw cce;
        }
    }

    protected boolean saveOrEdit(boolean returnToPreviousFragment) {
        if (validate()) {
            fieldsToBaseObject(obj);

            if (obj.getId() != null) {
                // Show a confirm dialog when updating
                SimpleDialog.confirmDialog(getContext(), getString(R.string.confirm_modification), (dialog, which) -> {
                    obj.update();
                    onItemEdit();
                    dialog.dismiss();
                    if (returnToPreviousFragment) {
                        requireActivity().onBackPressed();
                    }
                }).show();
            } else {
                boolean ret = obj.insert();
                onItemAdd();
                if (returnToPreviousFragment) {
                    requireActivity().onBackPressed();
                }
                return ret;
            }

            return true;
        } else {
            SimpleDialog.errorDialog(requireActivity(), getString(R.string.error), getString(R.string.error_add_object)).show();
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

    /**
     * Method called when menu item has been processed and persisted.
     * This method provides a way to add more stuff to do if needed
     */
    protected void onItemAdd() {
        ApplicationDrawer.getInstance().updateDrawerBadges();
    }

    /**
     * Method called when menu item has been processed and persisted.
     * This method provides a way to add more stuff to do if needed
     */
    protected void onItemEdit() {
    }

    protected T getObj() {
        return obj;
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        // Restore from beginning
        baseObjectToFields(obj);
        if (!isEditing())
            SoftKeyboardHelper.forceShowUp(requireActivity());
    }
}
