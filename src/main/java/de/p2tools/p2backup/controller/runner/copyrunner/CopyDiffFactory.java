package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;

import java.nio.file.Path;
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
        final Path toPath = FileFactory.getToPath(backupInfo);

        // ======================
        // altes Backup laden
        BackupData oldBackup = backupInfo.getBackupDataList().getLast();
        SqlFileData.readBackupFileList(backupInfo, oldBackup, oldFileList);

        oldFileList.forEach(fileData -> {
            if (!fileData.getFilePathStr().isEmpty() ||
                    fileData.getToPathStr().isEmpty()) {
                oldBackupFileMap.put(fileData.getFilePathStr(), fileData);
            }
        });

        // ====================
        // ToPath zum Backup eintragen
        backupInfo.runnerDto.getDataFileList().forEach(f -> {
            f.setToPathStr(FileFactory.getToPathStr(backupInfo));
//            f.setCorrBackupPath(FileFactory.getToPathStr(backupInfo));
        });

        // ===============
        // suchen was kopiert werden muss
        backupInfo.runnerDto.getDataFileList().forEach(f -> {

            FileData oldFile = oldBackupFileMap.get(f.getFilePathStr());
            if (oldFile == null ||
                    oldFile.getFilePathStr().isEmpty() ||
                    oldFile.getToPathStr().isEmpty() ||
                    !f.getHash().equals(oldFile.getHash())) {

                // dann gibt es sie nicht oder
                // hat kein Backup
                // oder sie sind nicht gleich -> aus DATEIEN kopieren
                copyList.add(f);

            } else {
                // dann sind sie gleich -> move aus altem Backup
                FileData moveData = f.getCopy();
                moveData.setFilePathStr(oldFile.getBackupFilePath().toString()); // DATEN-Pfad ist der alte BACKUP-Pfad
                if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
                    // aus der Map löschen
                    oldBackupFileMap.remove(oldFile.getFilePathStr());

                    // nur beim MOVE ist der BackupPath dann leer
                    oldFile.setFilePathStr(""); // gibts dann ja nicht mehr
                    oldFile.setToPathStr(""); // gibts dann ja nicht mehr
                }
                moveList.add(moveData);
            }
        });

        // ===============
        // oldBackup aktualisieren, backupFiles des alten Backup
        oldFileList.setAll(oldBackupFileMap.values());
        int count = 0;
        for (FileData fileData : oldFileList) {
            if (!fileData.getFilePathStr().isEmpty()) {
                // nur dann gibts das BackupFile noch
                ++count;
            }
        }
        oldBackup.setCount(count);
        if (!SqlFileData.updateBackupFileList(backupInfo, oldBackup.getId(), oldFileList)) {
            backupInfo.runnerDto.setStop();
        }

        // ====================
        // BackupPfad anlegen
        if (!CopyFactory.checkToPath(FileFactory.getToPath(backupInfo))) {
            return false;
        }

        // ====================
        // Dirs für die Dateien anlegen
        if (!CopyFactory.makeDirsOfFile(backupInfo.runnerDto.getDataFileList(), toPath)) {
            return false;
        }

        // ======================
        // und jetzt kopieren/linken/moven
        if (!CopyFactory.copyFiles(backupInfo, copyList)) {
            return false;
        }

        if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
            // move files form OldBAckup
            return CopyFactory.moveFiles(backupInfo, moveList);
        } else {
            // link files from OldBackup
            return CopyFactory.linkFiles(backupInfo, moveList);
        }
    }
}