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
import de.p2tools.p2backup.controller.runner.hashrunner.CreateDataHash;
import de.p2tools.p2backup.controller.runner.hashrunner.DirCreateHash;
import de.p2tools.p2backup.gui.tools.DialogCompareBackupData;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolCompareBackupData {

    private ProgData progData;
    private final BackupInfo backupInfo;
    private final BackupData backupData;
    private final AtomicBoolean atomicBoolean;
    private final DialogCompareBackupData dialogCompareBackupData;
    private final String toPath;
    private final boolean quick;


    public ToolCompareBackupData(DialogCompareBackupData dialogCompareBackupData,
                                 BackupInfo backupInfo, BackupData backupData, boolean quick, AtomicBoolean atomicBoolean) {
        this.progData = ProgData.getInstance();
        this.dialogCompareBackupData = dialogCompareBackupData;
        this.backupData = backupData;
        this.quick = quick;
        this.toPath = FileFactory.getToPathStr(backupInfo, backupData);
        this.backupInfo = backupInfo;
        this.atomicBoolean = atomicBoolean;
    }

    public void compare() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        new Thread(() -> {
            P2Log.sysLog("Start DirCompareHash: " + backupInfo.getName());
            P2Log.sysLog("=======================================");
            P2Log.sysLog("   Backup-Vergleich Start");
            P2Log.sysLog("=======================================");

            compareDir();

            backupInfo.runnerDto.stopRunner();
            progData.pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();
    }

    private void compareDir() {
        backupInfo.runnerDto.setRunnerText("Daten laden");
        FileDataList fileListData = getFileDataList();

        backupInfo.runnerDto.setRunnerText("Backup laden");
        FileDataList fileListBackup = getFileBackupList(toPath);

        FileFactory.cleanFileData(fileListBackup, toPath); // Pfade anpassen
        FileFactory.unSetCorrPath(fileListBackup); // Pfade anpassen

        FileDataList resultList = new FileDataList();
        if (backupInfo.runnerDto.isStop()) {
            // wenn abgebrochen, löschen
            fileListData.clear();
            fileListBackup.clear();

        } else if (fileListBackup.isEmpty()) {
            // wenn Backup leer ist
            P2AlertAppThread.infoAlert(dialogCompareBackupData.getStage(), "Vergleichen",
                    "Backup mit den Daten vergleichen",
                    "Das Backup ist leer, es enthält keine Dateien.");
            resultList.setAll(fileListData);
            resultList.forEach(f -> f.setExistInData(true));

        } else {
            // ==============
            // und jetzt mit dem Hash vergleichen
            fileListData.forEach(f -> f.setToPathStr(backupData.getToPathStr(backupInfo)));
            if (quick) {
                CompareFactory.compareQuick(backupInfo, fileListData, fileListBackup, resultList, new ArrayList<>());
            } else {
                List<FileData> list = new ArrayList<>();
                CompareFactory.compare(dialogCompareBackupData.getStage(),
                        fileListData, fileListBackup, resultList, list);

                if (list.isEmpty()) {
                    // dann nur eine kurze Meldung
                    P2AlertAppThread.infoAlert(dialogCompareBackupData.getStage(),
                            "Vergleich", "Dateien und Backup sind identisch",
                            "Die Dateien im Backup sind identisch\n" +
                                    "mit den Original-Dateien");
                }
            }
        }

        dialogCompareBackupData.setResult(resultList);
        atomicBoolean.set(false);
    }

    private FileDataList getFileDataList() {
        FileDataList fileDataList = new FileDataList();
        // dann ist es der Pfad der DATEN
        AtomicBoolean a = new AtomicBoolean(true);
        CreateDataHash.create(backupInfo, null, fileDataList,
                quick, false, a);
        while (a.get()) {
            P2Wait.pause(500);
        }
        return fileDataList;
    }

    private FileDataList getFileBackupList(String toPath) {
        FileDataList fileDataList = new FileDataList();
        // dann ist ein Backup-Pfad
        AtomicBoolean a = new AtomicBoolean(true);
        new DirCreateHash(backupInfo,
                Path.of(toPath).toFile(),
                null, fileDataList,
                "",
                quick, false,
                a).create();
        while (a.get()) {
            P2Wait.pause(500);
        }
        return fileDataList;
    }
}

