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

package de.p2tools.p2backup.gui.tools;


import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.runner.tools.ToolCountFiles;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Open;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import de.p2tools.p2lib.mediathek.tools.P2SizeTools;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;

public class DialogBackupInfo extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final ProgData progData;
    private final VBox vBoxGrid = new VBox();

    public DialogBackupInfo(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.BACKUP_INFO_DIALOG_SIZE, "Infos über das Backup anzeigen",
                true, true, false, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        init(true);
    }

    @Override
    public void close() {
        backupInfo.runnerDto.setStop();
        super.close();
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);

        Button btnHelp = P2Button.helpButton(getStage(), "Infos",
                "Hier werden Infos über das Backup angezeigt: Anzahl Backups, Größe, ..");

        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().addAll(hBox, btnHelp);
        add();
        search();
    }

    public void set() {
        setInfo();
    }

    private void search() {
        backupInfo.runnerDto.setRunnerText("Infos laden");
        new ToolCountFiles(progData, this,
                backupInfo, false, true).count();
    }

    private void setInfo() {
        vBoxGrid.getChildren().clear();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(10);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcPrefSizeCenter(),
                P2GridConstraints.getCcPrefSizeRight(),
                P2GridConstraints.getCcPrefSizeRight());

        gridPane.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        gridPane.getStyleClass().add("dialogInfo");

        int row = 0;


        VBox vBoxCount = new VBox();
        vBoxCount.setAlignment(Pos.CENTER_RIGHT);
        vBoxCount.getChildren().addAll(P2Text.getLblTextBold("Anzahl"), P2Text.getLblTextBold("Dateien"));
        gridPane.add(vBoxCount, 2, row);

        VBox vBoxSize = new VBox();
        vBoxSize.setAlignment(Pos.CENTER_RIGHT);
        vBoxSize.getChildren().addAll(P2Text.getLblTextBold("Größe"), P2Text.getLblTextBold("Ordner"));
        gridPane.add(vBoxSize, 3, row);

        // zuerst die DATEN
        gridPane.add(P2Text.getLblTextBoldUnderlineSize("Daten", "1.1"), 0, ++row);
        for (PathData p : backupInfo.getPathListFrom()) {
            final Button btnOpenDirectory;
            btnOpenDirectory = new Button();
            btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
            btnOpenDirectory.setTooltip(new Tooltip("Ordner öffnen"));
            btnOpenDirectory.setGraphic(P2IconFactory.P2ICON.P2_BTN_OPEN_DIR.getFontIcon());
            btnOpenDirectory.setOnAction((ActionEvent event) -> {
                Path path = p.getFilePathPath();
                if (path != null && path.toFile().exists() && path.toFile().isDirectory()) {
                    P2Open.openDir(getStage(), path.toFile().toString());
                }
            });
            btnOpenDirectory.setMinHeight(18);
            btnOpenDirectory.setMaxHeight(18);

            gridPane.add(new Label(p.getPath()), 0, ++row);
            gridPane.add(btnOpenDirectory, 1, row);
            if (backupInfo.getPathListFrom().size() > 1) {
                gridPane.add(new Label(p.getCount() + ""), 2, row);
                gridPane.add(new Label(P2SizeTools.humanReadableByteCount(p.getSize(), true)), 3, row);
            } else {
                gridPane.add(P2Text.getLblTextBold(p.getCount() + ""), 2, row);
                gridPane.add(P2Text.getLblTextBold(P2SizeTools.humanReadableByteCount(p.getSize(), true)), 3, row);
            }
        }

        if (backupInfo.getPathListFrom().size() > 1) {
            gridPane.add(P2Text.getLblTextBold("Summe:"), 0, ++row);
            gridPane.add(P2Text.getLblTextBold(backupInfo.getCount() + ""), 2, row);
            gridPane.add(P2Text.getLblTextBold(P2SizeTools.humanReadableByteCount(backupInfo.getSize(), true)), 3, row);
        }


        // Dann die angelegten Backups
        gridPane.add(new Label(""), 0, ++row);
        gridPane.add(P2Text.getLblTextBoldUnderlineSize("Backup-Ordner", "1.1"), 0, ++row);
        int sum = 0;
        long sumSize = 0;
        for (BackupData backupData : backupInfo.getBackupDataList()) {
            final Button btnOpenDirectory;
            btnOpenDirectory = new Button();
            btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
            btnOpenDirectory.setTooltip(new Tooltip("Ordner öffnen"));
            btnOpenDirectory.setGraphic(P2IconFactory.P2ICON.P2_BTN_OPEN_DIR.getFontIcon());
            btnOpenDirectory.setOnAction((ActionEvent event) -> {
                Path path = backupData.getToPath(backupInfo);
                if (path != null && path.toFile().exists() && path.toFile().isDirectory()) {
                    P2Open.openDir(getStage(), path.toFile().toString());
                }
            });
            btnOpenDirectory.setMinHeight(18);
            btnOpenDirectory.setMaxHeight(18);

            gridPane.add(new Label(backupInfo.getBackupPath() + File.separator + backupData.getSubPath()), 0, ++row);
            gridPane.add(btnOpenDirectory, 1, row);
            sum += backupData.getCount();
            sumSize += backupData.getSize();
            gridPane.add(new Label(backupData.getCount() + ""), 2, row);
            gridPane.add(new Label(P2SizeTools.humanReadableByteCount(backupData.getSize(), true)), 3, row);
        }

        gridPane.add(P2Text.getLblTextBold("Summe"), 0, ++row);
        gridPane.add(P2Text.getLblTextBold(sum + ""), 2, row);
        gridPane.add(P2Text.getLblTextBold(P2SizeTools.humanReadableByteCount(sumSize, true)), 3, row);

        vBoxGrid.getChildren().add(gridPane);
    }

    private void add() {
//        Button btnSearch = new Button("Infos laden");
//        btnSearch.disableProperty().bind(backupInfo.runnerDto.guiRunningProperty());
//        btnSearch.setOnAction(a -> search());

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.getChildren().addAll(P2Text.getLblTextBold("Backup:"),
                new Label(backupInfo.getName()), P2GuiTools.getHBoxGrower());
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getStyleClass().add("infoDialogTop");
        getVBoxCont().getChildren().addAll(hBoxBtn);

        vBoxGrid.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(vBoxGrid, Priority.ALWAYS);
        getVBoxCont().getChildren().add(vBoxGrid);
    }

    private HBox addProgress() {
        PProgressBar pProgressBar = new PProgressBar(backupInfo, true, true, true);
        Button btnStop = new Button();
        btnStop.setGraphic(P2IconFactory.P2ICON.P2_BTN_STOP.getFontIcon());
        btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.getChildren().addAll(pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}