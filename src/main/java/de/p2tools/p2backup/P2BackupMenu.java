/*
 * P2tools Copyright (C) 2022 W. Xaver W.Xaver[at]googlemail.com
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


package de.p2tools.p2backup;


import de.p2tools.p2backup.controller.ProgQuit;
import de.p2tools.p2backup.controller.config.PShortcut;
import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.update.SearchProgramUpdate;
import de.p2tools.p2backup.gui.configdialog.ConfigDialogController;
import de.p2tools.p2backup.gui.dialog.DialogAbout;
import de.p2tools.p2backup.gui.dialog.DialogResetConfig;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import de.p2tools.p2lib.ikonli.P2IconShow;
import de.p2tools.p2lib.tools.log.P2Logger;
import de.p2tools.p2lib.tools.shortcut.P2ShortcutWorker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;

import java.util.Arrays;

public class P2BackupMenu extends MenuButton {
    public P2BackupMenu() {
        makeButton();
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() > 1) {
                    ProgConfig.SYSTEM_GUI_THEME_1.set(!ProgConfig.SYSTEM_GUI_THEME_1.get());
                }
            }

            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                ProgConfig.SYSTEM_DARK_THEME.set(!ProgConfig.SYSTEM_DARK_THEME.get());
            }
        });
    }

    private void makeButton() {
        ProgData progData = ProgData.getInstance();

        // Menü
        final MenuItem miConfig = new MenuItem("Einstellungen des Programms");
        miConfig.setOnAction(e -> new ConfigDialogController(ProgData.getInstance()));

        final CheckMenuItem chkEnhanced = new CheckMenuItem("Erweiterte Einstellungen");
        chkEnhanced.setOnAction(a -> {
            if (chkEnhanced.isSelected()) {
                // einschalten
                ProgConfig.SYSTEM_ENHANCED.set(true);

            } else {
                // ausschalten geht nur, wenn noch nicht benutzte
                BooleanProperty found = new SimpleBooleanProperty(false);
                progData.backupInfoList.forEach(bi -> {
                    if (bi.getHow() == ProgConst.BACKUP_INTELLIGENT) {
                        found.set(true);
                    }
                    if (!bi.getPathListExcludeDir().isEmpty()) {
                        found.set(true);
                    }
                    if (!bi.getPathListExcludeFile().isEmpty()) {
                        found.set(true);
                    }
                });
                if (found.get()) {
                    P2Alert.showErrorAlert("Erweiterte Einstellungen",
                            "Die Erweiterten Einstellungen können nur abgeschaltet werden, " +
                                    "wenn sie noch nicht benutzt werden.\n\n" +
                                    "\"Intelligentes Kopieren\" oder\n" +
                                    "\"Ausschließen von Ordnern/Dateien von den " +
                                    "Ordnern die gesichert werden sollen.");
                    chkEnhanced.setSelected(true);
                } else {
                    ProgConfig.SYSTEM_ENHANCED.set(false);
                }
            }
        });

        final CheckMenuItem miDarkMode = new CheckMenuItem("Dunkle Oberfläche");
        miDarkMode.selectedProperty().bindBidirectional(ProgConfig.SYSTEM_DARK_THEME);

        final CheckMenuItem miColorMode = new CheckMenuItem("Farb-Modus-1");
        miColorMode.selectedProperty().bindBidirectional(ProgConfig.SYSTEM_GUI_THEME_1);

        final MenuItem miQuit = new MenuItem("Beenden");
        miQuit.setOnAction(e -> ProgQuit.quit());
        P2ShortcutWorker.addShortCut(miQuit, PShortcut.SHORTCUT_QUIT_PROGRAM);

        setTooltip(new Tooltip("Programmeinstellungen anzeigen"));
        setMinWidth(Region.USE_PREF_SIZE);
        getStyleClass().addAll("p2BackupMenu");
        setText("");
        setGraphic(P2IconFactory.P2ICON.P2__PROG_MENU.getFontIcon());

        getItems().addAll(miConfig, miDarkMode, miColorMode);
        getItems().addAll(addHelp(progData), new SeparatorMenuItem(), miQuit);
        if (ProgData.debug) {

            final MenuItem miIcon = new MenuItem("Icon");
            miIcon.setOnAction(e -> new P2IconShow(Arrays.asList(PIconFactory.PICON.values())));
            getItems().addAll(new SeparatorMenuItem(), new SeparatorMenuItem(), miIcon, chkEnhanced);
        }
    }

    private Menu addHelp(ProgData progData) {
//        final MenuItem miUrlHelp = new MenuItem("Anleitung im Web");
//        miUrlHelp.setOnAction(event -> {
//            P2Open.openURL(ProgConst.URL_WEBSITE_HELP,
//                    ProgConfig.SYSTEM_PROG_OPEN_URL, PIconFactory.PICON.BTN_FILE_OPEN.getFontIcon());
//        });
        final MenuItem miLog = new MenuItem("Logdatei öffnen");
        miLog.setOnAction(event -> {
            P2Logger.openLogFile();
        });
        final MenuItem miReset = new MenuItem("Einstellungen zurücksetzen");
        miReset.setOnAction(event -> new DialogResetConfig(progData));
        final MenuItem miSearchUpdate = new MenuItem("Gibt's ein Update?");
        miSearchUpdate.setOnAction(a -> new SearchProgramUpdate(progData).searchNewProgramVersion(true));
        final MenuItem miAbout = new MenuItem("Über dieses Programm");
        miAbout.setOnAction(event -> new DialogAbout(progData).showDialog());

        final Menu mHelp = new Menu("Hilfe");
        mHelp.getItems().addAll(miLog, miReset,
                new SeparatorMenuItem(), miSearchUpdate, miAbout);

        return mHelp;
    }
}
