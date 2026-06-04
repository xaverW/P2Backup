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

package de.p2tools.p2backup.controller;

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfoFactory;
import de.p2tools.p2backup.controller.data.dbdata.DbData;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.sqlite.SqlBackupInfo;
import de.p2tools.p2backup.controller.sqlite.SqlTable;
import de.p2tools.p2backup.gui.dialog.QuitDialogController;
import de.p2tools.p2lib.guitools.P2GuiSize;
import de.p2tools.p2lib.tools.log.P2LogMessage;
import javafx.application.Platform;

import java.nio.file.Path;

public class ProgQuit {

    private ProgQuit() {
    }

    /**
     * Quit the MTViewer application
     */
    public static void quit() {

        if (BackupInfoFactory.isRunning()) {
            new QuitDialogController();

        } else {
            quitNow();
        }
    }

    public static void quitNow() {
        if (ProgData.getInstance().backupBigGui != null &&
                ProgData.getInstance().primaryStage.isShowing()) {
            P2GuiSize.getSize(ProgConfig.SYSTEM_SIZE_BIG_GUI, ProgData.getInstance().primaryStage);
        }

        if (ProgData.getInstance().backupSmallGui != null &&
                ProgData.getInstance().primaryStageSmall.isShowing()) {
            ProgData.getInstance().backupSmallGui.close();
        }

        saveConfig();
        exitProg();
    }

    private static void saveConfig() {
        writeTabSettings();
        saveDb();
        ProgSave.saveAll();
        P2LogMessage.endMsg();
    }

    private static void saveDb() {
        ProgData.getInstance().dbDataList.clear();
        ProgData.getInstance().backupInfoList.forEach(backupInfo -> {
            DbData dbData = new DbData(backupInfo);
            ProgData.getInstance().dbDataList.add(dbData);
            if (!dbData.getPath().isEmpty() &&
                    Path.of(dbData.getPath()).toFile().exists()) {

                if (!Path.of(FileFactory.getBackupDbPath(backupInfo)).toFile().exists()) {
                    // dann ist das Backup noch nicht gelaufen, DB existiert noch nicht -> anlegen
                    SqlTable.makeBackupDb(backupInfo);
                }
                SqlBackupInfo.addUpdateBackupInfo(backupInfo);
            }
        });
    }

    private static void exitProg() {
        // dann jetzt beenden -> Tschüss
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private static void writeTabSettings() {
        // Tabelleneinstellungen merken
        final ProgData progData = ProgData.getInstance();
    }
}
