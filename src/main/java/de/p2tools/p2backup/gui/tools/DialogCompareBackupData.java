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
import de.p2tools.p2backup.controller.runner.tools.ToolCompareBackupData;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolCompareDir;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class DialogCompareBackupData extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final FileDataList fileDataList = new FileDataList();
    private final ProgData progData;
    private final ComboBox<BackupData> cboBackup = new ComboBox<>();
    private final Button btnStart = new Button("Backup laden");
    private final TableToolCompareDir tableView;

    private final RadioButton rbAllBackup = new RadioButton("Alle");
    private final RadioButton rbAllData = new RadioButton("Alle");
    private final RadioButton rbOk = new RadioButton("OK");
    private final RadioButton rbNotOk = new RadioButton("Fehler");
    private final RadioButton rbErrorDiff = new RadioButton("Datei ist verändert");
    private final RadioButton rbOnlyData = new RadioButton("Datei fehlt");
    private final RadioButton rbOnlyBackup = new RadioButton("Datei ist zu viel");
    private final RadioButton rbErrorHash = new RadioButton("Kann nicht gelesen werden");
    private final CheckBox chkQuick = new CheckBox("Schnell");
    private final Label lblSum = new Label();

    public DialogCompareBackupData(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.COMPARE_DIALOG_SIZE, "Daten mit Backup vergleichen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        tableView = new TableToolCompareDir(Table.TABLE_ENUM.DIR_COMPARE, getStageProp());
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);

        Button btnHelp = P2Button.helpButton(getStage(), "Backup mit den Daten vergleichen",
                "Hier kann man das Backup mit den Daten vergleichen. Damit sieht man, " +
                        "welche Dateien im Backup liegen und sich geändert haben." +
                        "\n\n" +
                        "Mit \"Schnell\" werden die Dateien (Daten und Backup) " +
                        "nur mit Größe und Änderungsdatum verglichen. Das ist schneller. " +
                        "Ansonsten wird der Hash der Dateien erstellt. Das ist sicherer, dauert aber " +
                        "deutlich länger.");

        HBox hBox = getProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().addAll(hBox, btnHelp);

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
        chkQuick.setSelected(true);
        cboBackup.setItems(backupInfo.getBackupDataList().sorted(Comparator.naturalOrder()));
        cboBackup.getSelectionModel().selectLast();
        cboBackup.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            fileDataList.clear();
            rbAllBackup.setSelected(true);
        });

        btnStart.setOnAction(a -> {
            BackupData backupData = cboBackup.getSelectionModel().getSelectedItem();
            if (backupData == null) {
                return;
            }
            fileDataList.clear();
            rbAllBackup.setSelected(true);
            backupInfo.runnerDto.initRunner();

            backupInfo.runnerDto.setRunnerText("Daten mit Backup vergleichen");
            new ToolCompareBackupData(this, backupInfo,
                    backupData, chkQuick.isSelected(), new AtomicBoolean(true)).compare();
        });
        btnStart.disableProperty().bind(
                (cboBackup.getSelectionModel().selectedItemProperty().isNull())
        );

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(P2Text.getLblTextBold("Backup:"), cboBackup, chkQuick,
                P2GuiTools.getHBoxGrower(), btnStart);

        getVBoxCont().getChildren().addAll(hBox);
    }

    private void addRadio() {
        ToggleGroup tg = new ToggleGroup();
        rbAllBackup.setToggleGroup(tg);
        rbAllData.setToggleGroup(tg);
        rbOk.setToggleGroup(tg);
        rbNotOk.setToggleGroup(tg);
        rbErrorDiff.setToggleGroup(tg);
        rbOnlyData.setToggleGroup(tg);
        rbOnlyBackup.setToggleGroup(tg);
        rbErrorHash.setToggleGroup(tg);
        rbAllBackup.setSelected(true);

        GridPane gridPane = new GridPane(P2LibConst.DIST_GRIDPANE_HGAP, P2LibConst.DIST_GRIDPANE_VGAP);
        gridPane.add(new Label("Daten:"), 0, 0);
        gridPane.add(rbAllData, 1, 0);

        gridPane.add(new Label("Backup:"), 0, 1);
//        gridPane.add(rbAllBackup, 1, 1);
//        gridPane.add(rbOk, 2, 1);
//        gridPane.add(rbNotOk, 3, 1);

//        gridPane.add(rbErrorDiff, 1, 2);
//        gridPane.add(rbOnlyData, 2, 2);
//        gridPane.add(rbOnlyBackup, 3, 2);
//        gridPane.add(rbErrorHash, 4, 2);
        getVBoxCont().getChildren().add(gridPane);

//        HBox hBox0 = new HBox(P2LibConst.SPACING_HBOX);
//        hBox0.setAlignment(Pos.CENTER_LEFT);
//        hBox0.getChildren().addAll(new Label("Daten:"), rbAllData);

        HBox hBox1 = new HBox(P2LibConst.SPACING_HBOX);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(/*new Label("Backup:"),*/ rbAllBackup, rbOk, rbNotOk);

        HBox hBox2 = new HBox(P2LibConst.SPACING_HBOX);
        GridPane.setHgrow(hBox2, Priority.ALWAYS);
        Label lbl = P2Text.getLblTextBold("Backup:");
        lbl.setVisible(false);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        hBox2.getChildren().addAll(/*lbl,*/ rbErrorDiff, rbOnlyData, rbOnlyBackup, rbErrorHash,
                P2GuiTools.getHBoxGrower(), lblSum);

        gridPane.add(hBox1, 1, 1);
        gridPane.add(hBox2, 1, 2);

//        getVBoxCont().getChildren().addAll(hBox0, hBox1, hBox2);
        rbAllBackup.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbAllData.selectedProperty().addListener((u, o, n) -> setPredicate());
        rbOk.selectedProperty().addListener((u, o, n) -> setPredicate());
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
        Predicate<FileData> prIsInData = FileDataProps::isExistInData;
        Predicate<FileData> prIsInBackup = FileDataProps::isExistInBackup;
        Predicate<FileData> prErrorHash = FileDataProps::isErrorHash;
        if (rbAllBackup.isSelected()) {
            // alle Backups
            predicate = predicate.and(prIsInBackup);

        } else if (rbAllData.isSelected()) {
            // alle Daten
            predicate = predicate.and(prIsInData);

        } else if (rbOk.isSelected()) {
            predicate = predicate.and(prIsInBackup)
                    .and(prErrorDiff.negate())
                    .and(prErrorHash.negate())
                    .and(prIsInData);


        } else if (rbNotOk.isSelected()) {
            predicate = predicate.and(prErrorHash
                    .or(prErrorDiff)
                    .or(prNotInData)
                    .or(prNotInBackup)
                    .or(prErrorHash));

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

    private HBox getProgress() {
        final PProgressBar progressBar = new PProgressBar(true, true);

        Button btnStop = new Button();
//        btnStop.setMinHeight(18);
//        btnStop.setMaxHeight(18);
        btnStop.setGraphic(P2IconFactory.P2ICON.P2_BTN_STOP.getFontIcon());
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