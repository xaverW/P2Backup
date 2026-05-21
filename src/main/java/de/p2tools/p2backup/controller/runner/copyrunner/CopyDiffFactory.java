package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;

import java.util.HashMap;
import java.util.Map;

public class CopyDiffFactory {
    public CopyDiffFactory() {
    }

    public static boolean copyDiffFilesToBackup(BackupInfo backupInfo) {
        FileDataList oldFileList = new FileDataList();
        FileDataList copyList = new FileDataList();
        FileDataList moveList = new FileDataList();
        final Map<String, FileData> oldBackupFileMap = new HashMap<>(); // sind alle Dateien im alten Backup

        // ======================
        // altes Backup laden
        BackupData oldBackup = backupInfo.getBackupDataList().getLast();
        final String oldToPathStr = FileFactory.getToPathStr(backupInfo, oldBackup);
        SqlFileData.readBackupFileList(backupInfo, oldBackup, oldFileList);

        oldFileList.forEach(fileData -> {
            if (!fileData.getFilePathStr().isEmpty() &&
                    !fileData.getToPathStr().isEmpty() &&
                    !fileData.isError()) {
                oldBackupFileMap.put(fileData.getFilePathStr(), fileData);
            }
        });
        oldFileList.clear();

        // ===============
        // suchen was kopiert werden muss
        backupInfo.runnerDto.getDataFileList().forEach(f -> {
            if (f.isError()) {
                // dann konnte es nicht gelesen werden, also nix!
                return;
            }

            FileData oldFile = oldBackupFileMap.get(f.getFilePathStr());
            if (oldFile == null || !f.getHash().equals(oldFile.getHash())) {
                // dann gibt es sie nicht oder
                // oder sie sind nicht gleich -> aus DATEIEN kopieren
                copyList.add(f);

            } else {
                // dann sind sie gleich -> move aus altem Backup
                FileData moveData = f.getCopy();
//                moveData.setFilePathStr(oldFile.getBackupFilePathStr()); // DATEN-Pfad ist der alte BACKUP-Pfad
                if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
                    // aus der Map löschen, gibts dann nicht mehr
                    oldBackupFileMap.remove(oldFile.getFilePathStr());
                } else if (backupInfo.getHow() == ProgConst.BACKUP_INTELLIGENT) {
                    // dann muss der toPath geändert werden!
//                    moveData.setToPathStr(toPathStr);

                }
                moveList.add(moveData);
            }
        });

        // ===============
        // oldBackup aktualisieren, backupFiles des alten Backup
        oldFileList.setAll(oldBackupFileMap.values()); // sind alle bei INTELLIGENT oder der Rest bei DIFF
        oldBackup.setCount(oldFileList.size());
        if (!SqlFileData.updateBackupFileList(backupInfo, oldBackup.getId(), oldFileList)) {
            backupInfo.runnerDto.setStop();
        }

        // ====================
        // Dirs für die Dateien anlegen
//        if (!CopyFactory.makeDirsOfFile(backupInfo.runnerDto.getDataFileList(), toPath)) {
//            return false;
//        }

        // ======================
        // und jetzt kopieren/linken/moven
        if (!CopyFactory.copyFiles(backupInfo, copyList)) {
            return false;
        }

        if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
            // move files form OldBAckup
            return CopyFactory.moveFiles(backupInfo, oldToPathStr, moveList);
        } else {
            // link files from OldBackup
            return CopyFactory.linkFiles(backupInfo, oldToPathStr, moveList);
        }
    }
}