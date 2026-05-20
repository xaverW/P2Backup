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
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.DirCreateHash;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.tools.CheckBackupDialogController;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2ToolsFactory;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCheckBackup {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private final CheckBackupDialogController checkBackupDialogController;


    public ToolCheckBackup(CheckBackupDialogController checkBackupDialogController,
                           BackupInfo backupInfo, BackupData backupData, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.checkBackupDialogController = checkBackupDialogController;
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
        FileDataList fileListDb = new FileDataList();
        if (SqlFileData.readFileListFromBackup(backupInfo, backupData, fileListDb)) {
//            // damit das Vergleichen klappt:
//            fileListDb.forEach(f -> f.setFilePathStr(f.getBackupFilePathStr()));
        } else {
            backupInfo.runnerDto.setStop();
        }

        // Backup laden
        FileDataList fileListBackup = getFileListBackup(backupData.getSubPath());
        // damit das Vergleichen mit den Daten klappt:
        fileListBackup.forEach(f -> f.setFilePathStr(FileFactory.unSetCorrPath(f.getFilePathStr())));

        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListDb.clear();
            fileListBackup.clear();

        } else {
            // ==============
            // und jetzt den Hash vergleichen
            FileDataList resultList = new FileDataList();
            CompareFactory.compare(checkBackupDialogController.getStage(),
                    fileListDb, fileListBackup, resultList, false);
            checkBackupDialogController.setResult(resultList);
        }

        atomicBoolean.set(false);
    }

    private FileDataList getFileListBackup(String subPath) {
        FileDataList fileDbList = new FileDataList();
        // dann ist ein Backup-Pfad
        Path toPath = FileFactory.getToPath(backupInfo, subPath);
        if (toPath != null) {
            AtomicBoolean a = new AtomicBoolean(true);
            new DirCreateHash(backupInfo,
                    toPath.toFile(),
                    null, fileDbList,
                    toPath.toString(), // damit der PATH korrigiert wird
                    false, false,
                    a).create();
            while (a.get()) {
                P2ToolsFactory.pause(500);
            }
        }

        return fileDbList;
    }
}

