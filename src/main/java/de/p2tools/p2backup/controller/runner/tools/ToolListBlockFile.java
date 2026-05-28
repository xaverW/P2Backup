package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.runner.hashrunner.FileListFactory;
import de.p2tools.p2backup.gui.tools.BlockedFilesDialogController;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolListBlockFile {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final AtomicBoolean atomicBoolean;
    private BooleanProperty stop = new SimpleBooleanProperty(false);
    private final BlockedFilesDialogController blockedFilesDialogController;
    private final Set<File> foundFileList;
    private final Set<File> blockedFileList;


    public ToolListBlockFile(BlockedFilesDialogController blockedFilesDialogController,
                             BackupInfo backupInfo,
                             Set<File> foundFileList, Set<File> blockedFileList, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.blockedFilesDialogController = blockedFilesDialogController;
        this.backupInfo = backupInfo;
        this.foundFileList = foundFileList;
        this.blockedFileList = blockedFileList;
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
        FileListFactory.getFileList(backupInfo, null, foundFileList, blockedFileList);
        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            foundFileList.clear();
            blockedFileList.clear();
        }

        blockedFilesDialogController.setResult();
        atomicBoolean.set(false);
    }
}
