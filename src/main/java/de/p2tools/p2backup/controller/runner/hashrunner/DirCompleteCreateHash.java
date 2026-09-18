package de.p2tools.p2backup.controller.runner.hashrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirCompleteCreateHash {
    // liefert FileData (mit Hash wenn nicht QUICK)
    // aus einer Liste vin DIRS
    // geliefert wird die FILE-LISTE: fileDataList
    // geliefert wird die DIR-LISTE: dirDataList

    private final List<File> fromPathList; // Dir-Liste, die durchsucht wird
    private final String toPath; // wird vom FileData entfernt
    private final FileDataList fileDataList; // liefert die gefundenen FILES
    private final boolean quick;
    private final boolean followLink;
    private final AtomicBoolean atomicBoolean;

    private final ProgData progData;
    private final BackupInfo backupInfo; // nur für Info/STOP
    private final Set<File> foundFileList = new HashSet<>(); // nur intern; für die Liste der Dateien

    public DirCompleteCreateHash(BackupInfo backupInfo,
                                 File fromPath,
                                 FileDataList fileDataList,
                                 String toPath,
                                 boolean quick, boolean followLink,
                                 AtomicBoolean atomicBoolean) {

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.fromPathList = Collections.singletonList(fromPath);
        this.fileDataList = fileDataList;
        this.toPath = toPath;
        this.quick = quick;
        this.followLink = followLink;
        this.atomicBoolean = atomicBoolean;
    }

    public synchronized void create(boolean countRunner) {
        // Dateien suchen und dann FileData mit Hash (wenn nicht quick) anlegen
        P2Log.sysLog("Start DirCreateHash");
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        new Thread(() -> {
            try {
                // ====================
                // zuerst mal alle Dirs/Dateien im fromPath suchen
                FileListFactory.getCompleteFileList(backupInfo, fromPathList, foundFileList);

                // Hash berechnen und FileData-Object erstellen, in fileDataList eintragen
                createFileHash(followLink, countRunner);

                if (backupInfo.runnerDto.isStop()) {
                    fileDataList.clear();
                }
            } catch (Exception ex) {
                P2Log.errorLog(952145036, ex.getMessage());
            }

            atomicBoolean.set(false);
        }).start();
    }

    private void createFileHash(boolean followLink, boolean countRunner) {
        // FileData-Object für alle gefundenen Dateien erstellen
        P2Log.sysLog("Start createFileHash");
        if (countRunner) {
            backupInfo.runnerDto.setRunnerMax(foundFileList.size());
        }
        for (File file : foundFileList) {
            if (backupInfo.runnerDto.isStop()) {
                break;
            }

            // Pfad steht im dataPath
            FileData fileData = HashFactory.getFileData(backupInfo,
                    toPath, quick, file, followLink);
            if (fileData != null) {
                fileData.setErrorHash(fileData.getHash().equals(FileFactory.HASH_ERROR));
                fileDataList.add(fileData);
            }

            if (countRunner) {
                backupInfo.runnerDto.setRunnerFileName(file.getName());
                backupInfo.runnerDto.addRunnerAlreadyDone();
            }
        }
        if (countRunner) {
            backupInfo.runnerDto.setRunnerMax(0);
        }
    }
}
