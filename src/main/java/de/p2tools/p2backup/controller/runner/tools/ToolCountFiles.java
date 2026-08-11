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
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.runner.FileRunner;
import de.p2tools.p2backup.controller.runner.hashrunner.FileListFactory;
import de.p2tools.p2backup.gui.tools.DialogBackupInfo;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.io.File;

public class ToolCountFiles {

    private ProgData progData;
    private BooleanProperty stop = new SimpleBooleanProperty(false);
    private final StringProperty searchDir = new SimpleStringProperty();
    private final BooleanProperty recursive = new SimpleBooleanProperty();
    private final BackupInfo backupInfo;
    private final boolean noBlocked;

    private final DialogBackupInfo backupInfoDialogController;

    public ToolCountFiles(ProgData progData, DialogBackupInfo backupInfoDialogController,
                          BackupInfo backupInfo, boolean noBlocked, boolean recursive) {
        this.progData = progData;
        this.backupInfoDialogController = backupInfoDialogController;
        this.backupInfo = backupInfo;
        this.noBlocked = noBlocked;
        this.recursive.set(recursive);
        backupInfo.clear();
    }

    public void setStop() {
        stop.set(true);
    }

    public void count() {
        backupInfo.runnerDto.startRunner(backupInfo.getName());
        P2Log.sysLog("Start count: " + searchDir.getValueSafe());

        // zuerst mal die Zähler zurücksetzen
        backupInfo.setSize(0);
        backupInfo.setCount(0);
        for (PathData pathData : backupInfo.getPathListFrom()) {
            pathData.setCount(0);
            pathData.setSize(0);
        }
        for (BackupData backupData : backupInfo.getBackupDataList()) {
            backupData.setSize(0);
            backupData.setCount(0);
        }

        CountFile countFile = new CountFile(backupInfo, recursive.getValue());
        Thread startenThread = new Thread(countFile);
        startenThread.setName("CountFile");
        startenThread.setDaemon(true);
        startenThread.start();
    }

    private class CountFile implements Runnable {
        final BackupInfo backupInfo;
        private final boolean recursive;
        int countAll = 0;


        public CountFile(BackupInfo backupInfo, boolean recursive) {
            this.backupInfo = backupInfo;
            this.recursive = recursive;
        }

        public synchronized void run() {
            try {
                for (PathData p : backupInfo.getPathListFrom()) {
                    int n = runDirFindFilesFrom(p);
                    p.setCount(n);
                }
            } catch (Exception ex) {
                P2Log.errorLog(952145036, ex.getMessage());
            }
            try {
                for (BackupData p : backupInfo.getBackupDataList()) {
                    int n = runDirFindFilesBackup(p);
                    p.setCount(n);
                }
            } catch (Exception ex) {
                P2Log.errorLog(989564789, ex.getMessage());
            }

            IntegerProperty count = new SimpleIntegerProperty(0);
            LongProperty size = new SimpleLongProperty(0);

            // FROM-Pfad die Gesamt-Summe eintragen
            backupInfo.getPathListFrom().forEach(pathData -> count.set(count.get() + pathData.getCount()));
            backupInfo.setCount(count.get());
            backupInfo.getPathListFrom().forEach(p -> size.set(size.get() + p.getSize()));
            backupInfo.setSize(size.get());

            ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
            Platform.runLater(backupInfoDialogController::set);
            backupInfo.runnerDto.stopRunner();
        }

        private int runDirFindFilesFrom(PathData pathData) {
            //Verzeichnis ablaufen und Dateien zählen
            File path = new File(pathData.getPath());
            countAll = 0;

            try {
                new FileRunner() {
                    @Override
                    public void workFile(File file) {
                        if (file.exists()) {
                            if (noBlocked &&
                                    !FileListFactory.checkFile(file, backupInfo)) {
                                return;
                            }
                            ++countAll;

                            // File size in bytes
                            long bytes = file.length();
                            pathData.setSize(pathData.getSize() + bytes);
                        } else {
                            System.out.println("File not found.");
                        }
                    }
                }.recDir(path, recursive);

                return countAll;
            } catch (Exception ex) {
                P2Log.errorLog(854541257, ex, "CreateHash.run - " + path.getAbsolutePath());
            }
            return 0;
        }

        private int runDirFindFilesBackup(BackupData backupData) {
            //Verzeichnis ablaufen und Dateien zählen
            File path = backupData.getToPath(backupInfo).toFile();
            countAll = 0;

            try {
                new FileRunner() {
                    @Override
                    public void workFile(File file) {
                        if (file.exists()) {
                            ++countAll;

                            // File size in bytes
                            long bytes = file.length();
                            backupData.setSize(backupData.getSize() + bytes);
                        } else {
                            System.out.println("File not found.");
                        }
                    }
                }.recDir(path, recursive);

                return countAll;
            } catch (Exception ex) {
                P2Log.errorLog(854541257, ex, "CreateHash.run - " + path.getAbsolutePath());
            }
            return 0;
        }
    }
}
