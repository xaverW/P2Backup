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

package de.p2tools.p2backup.gui.guismall;


import de.p2tools.p2backup.controller.ProgQuit;
import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.backuprunner.BackupRunner;
import de.p2tools.p2backup.gui.guibig.BackupGuiFactory;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.dialogs.dialog.P2DialogOnly;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2SmallGuiFactory;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class BackupSmallGui extends P2DialogOnly {

    private final ProgData progData;
    private final VBox vBox = new VBox();
    private final Button btnStartAll = new Button();

    public BackupSmallGui() {
        super(ProgData.getInstance().primaryStage, null, "P2Backup");

        this.progData = ProgData.getInstance();
        init(false);
        P2SmallGuiFactory.addBorderListener(getStage(), vBox);

        if (!ProgConfig.SYSTEM_SMALL_GUI_SHOW_START_HELP.getValue()) {
            new SmallGuiHelpDialogController(ProgData.getInstance().primaryStage);
            ProgConfig.SYSTEM_SMALL_GUI_SHOW_START_HELP.setValue(true);
        }
    }

    @Override
    public void close() {
        // Abfangen: Beenden mit "ESC"
        saveDialog();
        P2DialogExtra.getDialogList().remove(this);
        P2Log.debugLog("Anzahl Dialoge: " + P2DialogExtra.getDialogList().size());
        super.close();
    }

    @Override
    public void make() {
        initDialog();
        addBtn();
        addBackupList();
        progData.backupInfoList.addListener((u, o, n) -> {
            setStartAll();
            addBackupList();
            getStage().sizeToScene();
        });
    }

    private void initDialog() {
        getVBoxCompleteDialog().setPadding(new Insets(3));
        getVBoxCompleteDialog().setSpacing(0);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(vBox);
        getVBoxCompleteDialog().getChildren().addAll(scrollPane, P2GuiTools.getVBoxGrower());
    }

    private void addBtn() {
        final Button btnSmall = P2Button.getButton(P2IconFactory.P2ICON.SMALL_GUI_ICON_25.getFontIcon(), "Kleine Ansicht");
        btnSmall.getStyleClass().add("changeGuiBtn");
        btnSmall.setOnAction(a -> BackupGuiFactory.changeGui());


        btnStartAll.getStyleClass().addAll("smallGuiBtn");
        btnStartAll.setGraphic(PIconFactory.PICON.BTN_START_ALL.getFontIcon());
        btnStartAll.setTooltip(new Tooltip("Alle Backups starten"));
        btnStartAll.setOnAction(a -> progData.backupInfoList.forEach(b -> {
            if ((!b.runnerDto.getGuiRunning()) && (!b.isNotReady())) {
                new BackupRunner(b).makeBackup();
            }
        }));
        setStartAll();


        final Button btnQuitt = new Button();
        btnQuitt.setGraphic(P2IconFactory.P2ICON.BTN_QUIT.getFontIcon());
        btnQuitt.setTooltip(new Tooltip("Programm beenden"));
        btnQuitt.getStyleClass().addAll("smallGuiBtn");
        btnQuitt.setOnAction(a -> ProgQuit.quit());


        final HBox hBoxButton = new HBox();
        hBoxButton.getChildren().addAll(btnSmall, P2GuiTools.getHBoxGrower(), btnStartAll, btnQuitt);
        hBoxButton.setSpacing(5);
        hBoxButton.setPadding(new Insets(10, 0, 5, 0));
        hBoxButton.setAlignment(Pos.CENTER);
        getVBoxCompleteDialog().getChildren().addAll(hBoxButton);
    }

    private void setStartAll() {
        Optional<BackupInfo> opt = progData.backupInfoList.stream().filter(backupInfo -> !backupInfo.isNotReady()).findAny();
        if (opt.isPresent()) {
            btnStartAll.setDisable(false);
        } else {
            btnStartAll.setDisable(true);
        }
    }

    private void addBackupList() {
        vBox.getChildren().clear();

        for (BackupInfo backupInfo : progData.backupInfoList) {
            Button btnStart = new Button("");
            btnStart.setMinHeight(18);
            btnStart.setMaxHeight(18);
            btnStart.setGraphic(P2IconFactory.P2ICON.BTN_NEXT_15.getFontIcon());
            btnStart.disableProperty().bind(backupInfo.runnerDto.guiRunningProperty());
            btnStart.setOnAction(a -> {
                new BackupRunner(backupInfo).makeBackup();
            });
            btnStart.disableProperty().bind(backupInfo.runnerDto.guiRunningProperty()
                    .or(backupInfo.notReadyProperty()));

            Button btnStop = new Button();
            btnStop.setMinHeight(18);
            btnStop.setMaxHeight(18);
            btnStop.setGraphic(P2IconFactory.P2ICON.BTN_STOP_15.getFontIcon());
            btnStop.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
            btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

            final ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(backupInfo.runnerDto.guiProgressProperty());

            final Label lblOk = new Label("  ");
            final Label lblName = new Label();
            lblName.textProperty().bind(backupInfo.nameProperty());
            lblName.setMaxWidth(Double.MAX_VALUE);

            if (backupInfo.runnerDto.getDoneFirstRun()) {
                if (backupInfo.runnerDto.getGuiRunning()) {
                    lblOk.getStyleClass().add("smallGuiNameRun");
                } else if (backupInfo.runnerDto.isOk()) {
                    lblOk.getStyleClass().add("smallGuiNameOk");
                } else {
                    lblOk.getStyleClass().add("smallGuiNameError");
                }
            }

            backupInfo.runnerDto.guiRunningProperty().addListener((u, o, n) -> {
                lblOk.getStyleClass().remove("smallGuiNameRun");
                lblOk.getStyleClass().remove("smallGuiNameOk");
                lblOk.getStyleClass().remove("smallGuiNameError");

                if (backupInfo.runnerDto.getGuiRunning()) {
                    lblOk.getStyleClass().add("smallGuiNameRun");
                } else if (backupInfo.runnerDto.isOk()) {
                    lblOk.getStyleClass().add("smallGuiNameOk");
                } else {
                    lblOk.getStyleClass().add("smallGuiNameError");
                }
            });


            HBox hBoxProgress = new HBox(5);
            hBoxProgress.setAlignment(Pos.CENTER);
            hBoxProgress.getChildren().addAll(lblOk, lblName/*, P2GuiTools.getVDistance(20)*/, btnStop, progressBar, btnStart);
            HBox.setHgrow(lblName, Priority.ALWAYS);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            vBox.getChildren().add(hBoxProgress);
        }
    }
}