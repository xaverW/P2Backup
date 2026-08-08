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
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolCountFiles;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Open;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
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

public class ToolBackupInfoDialogController extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final ProgData progData;
    private final VBox vBoxGrid = new VBox();

    public ToolBackupInfoDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.BACKUP_INFO_DIALOG_SIZE, "Infos über die Daten",
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
        add();
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
        Label lblSum = P2Text.getLblTextBold("Anzahl\nDateien");
        lblSum.setWrapText(true);
        gridPane.add(lblSum, 2, row);
        Label lblSize = P2Text.getLblTextBold("Größe\nOrdner");
        lblSize.setWrapText(true);
        gridPane.add(lblSize, 3, row);


        // zuerst die DATEN
        gridPane.add(P2Text.getLblTextBold("Daten"), 0, ++row);
        ++row;
        if (backupInfo.getPathListFrom().size() > 1) {
            gridPane.add(new Label("Summe aller Dateien:"), 0, ++row);
        }
        gridPane.add(new Label(backupInfo.getCount() + ""), 2, row);
        gridPane.add(new Label(P2SizeTools.humanReadableByteCount(backupInfo.getSize(), true)), 3, row);

        for (PathData p : backupInfo.getPathListFrom()) {
            final Button btnOpenDirectory;
            btnOpenDirectory = new Button();
            btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
            btnOpenDirectory.setTooltip(new Tooltip("Ordner öffnen"));
            btnOpenDirectory.setGraphic(PIconFactory.PICON.TABLE_DIR_OPEN.getFontIcon());
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
            gridPane.add(new Label(p.getCount() + ""), 2, row);
            gridPane.add(new Label(P2SizeTools.humanReadableByteCount(p.getSize(), true)), 3, row);
        }


        // Dann die angelegten Backups
        gridPane.add(new Label(""), 0, ++row);
        gridPane.add(P2Text.getLblTextBold("Backup-Ordner"), 0, ++row);
        ++row;
        for (BackupData backupData : backupInfo.getBackupDataList()) {
            final Button btnOpenDirectory;
            btnOpenDirectory = new Button();
            btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
            btnOpenDirectory.setTooltip(new Tooltip("Ordner öffnen"));
            btnOpenDirectory.setGraphic(PIconFactory.PICON.TABLE_DIR_OPEN.getFontIcon());
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
            gridPane.add(new Label(backupData.getCount() + ""), 2, row);
            gridPane.add(new Label(P2SizeTools.humanReadableByteCount(backupData.getSize(), true)), 3, row);
        }

        vBoxGrid.getChildren().add(gridPane);
    }

    private void add() {
        Button btnSearch = new Button("Infos laden");
        btnSearch.disableProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        btnSearch.setOnAction(a -> search());

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.getChildren().addAll(P2Text.getLblTextBold(backupInfo.getName()), P2GuiTools.getHBoxGrower(),
                new PProgressBar(backupInfo, true, false, true), btnSearch);
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getStyleClass().add("infoDialogTop");
        getVBoxCont().getChildren().addAll(hBoxBtn);

        vBoxGrid.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(vBoxGrid, Priority.ALWAYS);
        getVBoxCont().getChildren().add(vBoxGrid);
    }
}