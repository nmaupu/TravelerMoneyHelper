package org.maupu.android.tmh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import org.maupu.android.tmh.database.CurrencyData;
import org.maupu.android.tmh.database.object.Account;
import org.maupu.android.tmh.database.object.Currency;
import org.maupu.android.tmh.dialog.CurrencyChooserBottomSheetDialog;
import org.maupu.android.tmh.dialog.FlagChooserBottomSheetDialog;
import org.maupu.android.tmh.ui.ApplicationDrawer;
import org.maupu.android.tmh.ui.CurrencyISO4217;
import org.maupu.android.tmh.ui.Flag;
import org.maupu.android.tmh.ui.SimpleDialog;
import org.maupu.android.tmh.ui.StaticData;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncFetcher;
import org.maupu.android.tmh.ui.async.OpenExchangeRatesAsyncUpdater;
import org.maupu.android.tmh.ui.widget.SimpleIconAdapter;
import org.maupu.android.tmh.ui.widget.SpinnerManager;
import org.maupu.android.tmh.util.ImageUtil;

import java.util.List;
import java.util.Map;

public class AddOrEditAccountFragment extends AddOrEditFragment<Account> {
    private ImageView imageViewIcon;
    private TextView textViewName;
    private SpinnerManager spinnerCurrencyManager;
    private Button buttonChooseCurrency;

    private OpenExchangeRatesAsyncFetcher oerFetcher;
    List<CurrencyISO4217> currenciesList;
    ArrayAdapter<CurrencyISO4217> currencyAdapter;

    private List<ResolveInfo> mApps;
    private String[] popupMenuIconNames;

    private static final int MENU_ITEM_APPS = 0;
    private static final int MENU_ITEM_FLAGS = 1;
    private static final int MENU_ITEM_DEFAULT = 2;
    private static final int MENU_ITEM_URL = 3;
    private static final int MENU_ITEM_CAMERA = 4;

    public AddOrEditAccountFragment() {
        super(R.string.fragment_title_edition_account, R.layout.add_or_edit_account, new Account());
    }

    @Override
    protected View initResources(View view) {
        imageViewIcon = view.findViewById(R.id.icon);
        textViewName = view.findViewById(R.id.name);
        buttonChooseCurrency = view.findViewById(R.id.button_choose_currency);

        Spinner spinnerCurrency = view.findViewById(R.id.currency);
        spinnerCurrencyManager = new SpinnerManager(requireContext(), spinnerCurrency);

        imageViewIcon.setOnClickListener(v -> createDialogMenu());

        buttonChooseCurrency.setOnClickListener(v -> {
            showBottomSheetCurrencyDialog();
        });

        // Setting spinner currency values
        Currency dummyCurrency = new Currency();
        Cursor c = dummyCurrency.fetchAll();
        //spinnerCurrency.setAdapter(SpinnerManager.createSpinnerCursorAdapter(this, c, CurrencyData.KEY_LONG_NAME));
        spinnerCurrencyManager.setAdapter(c, CurrencyData.KEY_LONG_NAME);

        popupMenuIconNames = new String[]{
                getString(R.string.popup_app_icon),
                getString(R.string.popup_flag_icon),
                getString(R.string.popup_default_icon)};

        loadApps();

        initOerFetcher();

        return textViewName;
    }

    @Override
    public void onDestroy() {
        spinnerCurrencyManager.closeAdapterCursor();
        super.onDestroy();
    }

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mApps = requireActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    private void createDialogMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle(R.string.account_icon_edit_dialog_title);
        builder.setItems(popupMenuIconNames, (dialog, which) -> {
            switch (which) {
                case MENU_ITEM_APPS:
                    createDialogFromApps().show();
                    break;
                case MENU_ITEM_URL:
                case MENU_ITEM_CAMERA:
                    Snackbar.make(
                            getView(),
                            getString(R.string.not_implemented),
                            Snackbar.LENGTH_SHORT).show();
                    break;
                case MENU_ITEM_FLAGS:
                    showBottomSheetFlagChooserDialog();
                    break;
                case MENU_ITEM_DEFAULT:
                    imageViewIcon.setImageResource(R.drawable.tmh_icon_48);
                    imageViewIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
            }
        });
        builder.create().show();
    }

    private AlertDialog createDialogFromApps() {
        AlertDialog.Builder builder;
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_account_app_icon, (ViewGroup) requireActivity().findViewById(R.id.root_layout), false);

        builder = new AlertDialog.Builder(requireContext());
        builder.setView(layout);

        final AlertDialog dialog = builder.create();

        GridView gridview = layout.findViewById(R.id.gridview);
        gridview.setAdapter(new AppsAdapter());
        gridview.setOnItemClickListener((parent, v, position, id) -> {
            ResolveInfo info = mApps.get(position % mApps.size());
            imageViewIcon.setImageDrawable(info.activityInfo.loadIcon(requireContext().getPackageManager()));
            dialog.dismiss();
        });

        Button close = (Button) layout.findViewById(R.id.close);
        close.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void showBottomSheetFlagChooserDialog() {
        SimpleIconAdapter adapter = new SimpleIconAdapter(
                requireContext(), Flag.getFlagsForAdapter(requireContext()),
                R.layout.icon_name_item_no_checkbox,
                new String[]{"icon", "name"},
                new int[]{R.id.icon, R.id.name});
        final FlagChooserBottomSheetDialog bottomSheetDialog = new FlagChooserBottomSheetDialog(adapter, (v, dlg, flag) -> {
            if (flag != null) {
                // Set flag
                imageViewIcon.setImageDrawable(getResources().getDrawable(flag.getDrawableId(Flag.ICON_LARGE_SIZE)));
                imageViewIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                dlg.dismiss();
            } else {
                SimpleDialog.errorDialog(AddOrEditAccountFragment.this.requireContext(), AddOrEditAccountFragment.this.getString(R.string.error), AddOrEditAccountFragment.this.getString(R.string.country_not_found)).show();
            }
        });
        bottomSheetDialog.show(requireActivity().getSupportFragmentManager(), "ModalFlagChooserDialog");
    }

    @NonNull
    private void showBottomSheetCurrencyDialog() {
        // Check for OER api key before going further
        if (!StaticData.isOerApiKeyValid()) {
            SimpleDialog.errorDialog(
                    requireContext(),
                    getString(R.string.error),
                    getString(R.string.error_no_oer_api_key),
                    (dialog, which) -> {
                        // Display preferences
                        Intent intent = new Intent(requireContext(), PreferencesActivity.class);
                        startActivity(intent);
                    }).show();
            return;
        }

        final CurrencyChooserBottomSheetDialog dialog = new CurrencyChooserBottomSheetDialog(
                currencyAdapter,
                (v, dlg, currency) -> {
                    // Add this currency to the db
                    Currency cur = new Currency();
                    cur.setIsoCode(currency.getCode());
                    cur.setLongName(currency.getName());
                    cur.setShortName(currency.getCurrencySymbol());
                    try {
                        OpenExchangeRatesAsyncUpdater updater = new OpenExchangeRatesAsyncUpdater(requireActivity(), StaticData.getPreferenceValueString(StaticData.PREF_KEY_OER_API_KEY));
                        updater.setAsyncListener(() -> {
                            cur.insertOrUpdate();
                            ApplicationDrawer.getInstance().updateDrawerBadges();

                            Currency dummyCurrency = new Currency();
                            Cursor c = dummyCurrency.fetchAll();
                            spinnerCurrencyManager.setAdapter(c, CurrencyData.KEY_LONG_NAME);
                            spinnerCurrencyManager.setSpinnerPositionCursor(cur.getLongName(), new Currency());

                            refreshDisplay();
                            dlg.dismiss();
                        });
                        updater.execute(cur);
                    } catch (Exception e) {
                        Snackbar.make(
                                getView(),
                                getString(R.string.error_currency_update) + " - " + e.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
        dialog.show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
    }

    private void initOerFetcher() {
        oerFetcher = new OpenExchangeRatesAsyncFetcher(getActivity());
        // init currencies list - set on callback method (async call)
        try {
            oerFetcher.setAsyncListener(() -> {
                currenciesList = oerFetcher.getCurrencies();
                currencyAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        currenciesList);
            });
            oerFetcher.execute((Currency[]) null);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(
                            requireContext(),
                            requireView(),
                            getString(R.string.error) + " err=" + e.getMessage(),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected boolean validate() {
        return !"".equals(textViewName.getText().toString().trim());
    }

    @Override
    protected void baseObjectToFields(Account obj) {
        if (obj == null) {
            textViewName.setText("");
            imageViewIcon.setImageResource(R.drawable.tmh_icon_48);
        } else {
            textViewName.setText(obj.getName());

            // Loading icon
            ImageUtil.setIcon(imageViewIcon, obj.getIcon());

            if (obj.getCurrency() != null)
                spinnerCurrencyManager.setSpinnerPositionCursor(obj.getCurrency().getLongName(), new Currency());
        }
    }

    @Override
    protected void fieldsToBaseObject(Account obj) {
        if (obj != null) {
            String account = textViewName.getText().toString().trim();
            Bitmap bitmap = ImageUtil.drawableToBitmap(imageViewIcon.getDrawable());

            obj.setIconBytes(ImageUtil.getBytesFromBitmap(bitmap));
            obj.setName(account);

            Cursor c = spinnerCurrencyManager.getSelectedItem();
            Currency cur = new Currency();
            cur.toDTO(c);
            obj.setCurrency(cur);
        }
    }

    @Override
    public void onResume() {
        // Invalidate current account if we are editing it
        try {
            if (super.isEditing() && super.getObj().getId().intValue() == StaticData.getCurrentAccount().getId().intValue()) {
                StaticData.invalidateCurrentAccount();
            }
        } catch (NullPointerException npe) {
            // Do nothing more here
        }

        super.onResume();
    }

    class AppsAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(requireContext());

            ResolveInfo info = mApps.get(position % mApps.size());

            i.setImageDrawable(info.activityInfo.loadIcon(requireContext().getPackageManager()));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final int w = (int) (36 * getResources().getDisplayMetrics().density + 0.5f);
            i.setLayoutParams(new GridView.LayoutParams(w, w));
            return i;
        }


        public final int getCount() {
            return mApps.size();
        }

        public final Object getItem(int position) {
            return mApps.get(position % mApps.size());
        }

        public final long getItemId(int position) {
            return position;
        }
    }

    @Override
    public Map<Integer, Object> handleRefreshBackground(AbstractAsyncTask asyncTask) {
        return super.handleRefreshBackground(asyncTask);
    }

    @Override
    public void handleRefreshEnding(Map<Integer, Object> results) {
        super.handleRefreshEnding(results);
    }

    @Override
    protected void onItemAdd() {
        super.onItemAdd();
        ApplicationDrawer.getInstance().refreshAccountsProfile();
    }

    @Override
    protected void onItemEdit() {
        super.onItemEdit();
        ApplicationDrawer.getInstance().refreshAccountsProfile();
    }
}
