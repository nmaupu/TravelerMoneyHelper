package org.maupu.android.tmh.util;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * First naive implementation of data export to help debugging other databases
 */
public class ImportExportUtil {
    private static final Class<ImportExportUtil> TAG = ImportExportUtil.class;

    public static boolean exportCurrentDatabase(String filename, View view) {
        try {
            String sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

            File backupDir = new File(sd, TmhApplication.APP_NAME_SHORT);
            if (!backupDir.mkdirs()) {
                return false;
            }

            String currentDbPath = TmhApplication.getDatabaseHelper().getCurrentDbPath();
            TmhLogger.d(TAG, "Current database path = " + currentDbPath);

            File srcFile = new File(currentDbPath);
            File dstFile = new File(backupDir.getAbsolutePath(), filename);

            FileUtil.copyFile(new FileInputStream(srcFile), new FileOutputStream(dstFile));

            if (view != null) {
                Snackbar.make(
                        view,
                        "File exported to : " + dstFile.getAbsolutePath(),
                        Snackbar.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean importDatabase(Activity activity, Uri uri, String destinationDbName) throws IOException {
        String dbName = DatabaseHelper.DATABASE_PREFIX + destinationDbName;

        // creating a new DB
        TmhApplication.changeOrCreateDatabase(dbName);
        String currentDbPath = TmhApplication.getDatabaseHelper().getCurrentDbPath();

        // Open backup db file
        File destFile = new File(currentDbPath);

        // importing data
        FileUtil.copyFile(activity.getContentResolver().openInputStream(uri), new FileOutputStream(destFile));

        return true;
    }
}
