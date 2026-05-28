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

public class DirCreateHash {
    // liefert FileData (mit Hash wenn nicht QUICK)
    // aus einer Liste vin DIRS
    // geliefert wird die FILE-LISTE: fileDataList
    // geliefert wird die DIR-LISTE: dirDataList

    private final List<File> fromPathList; // Dir-Liste, die durchsucht wird
    private final String toPath; // wird vom FileData entfernt
    private final FileDataList fileDataList; // liefert die gefundenen FILES
    private final FileDataList dirDataList; // liefert die gefundenen DIRs
    private final boolean quick;
    private final boolean followLink;
    private final AtomicBoolean atomicBoolean;

    private final ProgData progData;
    private final BackupInfo backupInfo; // nur für Info/STOP
    private final Set<File> foundFileList = new HashSet<>(); // nur intern; für die Liste der Dateien

    public DirCreateHash(BackupInfo backupInfo,
                         File fromPath,
                         FileDataList dirDataList,
                         FileDataList fileDataList,
                         String toPath,
                         boolean quick, boolean followLink,
                         AtomicBoolean atomicBoolean) {

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.fromPathList = Collections.singletonList(fromPath);
        this.dirDataList = dirDataList;
        this.fileDataList = fileDataList;
        this.toPath = toPath;
        this.quick = quick;
        this.followLink = followLink;
        this.atomicBoolean = atomicBoolean;
    }

    public DirCreateHash(BackupInfo backupInfo,
                         List<File> fromPathList,
                         FileDataList dirDataList,
                         FileDataList fileDataList,
                         String toPath,
                         boolean quick, boolean followLink,
                         AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.fromPathList = fromPathList;
        this.dirDataList = dirDataList;
        this.fileDataList = fileDataList;
        this.toPath = toPath;
        this.quick = quick;
        this.followLink = followLink;
        this.atomicBoolean = atomicBoolean;
    }

    public synchronized void create() {
        // Dateien suchen und dann FileData mit Hash (wenn nicht quick) anlegen
        P2Log.sysLog("Start DirCreateHash");
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));

        new Thread(() -> {
            try {
                final Set<File> foundDirList = new HashSet<>(); // nur intern: für die Liste der Dir
                // ====================
                // zuerst mal alle Dirs/Dateien im fromPath suchen
                FileListFactory.getFileList(backupInfo, fromPathList, foundDirList, foundFileList, null);

                backupInfo.runnerDto.getBackupData().setCount(foundFileList.size());
                backupInfo.runnerDto.setRunnerMax(foundFileList.size());
                backupInfo.runnerDto.setRunnerDone(0);
                backupInfo.runnerDto.setRunnerFileName("");

                // ============================
                // für jedes Verzeichnis im fromPath ein FileData-Object erstellen (und natürlich ohne Hash),
                // in DirDataList eintragen
                if (dirDataList != null) {
                    foundDirList.forEach(f -> {
                        FileData fileData = FileHashFactory.getFileDataHash(backupInfo,
                                toPath, true, f, followLink);
                        if (fileData != null) {
                            dirDataList.add(fileData);
                        }
                    });
                }

                foundDirList.clear();

                // Hash berechnen und FileData-Object erstellen, in fileDataList eintragen
                createFileHash(followLink);

                if (backupInfo.runnerDto.isStop()) {
                    fileDataList.clear();
                }
            } catch (Exception ex) {
                P2Log.errorLog(952145036, ex.getMessage());
            }

            atomicBoolean.set(false);
        }).start();
    }

    private void createFileHash(boolean followLink) {
        // FileData-Object für alle gefundenen Dateien erstellen
        P2Log.sysLog("Start createFileHash");
        int ready = 0;
        for (File file : foundFileList) {
            if (backupInfo.runnerDto.isStop()) {
                break;
            }

            backupInfo.runnerDto.setRunnerFileName(file.getName());
            // Pfad steht im dataPath
            FileData fileData = FileHashFactory.getFileDataHash(backupInfo,
                    toPath, quick, file, followLink);
            if (fileData != null) {
                fileData.setError(fileData.getHash().equals(FileFactory.HASH_ERROR));
                fileDataList.add(fileData);
            }

            ++ready;
            backupInfo.runnerDto.setRunnerDone(ready);
        }
    }
}
