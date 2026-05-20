package de.p2tools.p2backup.controller.runner.hashrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2ToolsFactory;
import de.p2tools.p2lib.tools.log.P2Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateDataHash {
    // von den DATEN eine HashListe erstellen

    private CreateDataHash() {
    }

    public static void create(BackupInfo backupInfo,
                              FileDataList dirDataList,
                              FileDataList fileDataList,
                              boolean quick, boolean followLink,
                              AtomicBoolean atomicBoolean) {

        // von den eigenen Daten die HashListe erstellen, in den Listen dirDataList, fileDataList
        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start createDataHashList");

            // sind die Dirs des Backups die gesichert werden sollen
            List<File> fromPathList = new ArrayList<>();
            for (PathData p : backupInfo.getPathListFrom()) {
                fromPathList.add(p.getFilePathFile());
            }

            AtomicBoolean a = new AtomicBoolean(true);

            new DirCreateHash(backupInfo, fromPathList, dirDataList,
                    fileDataList, "",
                    quick, followLink, a).create();

            while (a.get()) {
                P2ToolsFactory.pause(500);
            }

            atomicBoolean.set(false);
        }).start();
    }
}
