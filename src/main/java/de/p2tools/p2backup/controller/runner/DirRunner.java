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

import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.application.Platform;

import java.io.File;

public class DirRunner {
    private boolean stop = false;
    private boolean recur = false;
    private boolean altert = false; // nur einmal Fehler melden

    public int recDir(File dir, boolean recur) {
        this.recur = recur;
        return runDir(dir);
    }

    public void setStop() {
        stop = true;
    }

    public void work(File file) {
    }

    private int runDir(File dir) {
        int r = 0;
        try {
            if (stop) {
                return 0;
            }

            File[] list;
            if (dir.isDirectory()) {
                list = dir.listFiles();
                if (list == null) {
                    final String path = dir.getCanonicalPath();
                    if (!altert) {
                        altert = true;
                        Platform.runLater(() ->
                                P2AlertAppThread.showErrorAlert("Dateien lesen",
                                        "Kann Dateien des Ordners \"" + path + "\" nicht lesen", "Fehler!"));
                    }

                } else {
                    for (File file : list) {
                        if (file.isFile()) {
                            ++r;
                            work(file);

                        } else if (file.isDirectory() && recur) {
                            r += runDir(file);
                        }
                    }
                }
            }
            if (dir.isFile()) {
                ++r;
                work(dir);
            }
        } catch (Exception ex) {
            P2Log.errorLog(945123697, ex, "Fehler im FileRunner!");
        }
        return r;
    }
}
