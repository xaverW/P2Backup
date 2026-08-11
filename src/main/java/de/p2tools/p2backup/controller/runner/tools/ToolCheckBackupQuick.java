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
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.DirCreateHash;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCheckBackupQuick {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;


    public ToolCheckBackupQuick(BackupInfo backupInfo, BackupData backupData,
                                AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.backupData = backupData;
        this.atomicBoolean = atomicBoolean;
    }

    public void compare(FileDataList errorList) {
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start CheckBackup QUICK: " + backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Prüfen Start");
            P2Log.sysLog("=======================================");

            compareDir(errorList);

            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void compareDir(FileDataList errorList) {
        // Daten laden
        FileDataList fileListDb = new FileDataList();
        if (!SqlFileData.readBackupFileList(backupInfo, backupData, fileListDb)) {
            backupInfo.runnerDto.setStop();
        }

        // Backup laden
        String toPath = FileFactory.getToPathStr(backupInfo, backupData);
        FileDataList fileListBackup = getFileListBackup(toPath);
        FileFactory.cleanFileData(fileListBackup, toPath); // Pfade anpassen
        FileFactory.unSetCorrPath(fileListBackup); // Pfade anpassen

        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListDb.clear();
            fileListBackup.clear();

        } else {
            // ==============
            CompareFactory.compareQuick(backupInfo, fileListDb, fileListBackup, new ArrayList<>(), errorList);
            // und jetzt noch die, die "zuviel im Backup sind, löschen, sind keine BackupFehler
            errorList.removeIf(FileDataProps::isOnlyInBackup);
        }
        atomicBoolean.set(false);
    }

    private FileDataList getFileListBackup(String toPath) {
        FileDataList fileDbList = new FileDataList();
        // dann ist ein Backup-Pfad
        if (toPath != null) {
            AtomicBoolean a = new AtomicBoolean(true);
            new DirCreateHash(backupInfo,
                    Path.of(toPath).toFile(),
                    null, fileDbList,
                    "", // zum Eintragen in FileDate, brauchmer aber nicht
                    true, false,
                    a).create(false);
            while (a.get()) {
                P2Wait.pause(500);
            }
        }

        return fileDbList;
    }
}

