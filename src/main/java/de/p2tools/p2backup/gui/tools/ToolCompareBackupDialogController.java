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
import de.p2tools.p2backup.controller.runner.tools.ToolCompareHash;
import de.p2tools.p2backup.controller.runner.tools.ToolCompareHashSql;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolCompareDir;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
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

public class ToolCompareBackupDialogController extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final FileDataList fileDataList = new FileDataList();
    private final ProgData progData;
    private final ComboBox<BackupData> cboBackup = new ComboBox<>();
    private final Button btnStart = new Button("Starten");
    private final TableToolCompareDir tableView;

    private final RadioButton rbAll = new RadioButton("Alle");
    private final RadioButton rbNotOk = new RadioButton("Daten/Backup unterschiedlich");
    private final RadioButton rbErrorDiff = new RadioButton("Verändert");
    private final RadioButton rbOnlyData = new RadioButton("Nur in den Daten");
    private final RadioButton rbOnlyBackup = new RadioButton("Nur im Backup");
    private final RadioButton rbErrorHash = new RadioButton("Kann nicht gelesen werden");
    private final CheckBox chkLong = new CheckBox("Neu einlesen");
    private final Label lblSum = new Label();

    public ToolCompareBackupDialogController(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.COMPARE_DIALOG_SIZE, "Daten mit Backup vergleichen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        tableView = new TableToolCompareDir(Table.TABLE_ENUM.DIR_COMPARE);
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);
        HBox hBox = getProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().add(hBox);

        init();
        addTable();
        addRadio();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.DIR_COMPARE);
        backupInfo.runnerDto.setStop();
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

            if (chkLong.isSelected()) {
                backupInfo.runnerDto.setRunnerText("Daten mit Backup vergleichen");
                new ToolCompareHash(this, backupInfo,
                        backupData, new AtomicBoolean(true)).compare();
            } else {
                new ToolCompareHashSql(this, backupInfo,
                        backupData, new AtomicBoolean(true)).compare();
            }
        });
        btnStart.disableProperty().bind(
                (cboBackup.getSelectionModel().selectedItemProperty().isNull())
        );

        Button btnHelp = P2Button.helpButton(getStage(), "Neu einlesen",
                "Beim \"neu Einlesen\" werden die Dateien (Daten und Backup) " +
                        "neu gelesen und werden dann verglichen. Ansonsten werden die gespeicherten Infos " +
                        "verglichen. Das \"neue Einlesen\" kann bei vielen Dateien lange dauern.");

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(new Label("Backup:"), cboBackup, chkLong,
                P2GuiTools.getHBoxGrower(), btnHelp, btnStart);

        getVBoxCont().getChildren().addAll(hBox);
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

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(rbAll, rbNotOk, rbErrorDiff, rbOnlyData, rbOnlyBackup, rbErrorHash,
                P2GuiTools.getHBoxGrower(), lblSum);
        getVBoxCont().getChildren().add(hBox);
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
        Predicate<FileData> prExistNotInData = data -> !data.isExistInData();
        Predicate<FileData> prExistNotInBackup = data -> !data.isExistInBackup();
        Predicate<FileData> prErrorHash = FileDataProps::isErrorHash;
        if (rbNotOk.isSelected()) {
            predicate = predicate.and(prErrorDiff.or(prExistNotInData).or(prExistNotInBackup).or(prErrorHash));

        } else if (rbErrorDiff.isSelected()) {
            predicate = predicate.and(prErrorDiff);

        } else if (rbOnlyData.isSelected()) {
            predicate = predicate.and(prExistNotInBackup);

        } else if (rbOnlyBackup.isSelected()) {
            predicate = predicate.and(prExistNotInData);

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

    private HBox getProgress() {
        final PProgressBar progressBar = new PProgressBar(true, true);

        Button btnStop = new Button();
//        btnStop.setMinHeight(18);
//        btnStop.setMaxHeight(18);
        btnStop.setGraphic(PIconFactory.PICON.TABLE_FILE_DEL.getFontIcon());
        btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.setPadding(new Insets(0, 10, 0, 10));
        hBoxProgress.getChildren().addAll(progressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}