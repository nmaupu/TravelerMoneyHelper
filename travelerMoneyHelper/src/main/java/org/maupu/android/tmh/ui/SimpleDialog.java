package org.maupu.android.tmh.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public abstract class SimpleDialog {
    public static Builder errorDialog(Context context, String title, String content) {
        return errorDialog(context, title, content, (dialog, id) -> dialog.dismiss());
    }

    public static Builder errorDialog(Context context, String title, String content, OnClickListener listenerOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(content)
                .setTitle(title)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, listenerOk);

        builder.create();
        return builder;
    }

    public static Builder errorDialogWithCancel(Context context, String title, String content, OnClickListener listenerOk, OnClickListener listenerCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(content)
                .setTitle(title)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, listenerOk)
                .setNegativeButton(android.R.string.cancel, listenerCancel);

        builder.create();
        return builder;
    }

    public static Builder confirmDialog(Context context, String confirmQuestion, OnClickListener listenerOk) {
        return errorDialogWithCancel(
                context,
                context.getResources().getString(android.R.string.dialog_alert_title),
                confirmQuestion,
                listenerOk,
                (dialog, which) -> dialog.cancel()
        );
    }
}
