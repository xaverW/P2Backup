package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.tools.SearchInBackupDialogController;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicBoolean;

public class ToolSearchInBackup {

    private ProgData progData;
    private final BackupInfo backupInfos;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private BooleanProperty stop = new SimpleBooleanProperty(false);
    private final SearchInBackupDialogController searchInBackupDialogController;


    public ToolSearchInBackup(SearchInBackupDialogController searchInBackupDialogController,
                              BackupInfo backupInfos, BackupData backupData, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.searchInBackupDialogController = searchInBackupDialogController;
        this.backupInfos = backupInfos;
        this.backupData = backupData;
        this.atomicBoolean = atomicBoolean;
    }

    public void setStop() {
        stop.set(true);
    }

    public void search() {
        backupInfos.runnerDto.startRunner(backupInfos.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start FileSearchHash: " + backupInfos.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Suche Start");
            P2Log.sysLog("=======================================");

            work();

            backupInfos.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void work() {
        FileDataList fileDataList = new FileDataList();
        if (!SqlFileData.readFileListFromBackup(backupInfos, backupData, fileDataList)) {
            backupInfos.runnerDto.setStop();
        }
        searchInBackupDialogController.setResult(fileDataList);
        atomicBoolean.set(false);
    }
}
