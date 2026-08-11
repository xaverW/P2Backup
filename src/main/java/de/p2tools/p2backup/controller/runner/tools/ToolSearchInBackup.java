package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.tools.DialogSearchInBackup;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicBoolean;

public class ToolSearchInBackup {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private BooleanProperty stop = new SimpleBooleanProperty(false);
    private final DialogSearchInBackup dialogSearchInBackup;


    public ToolSearchInBackup(DialogSearchInBackup dialogSearchInBackup,
                              BackupInfo backupInfo, BackupData backupData, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.dialogSearchInBackup = dialogSearchInBackup;
        this.backupInfo = backupInfo;
        this.backupData = backupData;
        this.atomicBoolean = atomicBoolean;
    }

    public void setStop() {
        stop.set(true);
    }

    public void search() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start FileSearchHash: " + backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Suche Start");
            P2Log.sysLog("=======================================");

            work();

            backupInfo.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void work() {
        FileDataList fileDataList = new FileDataList();
        if (!SqlFileData.readBackupFileList(backupInfo, backupData, fileDataList)) {
            backupInfo.runnerDto.setStop();
        }
        dialogSearchInBackup.setResult(fileDataList);
        atomicBoolean.set(false);
    }
}
