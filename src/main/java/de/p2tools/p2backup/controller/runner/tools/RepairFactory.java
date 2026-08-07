package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.List;

public class RepairFactory {
    private RepairFactory() {
    }

    public static boolean repairBackup(BackupInfo backupInfo, List<FileData> errorList) {
        if (!SqlFileData.deleteBackupFileList(backupInfo, errorList)) {
            return false;
        }
        return deleteErrorFiles(errorList);
    }

    private static boolean deleteErrorFiles(List<FileData> errorList) {
        FileData fileData = null;
        boolean ret = true;
        try {
            for (FileData f : errorList) {
                fileData = f;
                if (fileData.isErrorDiff() || fileData.isOnlyInBackup() || fileData.isErrorHash()) {
                    Path baPath = fileData.getBackupFilePath();
                    if (baPath.toFile().exists()) {
                        if (!baPath.toFile().delete()) {
                            P2Log.errorLog(956234780, "Fehlerhafte Dateien aus dem Backup löschen");
                            P2Log.errorLog(956234780, baPath.toString());
                            ret = false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            P2Log.errorLog(959562354, "Fehlerhafte Dateien aus dem Backup löschen");
            if (fileData != null) {
                P2Log.errorLog(959562354, fileData.getBackupFilePathStr());
            }
            ret = false;
        }

        return ret;
    }
}
