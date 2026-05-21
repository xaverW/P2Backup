package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;

public class CopyAllFactory {
    public CopyAllFactory() {
    }

    public static boolean copyAllFilesToBackup(BackupInfo backupInfos) {
//        Path toDataPath = FileFactory.getToPath(backupInfos);
//        if (!CopyFactory.makeDirsOfFile(backupInfos.runnerDto.getDataFileList(), toDataPath)) {
//            return false;
//        }

        // und jetzt kopieren
        return CopyFactory.copyFiles(backupInfos, backupInfos.runnerDto.getDataFileList());
    }
}
