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
package de.p2tools.p2backup;

import de.p2tools.p2backup.controller.ProgQuit;
import de.p2tools.p2backup.controller.ProgStartAfterGui;
import de.p2tools.p2backup.controller.ProgStartBeforeGui;
import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.gui.guibig.BackupBigGui;
import de.p2tools.p2backup.gui.guismall.BackupSmallGui;
import de.p2tools.p2lib.css.P2CssFactory;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiSize;
import de.p2tools.p2lib.tools.P2InfoFactory;
import de.p2tools.p2lib.tools.duration.P2Duration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class P2Backup extends Application {

    private static final String LOG_TEXT_PROGRAMSTART = "Dauer Programmstart";
    protected ProgData progData;
    private boolean smallDone = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage primaryStage) {
        P2Duration.counterStart(LOG_TEXT_PROGRAMSTART);

        progData = ProgData.getInstance();
        progData.primaryStage = primaryStage;

        ProgStartBeforeGui.workBeforeGui();
        initRootLayout();
        ProgStartAfterGui.doWorkAfterGui();

        P2Duration.onlyPing("Gui steht!");
        P2Duration.counterStop(LOG_TEXT_PROGRAMSTART);
    }

    private void initRootLayout() {
        try {
            ProgConfig.SYSTEM_SMALL_BACKUP.addListener((u, o, n) ->
                    selectGui());

            initBigLayout();
            selectGui();

            if (ProgData.startSmall) {
                progData.primaryStage.setIconified(true);
                P2DialogExtra.getDialogList().forEach(d -> d.getStage().setIconified(true));
            }


            if (ProgData.firstProgramStart) {
                // dann gabs den Startdialog
                ProgConfig.SYSTEM_DARK_THEME.set(ProgConfig.SYSTEM_DARK_START.get());
                ProgConfig.SYSTEM_GUI_THEME_1.set(ProgConfig.SYSTEM_GUI_THEME_1_START.get());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void initBigLayout() {
        try {
            progData.backupBigGui = new BackupBigGui();
            Scene scene = new Scene(progData.backupBigGui,
                    P2GuiSize.getSceneSize(ProgConfig.SYSTEM_SIZE_BIG_GUI, true),
                    P2GuiSize.getSceneSize(ProgConfig.SYSTEM_SIZE_BIG_GUI, false));

            progData.primaryStage.setScene(scene);
            progData.primaryStage.setOnCloseRequest(e -> {
                //beim Beenden
                e.consume();
                ProgQuit.quit();
            });

            //Pos setzen
            progData.primaryStage.setOnShowing(e -> P2GuiSize.setSizePos(ProgConfig.SYSTEM_SIZE_BIG_GUI, progData.primaryStage));
            progData.primaryStage.setOnShown(e -> {
                P2GuiSize.setSizePos(ProgConfig.SYSTEM_SIZE_BIG_GUI, progData.primaryStage);
                progData.backupBigGui.setShowing();
            });

            P2CssFactory.addP2CssToScene(progData.primaryStage.getScene()); // und jetzt noch CSS einstellen
            setTitle();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void setTitle() {
        // muss nur für das große GUI gesetzt werden
        Stage stage = ProgData.getInstance().primaryStage;
        if (ProgData.debug) {
            stage.setTitle(ProgConst.PROGRAM_NAME + " " + P2InfoFactory.getProgVersion() + " / DEBUG");
        } else {
            stage.setTitle(ProgConst.PROGRAM_NAME + " " + P2InfoFactory.getProgVersion());
        }
    }

    private void initSmallLayout() {
        try {
            progData.backupSmallGui = new BackupSmallGui();
            progData.primaryStageSmall = progData.backupSmallGui.getStage();
            progData.primaryStageSmall.setOnCloseRequest(e -> {
                //beim Beenden
                e.consume();
                ProgQuit.quit();
            });

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void selectGui() {
        if (ProgConfig.SYSTEM_SMALL_BACKUP.getValue()) {
            if (!smallDone) {
                smallDone = true;
                initSmallLayout();
            }
            progData.primaryStageSmall.show();
            if (progData.primaryStage.isShowing()) {
                // nur wenn zu sehen, nicht beim Start in small!!
                P2GuiSize.getSize(ProgConfig.SYSTEM_SIZE_BIG_GUI, progData.primaryStage);
                Platform.runLater(() -> progData.primaryStage.close()); // kann erst geschlossen werden, wenn SMAL steht!!
            }

        } else {
            // BIG anzeigen
            if (progData.backupSmallGui != null &&
                    ProgData.getInstance().primaryStageSmall.isShowing()) {
                // nur wenn zu sehen, nicht beim Start in small!!
                progData.primaryStageSmall.hide();
            }

            progData.primaryStage.show();
        }
    }
}



