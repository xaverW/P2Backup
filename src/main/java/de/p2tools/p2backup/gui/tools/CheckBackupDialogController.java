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
import de.p2tools.p2backup.controller.runner.tools.ToolCheckBackup;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableCheckBackup;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.application.Platform;
import javafx.geometry.Insets;
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
    private final Button btnStart = new Button("Dateien laden");
    private final TableCheckBackup tableView;
    private final Label lblSum = new Label();

    private final RadioButton rbAll = new RadioButton("Alles");
    private final RadioButton rbNotOk = new RadioButton("Soll/Ist unterschiedlich");
    private final RadioButton rbDiff = new RadioButton("Datei verändert");
    private final RadioButton rbOnlyData = new RadioButton("Nur in der Datenbank, \"Soll\"");
    private final RadioButton rbOnlyBackup = new RadioButton("Nur in den Dateien, \"Ist\"");
    private final RadioButton rbReadError = new RadioButton("Kann nicht gelesen werden");

    public CheckBackupDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.CHECK_BACKUP_DIALOG_SIZE, "Backup prüfen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        tableView = new TableCheckBackup(Table.TABLE_ENUM.CHECK_BACKUP);
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

        init();
        addTable();
        addRadio();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.CHECK_BACKUP);
        super.close();
    }

    public void setResult(FileDataList fileDataList) {
        Platform.runLater(() -> {
                    this.fileDataList.setAll(fileDataList);
                    this.setPredicate();
                }
        );
    }

    private void init() {
        cboBackup.setItems(backupInfo.getBackupDataList());
        cboBackup.getSelectionModel().selectLast();

        btnStart.setOnAction(a -> {
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

//        HBox hBoxProgress = addProgress();
//        HBox.setHgrow(hBoxProgress, Priority.ALWAYS);
        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(new Label("Backup:"), cboBackup, P2GuiTools.getHBoxGrower(),
                /*hBoxProgress,*/ btnStart);
        getVBoxCont().getChildren().addAll(hBox/*, hBoxProgress*/);
    }

    private void addRadio() {
        ToggleGroup tg = new ToggleGroup();
        rbAll.setToggleGroup(tg);
        rbNotOk.setToggleGroup(tg);
        rbDiff.setToggleGroup(tg);
        rbOnlyData.setToggleGroup(tg);
        rbOnlyBackup.setToggleGroup(tg);
        rbReadError.setToggleGroup(tg);
        rbAll.setSelected(true);

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(rbAll, rbNotOk, rbDiff, rbOnlyData, rbOnlyBackup, rbReadError,
                P2GuiTools.getHBoxGrower(), lblSum);
        getVBoxCont().getChildren().add(hBox);
        rbAll.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbNotOk.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbDiff.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbOnlyData.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbOnlyBackup.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbReadError.selectedProperty().addListener((u, o, n) -> setPredicate());
    }

    private void setPredicate() {
        Predicate<FileData> predicate = fileData -> true;
        if (rbNotOk.isSelected()) {
            Predicate<FileData> prDiff = FileDataProps::isDiff;

            Predicate<FileData> prData = FileDataProps::isExistData;
            prData = prData.and(fileData -> !fileData.isExistBackup());

            Predicate<FileData> prBackup = FileDataProps::isExistBackup;
            prBackup = prBackup.and(fileData -> !fileData.isExistData());

            predicate = predicate.and(prDiff.or(prData).or(prBackup));

        } else if (rbDiff.isSelected()) {
            predicate = predicate.and(FileDataProps::isDiff);

        } else if (rbOnlyData.isSelected()) {
            predicate = predicate.and(FileDataProps::isExistData);
            predicate = predicate.and(fileData -> !fileData.isExistBackup());

        } else if (rbOnlyBackup.isSelected()) {
            predicate = predicate.and(fileData -> !fileData.isExistData());
            predicate = predicate.and(FileDataProps::isExistBackup);

        } else if (rbReadError.isSelected()) {
            predicate = predicate.and(FileDataProps::isError);
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
        hBoxProgress.setPadding(new Insets(0, 10, 0, 10));
        hBoxProgress.getChildren().addAll(pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.runningProperty());
        return hBoxProgress;
    }
}