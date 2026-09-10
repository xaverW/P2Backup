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
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.tools.DialogCompareBackupData;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCompareHashSql {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private final DialogCompareBackupData dialogCompareBackupData;
    private final String subPathBackup;

    public ToolCompareHashSql(DialogCompareBackupData dialogCompareBackupData,
                              BackupInfo backupInfo, BackupData backupData, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.dialogCompareBackupData = dialogCompareBackupData;
        this.backupInfo = backupInfo;
        this.backupData = backupData;
        this.subPathBackup = backupData.getSubPath();
        this.atomicBoolean = atomicBoolean;
    }

    public void compare() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start DirCompareHashSql: " + backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Vergleich Start");
            P2Log.sysLog("=======================================");

            compareDir();

            backupInfo.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void compareDir() {
        FileDataList fileListData = new FileDataList();
        FileDataList fileListBackup = new FileDataList();

        backupInfo.runnerDto.setRunnerText("Daten laden");
        if (!SqlFileData.readDataFileList(backupInfo, fileListData)) {
            backupInfo.runnerDto.setStop();
        }
        backupInfo.runnerDto.setRunnerText("Backup laden");
        if (!SqlFileData.readBackupFileList(backupInfo, backupData, fileListBackup)) {
            backupInfo.runnerDto.setStop();
        }

        FileDataList resultList = new FileDataList();
        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListData.clear();
            fileListBackup.clear();

        } else {
            // ==============
            // und jetzt mit dem Hash vergleichen
            fileListData.forEach(f -> f.setToPathStr(backupData.getToPathStr(backupInfo)));
            List<FileData> errorList = new ArrayList<>();
            CompareFactory.compare(dialogCompareBackupData.getStage(),
                    fileListData, fileListBackup, resultList, errorList);

            if (errorList.isEmpty()) {
                // dann nur eine kurze Meldung
                P2AlertAppThread.infoAlert(dialogCompareBackupData.getStage(),
                        "Vergleich", "Dateien und Backup sind identisch",
                        "Die Dateien im Backup sind identisch\n" +
                                "mit den Original-Dateien");
            }
        }

        dialogCompareBackupData.setResult(resultList);
        atomicBoolean.set(false);
    }
}

