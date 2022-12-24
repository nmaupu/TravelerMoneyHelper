package org.maupu.android.tmh.util.drive;

import org.apache.commons.io.FilenameUtils;
import org.maupu.android.tmh.database.DatabaseHelper;

import java.io.File;

public class BackupDbFileHelper {
    private String name;

    /**
     * Constructs a {@link BackupDbFileHelper} object
     *
     * @param name either an absolute database name or drive filename
     */
    public BackupDbFileHelper(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toDriveFilename() {
        File f = new File(name);
        return DatabaseHelper.stripDatabaseFileName(f.getName()) + ".db";
    }

    public String toDbName() {
        return DatabaseHelper.DATABASE_PREFIX + FilenameUtils.removeExtension(name);
    }
}
