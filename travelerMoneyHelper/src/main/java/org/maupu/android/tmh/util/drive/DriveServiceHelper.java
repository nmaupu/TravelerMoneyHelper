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
    /**
     * Upload files to a Google Drive account
     *
     * @param driveService          {@link Drive} object
     * @param refresher             refresher used to publish progress to the main thread UI
     * @param backupFolderName      backup folder name to use for backups and cleanup
     * @param files                 list of files to backup
     * @param retentionDurationDays retention duration. Older backups then retention days will be delete. If retentionDurationDays is <= 0, deletion is disabled
     * @return
     * @throws IOException
     */
    public static Task<FileList> upload(Drive driveService, AbstractAsyncTask refresher, String backupFolderName, BackupDbFileHelper[] files, int retentionDurationDays) throws IOException {
        TaskCompletionSource<FileList> taskCompletionSource = new TaskCompletionSource<>();
        FileList fileList = new FileList();

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

        int numberOfDriveOperations = files.length;
        int progressValue = 0;

        // delete old backups
        if (retentionDurationDays > 0) {
            Date deleteBeforeDate = new Date();
            deleteBeforeDate.setTime(new Date().getTime() - (long) retentionDurationDays * 24 * 3600 * 1000);
            String deleteBefore = DateUtil.dateToStringRFC3339(deleteBeforeDate);

            StringBuilder q = new StringBuilder()
                    .append("mimeType = 'application/vnd.google-apps.folder'")
                    .append(" and ")
                    .append("trashed = false")
                    .append(" and ")
                    .append("'" + backupFolderId + "' in parents")
                    .append(" and ")
                    .append("modifiedTime < " + "'" + deleteBefore + "'");
            FileList results = driveService.files()
                    .list()
                    .setQ(q.toString())
                    .execute();

            int nbResults = 0;
            if (results != null)
                nbResults = results.getFiles().size();

            numberOfDriveOperations += nbResults;

            if (nbResults > 0) {
                for (File f : results.getFiles()) {
                    driveService.files()
                            .delete(f.getId())
                            .execute();
                    refresher.publishProgress(progressValue++, numberOfDriveOperations);
                }
            }
            // end deletion old backups
        }

        if (files.length == 0)
            return taskCompletionSource.getTask();

        // Create a folder for this backup with the current date as name
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
        StringBuilder q = new StringBuilder()
                .append("mimeType = 'application/vnd.google-apps.folder'")
                .append(" and ")
                .append("trashed = false")
                .append(" and ")
                .append("properties has {key='app' and value='" + TmhApplication.APP_NAME + "'}");
        FileList results = driveService.files()
                .list()
                .setQ(q.toString())
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

    private static void deleteOldBackups(Drive driveService, String backupFolderId, int retentionDurationDays) throws IOException {
        String parentFolderId = getBackupFolderId(driveService, backupFolderId);
        if (parentFolderId == null || "".equals(parentFolderId))
            return;


    }

    public static FileList getAllBackups(Drive driveService, String folderName) throws IOException {
        FileList fileList = new FileList();

        String parentFolderId = getBackupFolderId(driveService, folderName);
        if (parentFolderId == null || "".equals(parentFolderId))
            return fileList;

        StringBuilder q = new StringBuilder()
                .append("mimeType = 'application/vnd.google-apps.folder'")
                .append(" and ")
                .append("trashed = false")
                .append(" and ")
                .append("'" + parentFolderId + "' in parents ");
        return driveService.files()
                .list()
                .setQ(q.toString())
                .execute();
    }
}
