/*
 * P2tools Copyright (C) 2018 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de/
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */


package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.DirCompleteCreateHash;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.tools.DialogCheckBackup;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCheckBackup {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private final DialogCheckBackup dialogCheckBackup;


    public ToolCheckBackup(DialogCheckBackup dialogCheckBackup,
                           BackupInfo backupInfo, BackupData backupData, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.dialogCheckBackup = dialogCheckBackup;
        this.backupInfo = backupInfo;
        this.backupData = backupData;
        this.atomicBoolean = atomicBoolean;
    }

    public void compare() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start CheckBackup: " + backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Prüfen Start");
            P2Log.sysLog("=======================================");

            compareDir();

            backupInfo.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void compareDir() {
        // Daten laden
        backupInfo.runnerDto.setRunnerText("Gespeicherte Backup-Dateien laden");
        FileDataList fileListDb = new FileDataList();
        if (!SqlFileData.readBackupFileList(backupInfo, backupData, fileListDb)) {
            backupInfo.runnerDto.setStop();
        }

        // Backup laden
        backupInfo.runnerDto.setRunnerText("Backup neu einlesen");
        String toPath = FileFactory.getToPathStr(backupInfo, backupData);
        FileDataList fileListBackup = getFileListBackup(toPath);
        FileFactory.cleanFileData(fileListBackup, toPath); // Pfade anpassen
        // /mnt/lager/p2BackupTest/ba1/2026-08-05__16-20-03/__mnt/lager/p2BackupTest/testDaten/1980er/1989/1989_011.jpg
        // -> /__mnt/lager/p2BackupTest/testDaten/1980er/1989/1989_011.jpg
        FileFactory.unSetCorrPath(fileListBackup); // Pfade anpassen
        // -> /mnt/lager/p2BackupTest/testDaten/1980er/1989/1989_011.jpg


        FileDataList resultList = new FileDataList();
        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListDb.clear();
            fileListBackup.clear();

        } else {
            // ==============
            // und jetzt den Hash vergleichen
            backupInfo.runnerDto.setRunnerText("Vergleichen");
            List<FileData> errorList = new ArrayList<>();
            CompareFactory.compare(dialogCheckBackup.getStage(),
                    fileListDb, fileListBackup, resultList, errorList);

            if (resultList.isEmpty()) {
                P2AlertAppThread.infoAlert(dialogCheckBackup.getStage(),
                        "Prüfen", "Backup ist OK",
                        "Im Backup befinden sich keine Dateien.");

            } else if (errorList.isEmpty()) {
                // dann nur eine kurze Meldung
                P2AlertAppThread.infoAlert(dialogCheckBackup.getStage(),
                        "Prüfen", "Backup ist OK",
                        "Das Backup ist unverändert. Es fehlt nichts " +
                                "oder ist verändert.");
            }
        }
        dialogCheckBackup.setResult(resultList);
        atomicBoolean.set(false);
    }

    private FileDataList getFileListBackup(String toPath) {
        FileDataList fileBackupList = new FileDataList();
        // dann ist ein Backup-Pfad
        if (toPath != null) {
            AtomicBoolean a = new AtomicBoolean(true);
            new DirCompleteCreateHash(backupInfo,
                    Path.of(toPath).toFile(),
                    fileBackupList,
                    toPath, // zum Eintragen in FileDate, brauch mer für Dateien die nur im Backup sind!
                    false, false,
                    a).create(true);
            while (a.get()) {
                P2Wait.pause(500);
            }
        }

        return fileBackupList;
    }
}

