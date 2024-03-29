package org.maupu.android.tmh;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import org.maupu.android.tmh.database.object.BaseObject;
import org.maupu.android.tmh.ui.AnimHelper;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.widget.CheckableCursorAdapter;
import org.maupu.android.tmh.ui.widget.NumberCheckedListener;
import org.maupu.android.tmh.util.TmhLogger;

public abstract class ManageableObjectFragment<T extends BaseObject> extends TmhFragment implements View.OnClickListener, NumberCheckedListener {
    private static final Class<ManageableObjectFragment> TAG = ManageableObjectFragment.class;
    private ListView listView;
    private TextView tvEmpty;
    private Button editButton;
    private Button deleteButton;
    private Button updateButton;
    private LinearLayout layoutRootFooter;
    private Class<?> addOrEditFragment;
    private T obj;
    private boolean animList = false;
    private CheckableCursorAdapter checkableCursorAdapter = null;
    private int title;
    private MenuProvider menuProvider;

    public ManageableObjectFragment(int title, Class<?> addOrEditFragment, T obj, boolean animList) {
        this(title, addOrEditFragment, obj, R.layout.manageable_object, animList);
    }

    public ManageableObjectFragment(int title, Class<?> addOrEditFragment, T obj, Integer layoutList, boolean animList) {
        super(layoutList);
        this.addOrEditFragment = addOrEditFragment;
        this.obj = obj;
        this.animList = animList;
        this.title = title;
    }
    
    @Override
    public void onAccountChange() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(this.title);
        setupMenu();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TmhLogger.d(TAG, "Calling onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        this.layoutRootFooter = view.findViewById(R.id.layout_root_footer);

        this.tvEmpty = view.findViewById(R.id.empty);

        this.listView = view.findViewById(R.id.list);
        this.listView.setEmptyView(tvEmpty);

        this.editButton = view.findViewById(R.id.button_edit);
        this.deleteButton = view.findViewById(R.id.button_delete);
        this.updateButton = view.findViewById(R.id.button_update);

        if (this.editButton != null)
            this.editButton.setOnClickListener(this);

        if (this.deleteButton != null)
            this.deleteButton.setOnClickListener(this);

        if (this.updateButton != null)
            this.updateButton.setOnClickListener(this);

        if (!this.updateButtonVisible()) {
            this.updateButton.setVisibility(View.GONE);
        }

        if (animList)
            AnimHelper.setListViewAnimation(listView);

        //
        initButtons();
        refreshDisplay();
    }

    public void setupMenu() {
        if (menuProvider == null) {
            menuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.manage_menu, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_add:
                            onAddClicked();
                            break;
                    }
                    return true;
                }
            };
        }
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void setAdapter(int layout, Cursor data, String[] from, int[] to) {
        checkableCursorAdapter = new CheckableCursorAdapter(getActivity(), layout, data, from, to);
        setAdapter(checkableCursorAdapter);
    }

    public CheckableCursorAdapter getAdapter() {
        return checkableCursorAdapter;
    }

    public void setAdapter(CheckableCursorAdapter adapter) {
        if (adapter == null)
            return;

        adapter.setOnNumberCheckedListener(this);
        listView.setAdapter(adapter);

        initButtons();
    }

    @Override
    public void onCheckedItem(int numberItemsChecked) {
        // Change state of buttonEdit and buttonDelete
        switch (numberItemsChecked) {
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
                setEnabledDeleteButton(true);
                setEnabledEditButton(false);
                setEnabledUpdateButton(true);
        }
    }

    @Override
    public void onClick(View v) {
        final Integer[] posChecked = ((CheckableCursorAdapter) listView.getAdapter()).getCheckedPositions();

        switch (v.getId()) {
            case R.id.button_update:
                Integer[] objsIds = new Integer[posChecked.length];
                for (int i = 0; i < posChecked.length; i++) {
                    Integer pos = posChecked[i];
                    Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                    obj.toDTO(cursor);
                    objsIds[i] = obj.getId();
                }
                onClickUpdate(objsIds);

                ((CheckableCursorAdapter) listView.getAdapter()).clearCheckedItems();
                buttonsBarGone();

                break;
            case R.id.button_edit:
                if (posChecked.length == 1) {
                    int p = posChecked[0];
                    Cursor cursor = (Cursor) listView.getItemAtPosition(p);
                    obj.toDTO(cursor);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(AddOrEditFragment.EXTRA_OBJECT_ID, obj);
                    ((MainActivity) requireActivity()).changeFragment(this.addOrEditFragment, true, bundle);
                }

                break;
            case R.id.button_delete:
                SimpleDialog.confirmDialog(getActivity(),
                        getString(R.string.manageable_obj_del_confirm_question),
                        (dialog, which) -> {
                            String errMessage = getString(R.string.manageable_obj_del_error);
                            boolean err = false;

                            for (Integer pos : posChecked) {
                                //Request deletion
                                Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                                obj.toDTO(cursor);
                                if (validateConstraintsForDeletion(obj))
                                    obj.delete();
                                else
                                    err = true;
                            }

                            if (err)
                                SimpleDialog.errorDialog(getActivity(), getString(R.string.error), errMessage).show();

                            onItemDelete();

                            dialog.dismiss();

                            refreshDisplay();
                        }).show();
                break;
        }
    }

    private void setEnabledButton(Button btn, boolean enabled) {
        if (btn != null) {
            btn.setEnabled(enabled);
            btn.setAlpha(enabled ? 1f : .4f);
        }
    }

    private void setEnabledDeleteButton(boolean enabled) {
        setEnabledButton(this.deleteButton, enabled);
    }

    private void setEnabledEditButton(boolean enabled) {
        setEnabledButton(this.editButton, enabled);
    }

    private void setEnabledUpdateButton(boolean enabled) {
        setEnabledButton(this.updateButton, enabled);
    }

    public void activateUpdateButton() {
        if (this.updateButton != null) {
            this.updateButton.setVisibility(View.VISIBLE);
            setEnabledUpdateButton(true);
        }
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

        // Already ok
        if ((!visible && layoutRootFooter.getVisibility() == View.GONE) || (visible && layoutRootFooter.getVisibility() == View.VISIBLE))
            return;

        if (visible) {
            layoutRootFooter.setVisibility(View.VISIBLE);
        } else {
            layoutRootFooter.setVisibility(View.GONE);
        }

        Animation animation = AnimationUtils.loadAnimation(layoutRootFooter.getContext(), anim);
        if (anim == R.anim.pushup)
            animation.setInterpolator(new DecelerateInterpolator());
        else
            animation.setInterpolator(new AccelerateInterpolator());

        layoutRootFooter.startAnimation(animation);
    }

    public ListView getListView() {
        return this.listView;
    }

    protected void onItemDelete() {
        ApplicationDrawer.getInstance().updateDrawerBadges();
    }

    /**
     * Validate an object before deletion
     *
     * @param obj
     * @return true if object can be deleted, false otherwise
     */
    protected abstract boolean validateConstraintsForDeletion(final T obj);

    /**
     * Callback when clicking a button
     *
     * @param objs button clicked
     */
    protected abstract void onClickUpdate(Integer[] objs);

    /**
     * Set the status of the update button
     */
    protected boolean updateButtonVisible() {
        return false;
    }

    protected void onAddClicked() {
        ((MainActivity) requireActivity()).changeFragment(this.addOrEditFragment, true, null);
    }
}
