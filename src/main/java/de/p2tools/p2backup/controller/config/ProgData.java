/*
 * MTViewer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de
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


package de.p2tools.p2backup.controller.config;

import de.p2tools.p2backup.controller.data.BackupPathList;
import de.p2tools.p2backup.controller.data.MTShortcut;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfoList;
import de.p2tools.p2backup.controller.worker.ColorWorker;
import de.p2tools.p2backup.controller.worker.Worker;
import de.p2tools.p2backup.gui.dialog.QuitDialogController;
import de.p2tools.p2backup.gui.guibig.BackupBigGui;
import de.p2tools.p2backup.gui.guismall.BackupSmallGui;
import de.p2tools.p2lib.css.P2CssFactory;
import de.p2tools.p2lib.guitools.pmask.P2MaskerPane;
import de.p2tools.p2lib.p2event.P2EventHandler;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;

public class ProgData {
    private static ProgData instance;

    // flags
    public static boolean debug = false; // Debugmodus
    public static boolean duration = false; // Duration ausgeben
    public static boolean reset = false; // Programm auf Starteinstellungen zurücksetzen
    public static boolean raspberry = false; // läuft auf einem Raspberry
    public static boolean firstProgramStart = false; // ist der allererste Programmstart: Init wird gemacht
    public static boolean startSmall = false; // Minimiert starten
    public P2EventHandler pEventHandler;
    public ObjectProperty<P2CssFactory.CSS> cssProp = new SimpleObjectProperty<>(P2CssFactory.CSS.CSS_1);

    public static String configDir = ""; // Verzeichnis zum Speichern der Programmeinstellungen
    public MTShortcut mtShortcut; // verwendete Shortcuts

    // gui
    public Stage primaryStage = null;
    public Stage primaryStageSmall = null;
    public BackupBigGui backupBigGui = null;
    public BackupSmallGui backupSmallGui = null;
    public ColorWorker colorWorker = null;

    public P2MaskerPane maskerPane = new P2MaskerPane();
    public QuitDialogController quitDialogController = null;

    // data
//    public DbDataList dbDataList;
    public BackupInfoList backupInfoList;
    public BackupPathList usedToPathList; // ist die Liste der bereits verwendeten ToPaths, zur Auswahl
    public IntegerProperty programState = new SimpleIntegerProperty(ProgConst.PROGRAM_STATE_BACKUP);
    public final ObjectProperty<BackupInfo> backupInfoProperty = new SimpleObjectProperty<>(null);
    public final boolean WINDOWS;

    // worker
    public Worker worker;

    private ProgData() {
        pEventHandler = new P2EventHandler(false);
        mtShortcut = new MTShortcut();
//        dbDataList = new DbDataList();
        backupInfoList = new BackupInfoList();
        usedToPathList = new BackupPathList();
        worker = new Worker(this);
        WINDOWS = SystemUtils.IS_OS_WINDOWS;
        colorWorker = new ColorWorker(this);
        backupInfoProperty.addListener((u, o, n) -> {
            System.out.println("BA-INFO");
        });
        backupInfoList.addListener((u, o, n) -> {
            System.out.println("BA_INFO_LIST");
        });
    }

    public synchronized static ProgData getInstance(String dir) {
        if (!dir.isEmpty()) {
            configDir = dir;
        }
        return getInstance();
    }

    public synchronized static ProgData getInstance() {
        return instance == null ? instance = new ProgData() : instance;
    }
}
