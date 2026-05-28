package de.p2tools.p2backup.controller.runner.deleterunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.sqlite.SqlBackupData;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeleteRunner {

    private final ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private boolean ret = true;
    private final AtomicBoolean a = new AtomicBoolean(true);

    public DeleteRunner(BackupInfo backupInfo, BackupData backupData) {
        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.backupData = backupData;
    }

    public boolean deleteBackupDoNotAsk() {
        // ========================
        // Backup-Listen suchen und löschen
        if (backupInfo.getBackupDataList().isEmpty()) {
            // dann gibts nix
            return false;
        }

        if (backupInfo.getBackupDataList().size() > 1 &&
                backupInfo.getBackupDataList().get(backupInfo.getBackupDataList().size() - 1).equals(backupData)) {
            return false;
        }
        delete();
        while (a.get()) {
            P2Wait.pause(100);
        }
        return ret;
    }

    public void deleteBackup() {
        // ========================
        // Backup-Listen suchen und löschen
        if (backupInfo.getBackupDataList().isEmpty()) {
            // dann gibts nix
            return;
        }

        if (backupInfo.getBackupDataList().size() > 1 &&
                backupInfo.getBackupDataList().get(backupInfo.getBackupDataList().size() - 1).equals(backupData)) {
            P2Alert.showInfoAlert("Backup löschen", "Erstes Backup löschen",
                    "Das erste Backup " +
                            "kann nicht sofort gelöscht werden. Zuerst müssen die älteren Backups " +
                            "gelöscht werden.");
            return;
        }

        Path path = backupData.getToPath(backupInfo);
        if (P2Alert.BUTTON.NO == P2Alert.showAlert_yes_no(progData.primaryStage, "Löschen",
                "Backup löschen", "Soll das Backup:" +
                        (path == null ? "" : "\n\n" + path + "\n\n") +
                        "gelöscht werden?")) {
            return;
        }

        // ==================
        // und jetzt löschen
        this.backupInfo.runnerDto.startRunner(this.backupInfo.getName());
        this.backupInfo.runnerDto.setDoneFirstRun(true);
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        delete();
    }

    private void delete() {
        // ==================
        // und jetzt löschen
        this.backupInfo.runnerDto.startRunner(this.backupInfo.getName());
        this.backupInfo.runnerDto.setDoneFirstRun(true);
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        new Thread(() -> {
            P2Log.sysLog("Start DeleteRunner: " + this.backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Delete Start");
            P2Log.sysLog("=======================================");
            this.backupInfo.runnerDto.setRunnerText("Start Delete");

            // zuerst die Dateien löschen
            if (!DeleteRunnerFactory.deleteBackup(backupInfo, backupData)) {
                quitt(false);
                return;
            }

            // dann aus der DB löschen
            if (!SqlBackupData.delBackupData(backupInfo, backupData)) {
                quitt(false);
                return;
            }

            // und noch im BackupInfo löschen^^^
            backupInfo.getBackupDataList().remove(backupData);
            quitt(true);
        }).start();
    }

    private void quitt(boolean r) {
        ret = r;
        backupInfo.runnerDto.stopRunner();
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        a.set(false);
    }
}