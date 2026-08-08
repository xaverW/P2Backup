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
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.tools.RepairFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolCheckBackup;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableCheckBackup;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class CheckBackupDialogController extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final FileDataList fileDataList = new FileDataList();
    private final ProgData progData;
    private final ComboBox<BackupData> cboBackup = new ComboBox<>();
    private final TableCheckBackup tableView;
    private final Label lblSum = new Label();

    private final RadioButton rbAll = new RadioButton("Alles");
    private final RadioButton rbNotOk = new RadioButton("Fehler");
    private final RadioButton rbErrorDiff = new RadioButton("Datei ist verändert");
    private final RadioButton rbOnlyData = new RadioButton("Datei fehlt");
    private final RadioButton rbOnlyBackup = new RadioButton("Datei ist zu viel");
    private final RadioButton rbErrorHash = new RadioButton("Kann nicht gelesen werden");
    private final Button btnStart = new Button("Dateien laden");
    private final Button btnRepair = new Button("Reparieren");
    private final ObservableList<FileData> errorList = FXCollections.observableArrayList();

    public CheckBackupDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.CHECK_BACKUP_DIALOG_SIZE, "Backup prüfen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        tableView = new TableCheckBackup(Table.TABLE_ENUM.CHECK_BACKUP, getStageProp());
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);
        btnRepair.setOnAction(a -> {
            if (!RepairFactory.repairBackup(backupInfo, errorList)) {
                P2Alert.showErrorAlert(getStage(), "Dateien aus dem Backup löschen",
                        "Es konnten nicht alle fehlerhaften Dateien aus dem " +
                                "Backup gelöscht werden");
            }
            this.fileDataList.clear();
        });
        btnRepair.setDisable(true);
        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().addAll(hBox, btnRepair);

        init();
        addTable();
        addRadio();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.CHECK_BACKUP);
        backupInfo.runnerDto.setStop();
        super.close();
    }

    public void setResult(FileDataList fileDataList) {
        Platform.runLater(() -> {
                    this.fileDataList.setAll(fileDataList);
                    boolean found = false;
                    for (FileData fileData : this.fileDataList) {
                        if (fileData.isErrorDiff() || fileData.isOnlyInData() || fileData.isOnlyInBackup() ||
                                fileData.isErrorHash()) {
                            found = true;
                            errorList.add(fileData);
                        }
                    }
                    if (found) {
                        btnRepair.setDisable(false);
                        rbNotOk.setSelected(true);
                    }
                    this.setPredicate();
                }
        );
    }

    private void init() {
        cboBackup.setItems(backupInfo.getBackupDataList());
        cboBackup.getSelectionModel().selectLast();

        btnStart.setOnAction(a -> {
            errorList.clear();
            btnRepair.setDisable(true);
            BackupData backupData = cboBackup.getSelectionModel().getSelectedItem();
            if (backupData == null) {
                return;
            }
            fileDataList.clear();
            backupInfo.runnerDto.initRunner();
            backupInfo.runnerDto.setRunnerText("Backup prüfen");
            new ToolCheckBackup(this, backupInfo,
                    backupData, new AtomicBoolean(true)).compare();
        });

        btnStart.disableProperty().bind((cboBackup.getSelectionModel().selectedItemProperty().isNull()));

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(new Label("Backup:"), cboBackup, P2GuiTools.getHBoxGrower(), btnStart);
        getVBoxCont().getChildren().addAll(hBox/*, hBoxProgress*/);
    }

    private void addRadio() {
        ToggleGroup tg = new ToggleGroup();
        rbAll.setToggleGroup(tg);
        rbNotOk.setToggleGroup(tg);
        rbErrorDiff.setToggleGroup(tg);
        rbOnlyData.setToggleGroup(tg);
        rbOnlyBackup.setToggleGroup(tg);
        rbErrorHash.setToggleGroup(tg);
        rbAll.setSelected(true);

        HBox hBox1 = new HBox(P2LibConst.SPACING_HBOX);
        hBox1.getChildren().addAll(rbAll, rbNotOk, P2GuiTools.getHBoxGrower(), lblSum);
        HBox hBox2 = new HBox(P2LibConst.SPACING_HBOX);
        hBox2.getChildren().addAll(rbErrorDiff, rbOnlyData, rbOnlyBackup, rbErrorHash);
        getVBoxCont().getChildren().addAll(hBox1, hBox2);

        rbAll.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbNotOk.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbErrorDiff.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbOnlyData.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbOnlyBackup.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbErrorHash.selectedProperty().addListener((u, o, n) -> setPredicate());
    }

    private void setPredicate() {
        Predicate<FileData> predicate = fileData -> true;
        Predicate<FileData> prErrorDiff = FileDataProps::isErrorDiff;
        Predicate<FileData> prNotInData = data -> !data.isExistInData();
        Predicate<FileData> prNotInBackup = data -> !data.isExistInBackup();
        Predicate<FileData> prErrorHash = FileDataProps::isErrorHash;
        if (rbNotOk.isSelected()) {
            predicate = predicate.and(prErrorHash
                    .or(prErrorDiff).or(prNotInData).or(prNotInBackup).or(prErrorHash));

        } else if (rbErrorDiff.isSelected()) {
            predicate = predicate.and(prErrorDiff);

        } else if (rbOnlyData.isSelected()) {
            predicate = predicate.and(prNotInBackup);

        } else if (rbOnlyBackup.isSelected()) {
            predicate = predicate.and(prNotInData);

        } else if (rbErrorHash.isSelected()) {
            predicate = predicate.and(prErrorHash);
        }

        fileDataList.getFilteredList().setPredicate(predicate);
        lblSum.setText("Anzahl: " + fileDataList.getFilteredList().size());
    }

    private void addTable() {
        getVBoxCont().getChildren().addAll(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Table.setTable(tableView);
        tableView.setItems(fileDataList.getSortedList());
        fileDataList.getSortedList().comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setOnMousePressed(m -> {
            if (m.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu contextMenu = getContextMenu();
                tableView.setContextMenu(contextMenu);
            }
        });
    }

    private ContextMenu getContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem resetTable = new MenuItem("Tabelle zurücksetzen");
        resetTable.setOnAction(e -> tableView.resetTable());
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().addAll(resetTable);
        return contextMenu;
    }

    private HBox addProgress() {
        PProgressBar pProgressBar = new PProgressBar(true, true);
        Button btnStop = new Button();
        btnStop.setGraphic(PIconFactory.PICON.TABLE_FILE_DEL.getFontIcon());
        btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.getChildren().addAll(pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}