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
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.CreateDataHash;
import de.p2tools.p2backup.controller.runner.hashrunner.DirCreateHash;
import de.p2tools.p2backup.gui.tools.CompareBackupDialogController;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2ToolsFactory;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCompareHash {

    private ProgData progData;
    private final BackupInfo backupInfos;
    private final AtomicBoolean atomicBoolean;
    private final CompareBackupDialogController compareBackupDialogController;
    private final String subPathBackup;


    public ToolCompareHash(CompareBackupDialogController compareBackupDialogController,
                           String subPathBackup,
                           BackupInfo backupInfos, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.compareBackupDialogController = compareBackupDialogController;
        this.subPathBackup = subPathBackup;
        this.backupInfos = backupInfos;
        this.atomicBoolean = atomicBoolean;
    }

    public void compare() {
        backupInfos.runnerDto.startRunner(backupInfos.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start DirCompareHash: " + backupInfos.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Vergleich Start");
            P2Log.sysLog("=======================================");

            compareDir();

            backupInfos.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void compareDir() {
        FileDataList fileListData = getFileDataList("");
        FileDataList fileListBackup = getFileDataList(subPathBackup);

        if (backupInfos.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListData.clear();
            fileListBackup.clear();

        } else {
            // ==============
            // und jetzt mit dem Hash vergleichen
            FileDataList resultList = new FileDataList();
            CompareFactory.compare(compareBackupDialogController.getStage(),
                    fileListData, fileListBackup, resultList, true);
            compareBackupDialogController.setResult(resultList);
        }

        atomicBoolean.set(false);
    }

    private FileDataList getFileDataList(String subPath) {
        FileDataList fileDataList = new FileDataList();
        if (subPath.isEmpty()) {
            // dann ist es der Pfad der DATEN
            AtomicBoolean a = new AtomicBoolean(true);
            CreateDataHash.create(backupInfos, null, fileDataList,
                    false, false, a);
            while (a.get()) {
                P2ToolsFactory.pause(500);
            }

        } else {
            // dann ist ein Backup-Pfad
            Path toPath = FileFactory.getToPath(backupInfos, subPath);
            if (toPath != null) {
                AtomicBoolean a = new AtomicBoolean(true);
                new DirCreateHash(backupInfos,
                        toPath.toFile(),
                        null, fileDataList,
                        toPath.toString(), // damit der PATH korrigiert wird
                        false, false,
                        a).create();
                while (a.get()) {
                    P2ToolsFactory.pause(500);
                }
            }
        }

        return fileDataList;
    }
}

