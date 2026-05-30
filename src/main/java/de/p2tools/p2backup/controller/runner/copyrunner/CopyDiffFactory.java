package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.FileHashFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolCheckBackupQuick;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.tools.P2Wait;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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


        // =====================================
        // erst mal das alte Backup überprüfen
        // =====================================
        BooleanProperty foundError = new SimpleBooleanProperty(false);
        AtomicBoolean a = new AtomicBoolean(true);
        new ToolCheckBackupQuick(backupInfo, oldBackup, a).compare(foundError);
        while (a.get()) {
            P2Wait.pause(500);
        }

        if (foundError.get()) {
            Stage stage;
            if (ProgData.getInstance().primaryStageSmall != null &&
                    ProgData.getInstance().primaryStageSmall.isShowing()) {
                stage = ProgData.getInstance().primaryStageSmall;
            } else {
                stage = ProgData.getInstance().primaryStage;
            }
            P2AlertAppThread.showErrorAlert(stage, "Backup erstellen",
                    """
                            Das vorherige Backup ist beschädigt. Es kann dann kein neues Backup mit
                            "nur geänderten Dateien"
                            angelegt werden.
                            
                            Es werden stattdessen wieder alle Dateien gesichert.""");
            return CopyFactory.copyFiles(backupInfo, backupInfo.runnerDto.getDataFileList());
        }


        // =====================================
        // und jetzt Dateien kopieren
        // =====================================
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
        backupInfo.runnerDto.getDataFileList().forEach(fileData -> {
            backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
            FileHashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setError(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isError()) {
                return;
            }

            FileData oldFile = oldBackupFileMap.get(fileData.getFilePathStr());
            if (oldFile == null || !fileData.getHash().equals(oldFile.getHash())) {
                // dann gibt es sie nicht oder
                // oder sie sind nicht gleich -> aus DATEIEN kopieren
                copyList.add(fileData);

            } else {
                // dann sind sie gleich -> move aus altem Backup
                FileData moveData = fileData.getCopy();
                if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
                    // aus der Map löschen, gibts dann nicht mehr
                    oldBackupFileMap.remove(oldFile.getFilePathStr());
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