package de.p2tools.p2backup.controller.runner.deleterunner;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class DeleteRunnerFactory {
    private DeleteRunnerFactory() {
    }

    public static boolean deleteBackup(BackupInfo backupInfos, BackupData backupData) {
        // das Backup wird einfach gelöscht
        boolean ret = false;
        try {
            Path file = backupData.getToPath(backupInfos);
            File f = file.toFile();
            if (f.exists() && f.isDirectory()) {
                FileUtils.deleteDirectory(f);
            }
            ret = true;
        } catch (IOException exception) {
            P2AlertAppThread.showErrorAlert("Backup löschen",
                    "Das Backup konnte nicht gelöscht werden.");
        }

        return ret;
    }
}
