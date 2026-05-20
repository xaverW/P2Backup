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
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.mediathek.tools.P2SizeTools;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BackupInfoDialogController extends P2DialogExtra {

    private final BackupInfo backupInfos;
    private final ProgData progData;
    private final VBox vBoxGrid = new VBox();

    public BackupInfoDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.BACKUP_INFO_DIALOG_SIZE, "Infos über die Daten",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfos = backupInfo;
        init(true);
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
        new ToolCountFiles(progData, this,
                backupInfos, false, true).count();
    }

    private void setInfo() {
        vBoxGrid.getChildren().clear();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(5);
        gridPane.getStyleClass().add("infoBackupDialogGridPane");
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrowRight(),
                P2GridConstraints.getCcComputedSizeAndHgrowRight());

        int row = 0;
        Label lblSum = new Label("Anzahl Dateien");
        gridPane.add(lblSum, 0, row, 2, 1);
        GridPane.setHalignment(lblSum, HPos.RIGHT);
        gridPane.add(new Label("Größe"), 2, row);


        // zuerst die DATEN
        gridPane.add(new Label(""), 0, ++row);
        gridPane.add(P2Text.getLblTextBold("Daten"), 0, ++row);
        if (backupInfos.getPathListFrom().size() > 1) {
            gridPane.add(new Label("Summe aller Dateien:"), 0, ++row);
        }
        gridPane.add(new Label(backupInfos.getCount() + ""), 1, row);
        gridPane.add(new Label(P2SizeTools.humanReadableByteCount(backupInfos.getSize(), true)), 2, row);

        for (PathData p : backupInfos.getPathListFrom()) {
            gridPane.add(new Label(p.getPath()), 0, ++row);
            gridPane.add(new Label(p.getCount() + ""), 1, row);
            gridPane.add(new Label(P2SizeTools.humanReadableByteCount(p.getSize(), true)), 2, row);
        }


        // Dann die angelegten Backups
        gridPane.add(new Label(""), 0, ++row);
        gridPane.add(P2Text.getLblTextBold("Backups:"), 0, ++row);
        for (BackupData backupData : backupInfos.getBackupDataList()) {
            gridPane.add(new Label(backupData.getSubPath()), 0, ++row);
            gridPane.add(new Label(backupData.getCount() + ""), 1, row);
            gridPane.add(new Label(P2SizeTools.humanReadableByteCount(backupData.getSize(), true)), 2, row);
        }

        vBoxGrid.getChildren().add(gridPane);
    }

    private void add() {
        Button btnSearch = new Button("Infos laden");
        btnSearch.setOnAction(a -> search());

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.getChildren().addAll(P2Text.getLblTextBold(backupInfos.getName()), P2GuiTools.getHBoxGrower(), btnSearch);
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getStyleClass().add("infoBackupDialogTop");
        getVBoxCont().getChildren().addAll(hBoxBtn);

        getVBoxCont().getChildren().add(vBoxGrid);
        getVBoxCont().getChildren().add(P2GuiTools.getVBoxGrower());
    }
}