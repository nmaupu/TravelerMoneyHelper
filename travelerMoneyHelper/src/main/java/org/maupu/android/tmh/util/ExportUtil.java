package org.maupu.android.tmh.util;

import android.os.Environment;
import android.widget.Toast;

import org.maupu.android.tmh.core.TmhApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * First naive implementation of data export to help debugging other dabatases
 */
public class ExportUtil {
    private static final Class TAG = ExportUtil.class;

    public static boolean exportCurrentDatabase(String filename) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            File backupDir = new File(sd.getAbsolutePath() + "/" + TmhApplication.APP_NAME_SHORT);
            backupDir.mkdirs();

            File backupFile = new File(backupDir.getAbsolutePath() + "/" + filename);

            String currentDbPath = TmhApplication.getDatabaseHelper().getCurrentDbName();
            TmhLogger.d(TAG, "Current database path = " + currentDbPath);

            File srcFile = new File(currentDbPath);
            File dstFile = new File(backupFile.getAbsolutePath());

            dstFile.createNewFile();

            FileChannel src = new FileInputStream(srcFile).getChannel();
            FileChannel dst = new FileOutputStream(dstFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Toast.makeText(TmhApplication.getAppContext(), "File exported to : " + backupFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
