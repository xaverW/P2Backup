package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.HashFactory;
import de.p2tools.p2backup.controller.runner.tools.RepairFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolCheckBackupQuick;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.dialog.BackupErrorListDialogController;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.tools.P2Wait;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CopyDiffFactory {
    public CopyDiffFactory() {
    }

    public static boolean copyDiffFilesToBackup(BackupInfo backupInfo) {
        Stage stage;
        if (ProgData.getInstance().primaryStageSmall != null &&
                ProgData.getInstance().primaryStageSmall.isShowing()) {
            stage = ProgData.getInstance().primaryStageSmall;
        } else {
            stage = ProgData.getInstance().primaryStage;
        }


        // =========================================
        // erst mal das alte Backup überprüfen
        // =========================================
        BackupData oldBackup = backupInfo.getBackupDataList().getLast();
        final String oldToPathStr = FileFactory.getToPathStr(backupInfo, oldBackup);

        AtomicBoolean a = new AtomicBoolean(true);
        FileDataList errorList = new FileDataList();
        new ToolCheckBackupQuick(backupInfo, oldBackup, a).compare(errorList);
        while (a.get()) {
            P2Wait.pause(500);
        }

        if ((!errorList.isEmpty())) {
            ObjectProperty<BackupErrorListDialogController.ERROR> repairProp = new SimpleObjectProperty<>(null);
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            Platform.runLater(() -> {
                // wird im GUI angezeigt
                new BackupErrorListDialogController(backupInfo, errorList, repairProp);
                atomicBoolean.set(false);
            });
            while (atomicBoolean.get()) {
                P2Wait.pause(500);
            }
            switch (repairProp.get()) {
                case null -> {
                    return false;
                }
                case CANCEL -> {
                    return false;
                }
                case IGNORE -> {
                    return CopyFactory.copyFiles(backupInfo, backupInfo.runnerDto.getDataFileList());
                }
                case REPAIR -> {
                    if (!RepairFactory.repairBackup(backupInfo, errorList)) {
                        P2AlertAppThread.showErrorAlert(stage,
                                "Fehler reparieren",
                                "Es konnten nicht alle fehlerhaften Dateien aus dem " +
                                        "Backup gelöscht werden");
                        return false;
                    }
                }
            }
        }


        // =========================================
        // und jetzt Dateien kopieren
        // =========================================

        backupInfo.runnerDto.setRunnerMax(backupInfo.runnerDto.getDataFileList().size() * 2); // läuft 2x durch
        backupInfo.runnerDto.runnerDoubleProperty().set(true); // läuft 2x durch

        final Map<String, FileData> oldBackupFileMap = new HashMap<>(); // sind alle Dateien im alten Backup
        final FileDataList oldBackupFileList = new FileDataList();
        final FileDataList copyList = new FileDataList();
        final FileDataList moveList = new FileDataList();

        // altes Backup laden
        SqlFileData.readBackupFileList(backupInfo, oldBackup, oldBackupFileList);
        oldBackupFileList.forEach(fileData -> {
            if (!fileData.getFilePathStr().isEmpty() &&
                    !fileData.getToPathStr().isEmpty() &&
                    !fileData.isErrorHash()) {
                oldBackupFileMap.put(fileData.getFilePathStr(), fileData);
            }
        });

        // suchen was kopiert werden muss
        for (FileData fileData : backupInfo.runnerDto.getDataFileList()) {
            backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
            backupInfo.runnerDto.addRunnerAlreadyDone();
            HashFactory.setFileData(backupInfo, false, fileData);
            fileData.setErrorHash(fileData.getHash().equals(FileFactory.HASH_ERROR));

            if (backupInfo.runnerDto.isStop()) {
                return false;
            }
            if (fileData.isErrorHash()) {
                continue;
            }

            FileData oldFile = oldBackupFileMap.remove(fileData.getFilePathStr());
            if (oldFile == null || !fileData.getHash().equals(oldFile.getHash())) {
                // dann gibt es sie nicht oder
                // sie sind nicht gleich -> aus DATEIEN kopieren
                copyList.add(fileData);

            } else {
                // dann sind sie gleich -> move aus altem Backup
                moveList.add(fileData.getCopy());
            }
        }


        // =========================================
        // und jetzt kopieren/linken/moven
        // =========================================
        if (!CopyFactory.copyFiles(backupInfo, copyList)) {
            return false;
        }

        if (backupInfo.getHow() == ProgConst.BACKUP_DIFF) {
            oldBackupFileList.setAll(oldBackupFileMap.values()); // ist der Rest bei DIFF
            FileDataList resetList = new FileDataList();
            boolean ret = CopyFactory.moveFiles(backupInfo, oldToPathStr, moveList, resetList);

            if (backupInfo.runnerDto.isStop() && !resetList.isEmpty()) {
                // ======== FEHLER ======================
                // dann wieder alles zurückfahren, resetList sind die kopierten
                backupInfo.runnerDto.setRunnerText("Abbruch: aufräumen");
                backupInfo.runnerDto.setRunnerMax(resetList.size());

                // die gesamte moveList wieder eintragen
                oldBackupFileList.addAll(moveList);

                // und jetzt zurück kopieren
                boolean error = false;
                for (FileData fileData : resetList) {
                    File fromFile = fileData.getBackupFilePath().toFile();
                    File toFile = fileData.getBackupFilePath(oldToPathStr).toFile();
                    try {
                        backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                        backupInfo.runnerDto.addRunnerAlreadyDone();
                        FileUtils.moveFileToDirectory(fromFile, toFile.getParentFile(), true);
                    } catch (IOException e) {
                        error = true;
                    }
                }
                if (error) {
                    P2AlertAppThread.showErrorAlert(stage, "Backup abbrechen", "Es können nicht alle " +
                            "Dateien wieder hergestellt werden. Bitte nochmals ein Backup machen!");
                }
            }

            // oldBackup aktualisieren, backupFiles des alten Backup
            oldBackup.setCount(oldBackupFileList.size());
            if (!SqlFileData.updateBackupFileList(backupInfo, oldBackup.getId(), oldBackupFileList)) {
                backupInfo.runnerDto.setStop();
            }
            return ret;


        } else {
            // ==============================
            // link files
            return CopyFactory.linkFiles(backupInfo, oldToPathStr, moveList);
        }
    }
}