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
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolSearchInBackup;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicBoolean;

public class ToolSearchInBackupDialogController extends P2DialogExtra {

    private final Label lblSum = new Label();
    private final Button btnLoad = new Button("Dateien laden");
    private final ComboBox<BackupData> cboBackup = new ComboBox<>();

    private final ObjectProperty<BackupInfo> backupInfoProp = new SimpleObjectProperty<>(null);
    private ObjectProperty<BackupData> backupDataProp = new SimpleObjectProperty<>(null);
    //    private Label lblPath = new Label("");
    private final ProgData progData;
    private final PaneSearchInBackup paneSearchInBackup;

    public ToolSearchInBackupDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.SEARCH_DIALOG_SIZE, "Dateien im Backup suchen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfoProp.set(backupInfo);
        this.paneSearchInBackup = new PaneSearchInBackup(getStageProp(), backupInfoProp.get(), backupDataProp);
        VBox.setVgrow(paneSearchInBackup, Priority.ALWAYS);
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);
        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().add(hBox);

        addSearch();
        addComboBox();
        getVBoxCont().getChildren().add(paneSearchInBackup);
        initSum();
    }

    public void close() {
        paneSearchInBackup.close();
        if (backupInfoProp.get() != null) {
            backupInfoProp.get().runnerDto.setStop();
        }
        super.close();
    }

    public void setResult(FileDataList fileDataList) {
        Platform.runLater(() -> paneSearchInBackup.makeTree(fileDataList));
    }

    private void addSearch() {
        btnLoad.setOnAction(a -> {
            if (cboBackup.getSelectionModel().getSelectedItem() != null) {
                backupDataProp.set(cboBackup.getSelectionModel().getSelectedItem());
                String subPath = backupDataProp.get().getSubPath();
                if (!subPath.isEmpty()) {
                    backupInfoProp.get().runnerDto.initRunner();
                    backupInfoProp.get().runnerDto.setRunnerText("Backup laden");
                    new ToolSearchInBackup(this,
                            backupInfoProp.get(), backupDataProp.get(), new AtomicBoolean(true)).search();
                }

            } else {
                backupDataProp.set(null);
            }
        });

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(cboBackup, P2GuiTools.getHBoxGrower(), btnLoad);
        getVBoxCont().getChildren().addAll(hBox);
    }

    private HBox addProgress() {
        Button btnStop = new Button();
        btnStop.setMinHeight(18);
        btnStop.setMaxHeight(18);
        btnStop.setGraphic(PIconFactory.PICON.TABLE_FILE_DEL.getFontIcon());
        btnStop.setOnAction(a -> backupInfoProp.get().runnerDto.setStop());

        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.setPadding(new Insets(0, 10, 0, 10));
        PProgressBar pProgressBar = new PProgressBar(true, true);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);
        hBoxProgress.getChildren().addAll(/*P2GuiTools.getHBoxGrower(),*/ pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER);

        hBoxProgress.visibleProperty().bind(backupInfoProp.get().runnerDto.guiRunningProperty());
        return hBoxProgress;
    }

    private void addComboBox() {
        cboBackup.setItems(backupInfoProp.get().getBackupDataList());
        if (!cboBackup.getItems().isEmpty()) {
            cboBackup.getSelectionModel().selectLast();
        }
        cboBackup.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            paneSearchInBackup.clearTree();
        });

        btnLoad.disableProperty().bind((cboBackup.getSelectionModel().selectedItemProperty().isNull()));
    }

    private void initSum() {
        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(P2GuiTools.getHBoxGrower(), new Label("Anzahl: "), lblSum);
        hBox.setAlignment(Pos.CENTER_LEFT);
        paneSearchInBackup.getSizeProp().addListener((u, o, n) -> lblSum.setText(paneSearchInBackup.getSizeProp().get() + ""));
        getVBoxCont().getChildren().add(hBox);
    }
}