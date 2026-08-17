package de.p2tools.p2backup.controller.data.resetdata;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.filedata.FileData;

public class ResetData {
    private FileData fileData;
    private BackupData backupData;

    public ResetData(FileData fileData, BackupData backupData) {
        this.fileData = fileData;
        this.backupData = backupData;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    public BackupData getBackupData() {
        return backupData;
    }

    public String getFileName() {
        return fileData.getFilePathStr();
    }

    public void setBackupData(BackupData backupData) {
        this.backupData = backupData;
    }

    public long getBackupId() {
        return backupData.getId();
    }

    public String getBackupSubPath() {
        return backupData.getSubPath();
    }
}
