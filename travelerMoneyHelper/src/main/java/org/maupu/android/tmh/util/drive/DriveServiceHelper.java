package org.maupu.android.tmh.util.drive;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.maupu.android.tmh.core.TmhApplication;
import org.maupu.android.tmh.ui.async.AbstractAsyncTask;
import org.maupu.android.tmh.util.DateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DriveServiceHelper {
    public static Task<FileList> upload(Drive driveService, AbstractAsyncTask refresher, String backupFolderName, BackupDbFileHelper[] files) throws IOException {
        TaskCompletionSource<FileList> taskCompletionSource = new TaskCompletionSource<>();
        FileList fileList = new FileList();

        if (files.length == 0)
            return taskCompletionSource.getTask();

        int numberOfDriveOperations = files.length + 2;
        int progressValue = 0;
        refresher.publishProgress(0);

        String backupFolderId = getBackupFolderId(driveService, backupFolderName);
        if (backupFolderId == null) {
            Map<String, String> props = new HashMap<>();
            props.put("app", TmhApplication.APP_NAME);
            File folderMetadata = new File()
                    .setMimeType("application/vnd.google-apps.folder")
                    .setProperties(props)
                    .setName(backupFolderName);
            File backupFolder = driveService
                    .files()
                    .create(folderMetadata)
                    .execute();
            backupFolderId = backupFolder.getId();
        }

        refresher.publishProgress(progressValue++, numberOfDriveOperations);

        // Create a folder for this backup with the current date
        File todayFolderMetadata = new File()
                .setName(DateUtil.dateToStringForFilenameLong(new Date()))
                .setMimeType("application/vnd.google-apps.folder")
                .setParents(Collections.singletonList(backupFolderId));
        File todayFolder = driveService
                .files()
                .create(todayFolderMetadata)
                .execute();
        String todayFolderId = todayFolder.getId();

        refresher.publishProgress(progressValue++, numberOfDriveOperations);


        List<File> processedFiles = new ArrayList<>();
        for (BackupDbFileHelper backupDbFileHelper : files) {
            java.io.File file = new java.io.File(backupDbFileHelper.getName());

            FileContent fileContent = new FileContent("application/octet-stream", file);

            File fileMetadata = new File();
            fileMetadata.setName(backupDbFileHelper.toDriveFilename());
            fileMetadata.setParents(Collections.singletonList(todayFolderId));

            processedFiles.add(driveService.files().create(fileMetadata, fileContent).execute());
            refresher.publishProgress(progressValue++, numberOfDriveOperations);
        }

        taskCompletionSource.setResult(fileList.setFiles(processedFiles));
        refresher.publishProgress(100);
        return taskCompletionSource.getTask();
    }

    private static String getBackupFolderId(Drive driveService, String folderName) throws IOException {
        FileList results = driveService.files()
                .list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false and properties has {key='app' and value='" + TmhApplication.APP_NAME + "'}")
                .execute();

        // Looking for provided folder name
        if (results == null || results.getFiles().size() == 0)
            return null;

        for (File f : results.getFiles())
            if (folderName.equals(f.getName()))
                return f.getId();

        // nothing has been found
        return null;
    }
}
