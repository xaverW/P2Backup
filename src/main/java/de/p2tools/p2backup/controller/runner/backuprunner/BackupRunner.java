package de.p2tools.p2backup.controller.runner.backuprunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.controller.sqlite.SqlTable;
import de.p2tools.p2backup.gui.dialog.BackupErrorDialogController;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2ToolsFactory;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackupRunner {

    private final ProgData progData;
    private final BackupInfo backupInfos;

    public BackupRunner(BackupInfo backupInfos) {
        this.progData = ProgData.getInstance();
        this.backupInfos = backupInfos;
    }

    public void makeBackup() {
        backupInfos.runnerDto.startRunner(backupInfos.getName());
        backupInfos.runnerDto.setFirstRun(true);

        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        new Thread(() -> {
            P2Log.sysLog("Start BackupRunner: " + this.backupInfos.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup Start");
            P2Log.sysLog("=======================================");

            this.backupInfos.runnerDto.setRunnerText("Start Backup");

            // erst mal alles putzen, prüfen und Infos sammeln
            if (!BackupRunnerFactory.collectInfos(this.backupInfos)) {
                close();
                return;
            }

            // =======================
            // dann das Backup starten
            // =======================


            // ===============================
            // backup Verzeichnis anlegen
            BackupData backupData = new BackupData();
            backupData.setBackupInfoId(backupInfos.getId());
            this.backupInfos.runnerDto.setBackupData(backupData);

            AtomicBoolean a = new AtomicBoolean(true);
            Platform.runLater(() -> {
                backupData.setStartDate(this.backupInfos.getLastStartDate());
                backupData.setSubPath(this.backupInfos.runnerDto.getDataSubPath());
                a.set(false);
            });
            while (a.get()) {
                P2ToolsFactory.pause(100);
            }

            if (!BackupRunnerFactory.makeBackupDirectory(this.backupInfos)) {
                quitt(false);
                return;
            }


            // ===============================
            // toPath Verzeichnis anlegen
            if (!BackupRunnerFactory.makeToPathDirectory(this.backupInfos)) {
                quitt(false);
                return;
            }

            // ================================
            // falls es das erste Mal ist, muss
            // zuerst die DB angelegt werden
            if (!SqlTable.makeBackupDb(this.backupInfos)) {
                quitt(false);
                return;
            }


            // ===============================
            // fromHash erstellen und toPath eintragen
            this.backupInfos.runnerDto.setRunnerText("Dateien einlesen");
            if (!BackupRunnerFactory.makeFromHash(this.backupInfos)) {
                quitt(false);
                return;
            }
            if (this.backupInfos.runnerDto.isStop()) {
                quitt(false);
                return;
            }

            // ===============================
            // Dateien kopieren
            this.backupInfos.runnerDto.setRunnerText("Dateien kopieren");
            if (!BackupRunnerFactory.copyFilesToBackup(this.backupInfos)) {
                quitt(false);
                return;
            }
            if (this.backupInfos.runnerDto.isStop()) {
                quitt(false);
                return;
            }

            // ============================
            // Überzählige Backups löschen
            if (!BackupRunnerFactory.deleteBackupData(this.backupInfos)) {
                quitt(false);
                return;
            }

            // ============================
            // Update BackupData
            if (!BackupRunnerFactory.updateBackupData(this.backupInfos)) {
                quitt(false);
                return;
            }

            quitt(true);
        }).start();
    }

    private void quitt(boolean ret) {
        backupInfos.runnerDto.getBackupData().setOk(ret);

        if (backupInfos.runnerDto.isStop() || !ret) {
            backupInfos.runnerDto.setOk(false);
            // dann wurde abgebrochen oder hatte einen Fehler
            final Stage stage;
            if (progData.primaryStageSmall != null && progData.primaryStageSmall.isShowing()) {
                stage = progData.primaryStageSmall;
            } else {
                stage = progData.primaryStage;
            }

            Platform.runLater(() -> new BackupErrorDialogController(stage, backupInfos));

            // delete backup-files
            Path toPath = backupInfos.runnerDto.getToPath();
            try {
                if (toPath != null && toPath.toFile().isDirectory() && toPath.toFile().exists()) {
                    FileUtils.deleteDirectory(toPath.toFile());
                }
            } catch (IOException ex) {
                P2AlertAppThread.showErrorAlert("Backup",
                        "Der bereits angelegte Backup-Ordner:\n" +
                                toPath + "\n" +
                                "konnte nicht gelöscht werden.");
            }

            // cleanUp DB todo! rest??
            SqlFileData.deleteFileList(backupInfos, backupInfos.runnerDto.getBackupData());

        } else {
            backupInfos.runnerDto.setOk(true);
        }
        close();
    }

    private void close() {
        backupInfos.runnerDto.stopRunner();
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

    }
}
