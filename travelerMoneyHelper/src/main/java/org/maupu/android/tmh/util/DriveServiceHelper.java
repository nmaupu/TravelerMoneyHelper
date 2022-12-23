package org.maupu.android.tmh.util;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.maupu.android.tmh.core.TmhApplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class DriveServiceHelper {
    private final static String TAG = DriveServiceHelper.class.getName();

    public static Task<File> upload(Drive driveService, String filepath) {
        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();
        File fileMetadata = new File();
        fileMetadata.setName("backup.db");

        // TODO pass correct folder name
        final String folderName = "foobar";

        String existingBackupFolderId = getExistingBackupFolderId(driveService, folderName);
        java.io.File file = new java.io.File(filepath);
        FileContent fileContent = new FileContent("application/octet-stream", file);

        File backupFile = null;
        try {
            if (existingBackupFolderId == null) {
                Map<String, String> props = new HashMap<>();
                props.put("app", TmhApplication.APP_NAME);
                File folderMetadata = new File()
                        .setMimeType("application/vnd.google-apps.folder")
                        .setProperties(props)
                        .setName(folderName);
                File backupFolder = driveService
                        .files()
                        .create(folderMetadata)
                        .execute();
                existingBackupFolderId = backupFolder.getId();
            }

            fileMetadata.setParents(Arrays.asList(existingBackupFolderId));
            backupFile = driveService.files().create(fileMetadata, fileContent).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        taskCompletionSource.setResult(backupFile);
        return taskCompletionSource.getTask();
    }

    private static String getExistingBackupFolderId(Drive driveService, String folderName) {
        FileList results = null;

        try {
            results = driveService.files()
                    .list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false and properties has {key='app' and value='" + TmhApplication.APP_NAME + "'}")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // TODO look for the correct folder name from results
        if (results != null && results.getFiles().size() > 0) {
            return results.getFiles().get(0).getId();
        }

        return null;
    }
}
