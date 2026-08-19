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


package de.p2tools.p2backup.controller.runner;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.tools.log.P2Log;

import java.io.File;

public class FileRunner {
    private boolean recur = false;
    private final BackupInfo backupInfo;

    public FileRunner(BackupInfo backupInfo) {
        this.backupInfo = backupInfo;
    }

    public int recDir(File dir, boolean recur) {
        this.recur = recur;
        return runDir(dir);
    }

    public void workFile(File file) {
    }

    public void workDir(File file) {
    }

    private int runDir(File dir) {
        int r = 0;
        try {
            File[] list;
            if (dir.isDirectory()) {
                workDir(dir);

                list = dir.listFiles();
                if (list == null) {
                    final String path = dir.getCanonicalPath();
                    if (backupInfo.runnerDto.isAsk()) {
                        if (!FileFactory.goOnError(backupInfo, path, false)) {
                            backupInfo.runnerDto.setStop();
                            return 0;
                        }
                    }

                } else {
                    for (File file : list) {
                        if (backupInfo.runnerDto.isStop()) {
                            return 0;
                        }
                        if (file.isFile()) {
                            ++r;
                            workFile(file);

                        } else if (file.isDirectory() && recur) {
                            r += runDir(file);
                        }
                    }
                }
            }
            if (dir.isFile()) {
                ++r;
                workFile(dir);
            }
        } catch (Exception ex) {
            P2Log.errorLog(945123697, ex, "Fehler im FileRunner!");
        }
        return r;
    }
}
