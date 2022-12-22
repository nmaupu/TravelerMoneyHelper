package org.maupu.android.tmh.util;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public abstract class DriveServiceHelper {
    private final static String TAG = DriveServiceHelper.class.getName();

    public static Task<File> upload(Drive driveService, String filepath) {
        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();
        File fileMetadata = new File();
        fileMetadata.setName("yala.png");

        java.io.File file = new java.io.File(filepath);
        FileContent fileContent = new FileContent("image/png", file);

        File myFile = null;
        try {
            myFile = driveService.files().create(fileMetadata, fileContent).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        taskCompletionSource.setResult(myFile);
        return taskCompletionSource.getTask();
    }
}
