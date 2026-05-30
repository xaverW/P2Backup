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
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackupRunner {

    private final ProgData progData;
    private final BackupInfo backupInfo;

    public BackupRunner(BackupInfo backupInfo) {
        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
    }

    public void makeBackup() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        backupInfo.runnerDto.setDoneFirstRun(true);

        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        new Thread(() -> {
            P2Log.sysLog("Start BackupRunner: " + this.backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup Start");
            P2Log.sysLog("=======================================");


            // ===============================
            // erst mal alles putzen, prüfen und Infos sammeln
            // ===============================
            this.backupInfo.runnerDto.setRunnerText("Starte Backup: " + this.backupInfo.getName());
            if (!BackupRunnerFactory.collectInfos(this.backupInfo)) {
                close();
                return;
            }


            // ===============================
            // backup Verzeichnis anlegen
            // ===============================
            BackupData backupData = new BackupData();
            backupData.setBackupInfoId(backupInfo.getId());
            this.backupInfo.runnerDto.setBackupData(backupData);

            AtomicBoolean a = new AtomicBoolean(true);
            Platform.runLater(() -> {
                backupData.setStartDate(this.backupInfo.getLastStartDate());
                backupData.setSubPath(this.backupInfo.runnerDto.getDataSubPath());
                a.set(false);
            });
            while (a.get()) {
                P2Wait.pause(100);
            }

            if (!BackupRunnerFactory.makeBackupDirectory(this.backupInfo)) {
                quitt(false);
                return;
            }


            // ===============================
            // toPath Verzeichnis anlegen
            // ===============================
            if (!BackupRunnerFactory.makeToPathDirectory(this.backupInfo)) {
                quitt(false);
                return;
            }


            // ================================
            // zuerst die DB angelegt werden
            // ===============================
            if (!SqlTable.makeBackupDb(this.backupInfo)) {
                quitt(false);
                return;
            }


            // ===============================
            // fromHash erstellen und toPath eintragen
            // ===============================
            this.backupInfo.runnerDto.setRunnerText("Dateien lesen");
            if (!BackupRunnerFactory.makeFromHash(this.backupInfo)) {
                quitt(false);
                return;
            }
            if (this.backupInfo.runnerDto.isStop()) {
                quitt(false);
                return;
            }


            // ===============================
            // Dateien kopieren
            // ===============================
            this.backupInfo.runnerDto.setRunnerText("Dateien kopieren");
            if (!BackupRunnerFactory.copyFilesToBackup(this.backupInfo)) {
                quitt(false);
                return;
            }
            if (this.backupInfo.runnerDto.isStop()) {
                quitt(false);
                return;
            }


            // ============================
            // BackupData schreiben
            // ===============================
            backupInfo.runnerDto.resetRunner();
            backupInfo.runnerDto.setRunnerText("Aufräumen");

            if (!BackupRunnerFactory.writeBackupData(this.backupInfo)) {
                quitt(false);
                return;
            }


            // ============================
            // Überzählige Backups löschen (max Anzahl)
            // ===============================
            if (!BackupRunnerFactory.deleteBackupData(this.backupInfo)) {
                quitt(false);
                return;
            }

            quitt(true);
        }).start();
    }

    private void quitt(boolean ret) {
        backupInfo.runnerDto.getBackupData().setOk(ret);

        // ==============================================
        // Fehler
        // ==============================================
        if (backupInfo.runnerDto.isStop() || !ret) {
            backupInfo.runnerDto.setOk(false);
            // dann wurde abgebrochen oder hatte einen Fehler
            final Stage stage;
            if (progData.primaryStageSmall != null && progData.primaryStageSmall.isShowing()) {
                stage = progData.primaryStageSmall;
            } else {
                stage = progData.primaryStage;
            }

            Platform.runLater(() -> new BackupErrorDialogController(stage, backupInfo));

            // delete backup-files
            Path toPath = backupInfo.runnerDto.getToPath();
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
            SqlFileData.deleteFileList(backupInfo, backupInfo.runnerDto.getBackupData());

        } else {
            // ==============================================
            // dann passts
            // ==============================================
            backupInfo.runnerDto.setOk(true);
        }

        close();
    }

    private void close() {
        backupInfo.runnerDto.stopRunner();
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

    }
}
