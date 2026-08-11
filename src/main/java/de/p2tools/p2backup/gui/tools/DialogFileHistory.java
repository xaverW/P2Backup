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
import de.p2tools.p2backup.controller.data.filedata.HistoryFileData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolFileHistoryInBackup;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolFileHistory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class DialogFileHistory extends P2DialogExtra {

    private final Label lblSumFound = new Label();
    private final Label lblSumAll = new Label();
    private final Button btnLoad = new Button("Dateien laden");
    private final Button btnClear = new Button("");

    private final ObjectProperty<BackupInfo> backupInfoProp = new SimpleObjectProperty<>(null);
    private ObjectProperty<BackupData> backupDataProp = new SimpleObjectProperty<>(null);

    private final ListView<FileData> listViewFile = new ListView<>();
    private final TableToolFileHistory tableView;
    private final TextField txtSearch = new TextField();
    private final FileDataList fileDataList = new FileDataList();
    private final Label lblToPath = new Label();
    private final Label lblFilePath = new Label();
    private final Label lblFileName = new Label();

    public DialogFileHistory(ProgData progData) {
        super(progData.primaryStage, ProgConfig.FILE_HISTORY_DIALOG_SIZE, "Dateien im Backup suchen",
                true, true, true, DECO.NO_BORDER);

        this.backupInfoProp.set(progData.backupInfoProperty.get());
        this.tableView = new TableToolFileHistory(Table.TABLE_ENUM.FILE_HISTORY, getStageProp());
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);

        Button btnHelp = P2Button.helpButton(getStage(), "Änderungen einer Datei",
                "Es werden alle Dateien zum Sichern angezeigt. Beim Klick auf eine Datei " +
                        "werden alle Backups angezeigt in dem die Datei gesichert ist. " +
                        "Dadurch kann man sehen, wie oft sich die Datei geändert hat und dann gesichert wurde.");

        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().addAll(hBox, btnHelp);

        addSearch();
        initGui();
        initTable();
        initSum();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.FILE_HISTORY);
        backupInfoProp.get().runnerDto.setStop();
        super.close();
    }

    public void setResult(FileDataList fileDataList) {
        Platform.runLater(() -> {
            this.fileDataList.setAll(fileDataList);
            lblSumFound.setText(tableView.getItems().size() + "");
            lblSumAll.setText(listViewFile.getItems().size() + "");
            setPred();
        });
    }

    private void initGui() {
        Label lblBackupPath = P2Text.getLblTextBold("Backupordner:");
        Label lblPath = P2Text.getLblTextBold("Dateipfad:");
        Label lblFile = P2Text.getLblTextBold("Dateiname:");
//
        GridPane gridPane = new GridPane();
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrowLeft());

        int row = 0;
        gridPane.add(lblBackupPath, 0, row);
        gridPane.add(lblToPath, 1, row);
        gridPane.add(lblPath, 0, ++row);
        gridPane.add(lblFilePath, 1, row);
        gridPane.add(lblFile, 0, ++row);
        gridPane.add(lblFileName, 1, row);

        Label lblDaten = P2Text.getLblTextBold("Daten");
        Label lblBackup = P2Text.getLblTextBold("Backup");
        HBox hBoxDaten = new HBox();
        hBoxDaten.getStyleClass().add("dialogInfo");
        hBoxDaten.setAlignment(Pos.CENTER);
        hBoxDaten.getChildren().add(lblDaten);
        HBox hBoxBackup = new HBox();
        hBoxBackup.getStyleClass().add("dialogInfo");
        hBoxBackup.setAlignment(Pos.CENTER);
        hBoxBackup.getChildren().add(lblBackup);

        VBox vBoxDaten = new VBox(P2LibConst.SPACING_VBOX);
        vBoxDaten.setPadding(new Insets(5));
        vBoxDaten.getChildren().addAll(hBoxDaten, listViewFile);
        VBox.setVgrow(listViewFile, Priority.ALWAYS);

        VBox vBoxBackup = new VBox(P2LibConst.SPACING_VBOX);
        vBoxBackup.setPadding(new Insets(5));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        vBoxBackup.getChildren().addAll(hBoxBackup, gridPane, tableView);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(vBoxDaten, vBoxBackup);
        splitPane.getDividers().getFirst().positionProperty().bindBidirectional(ProgConfig.FILE_HISTORY_SPLIT_DIVIDER);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getVBoxCont().getChildren().addAll(splitPane);

        listViewFile.setItems(fileDataList.getSortedList());
        listViewFile.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> setTable());

        btnClear.setGraphic(PIconFactory.PICON.BTN_CLEAR.getFontIcon());
        btnClear.setTooltip(new Tooltip("Suche löschen"));
        btnClear.setOnAction(a -> txtSearch.clear());
        txtSearch.textProperty().addListener((u, o, n) -> setPred());
    }

    private void setPred() {
        Predicate<FileData> pred = (p -> true);
        pred = pred.and(f -> f.getFileNameStr().toLowerCase().contains(txtSearch.getText().toLowerCase()));
        fileDataList.getFilteredList().setPredicate(pred);
        lblSumAll.setText(listViewFile.getItems().size() + "");
    }

    private void initTable() {
        Table.setTable(tableView);
        tableView.setOnMousePressed(m -> {
            if (m.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu contextMenu = getContextMenu();
                tableView.setContextMenu(contextMenu);
            }
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            HistoryFileData f = tableView.getSelectionModel().getSelectedItem();
            if (f == null) {
                lblToPath.setText("");
            } else {
                lblToPath.setText(f.getToPathStr());
            }
        });
    }

    private void setTable() {
        FileData fileData = listViewFile.getSelectionModel().getSelectedItem();
        if (fileData == null) {
            tableView.getItems().clear();
            lblFileName.setText("");
            lblFilePath.setText("");
            return;
        }
        lblFileName.setText(fileData.getFileNameStr());
        lblFilePath.setText(fileData.getCorrParentFilePathStr());

        ObservableList<HistoryFileData> list = FXCollections.observableArrayList();
        SqlFileData.readFileHistoryList(backupInfoProp.get(), fileData.getFilePathStr(), list);
        tableView.setItems(list);
        tableView.getSelectionModel().selectFirst();
        lblSumFound.setText(tableView.getItems().size() + "");
    }

    private ContextMenu getContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem resetTable = new MenuItem("Tabelle zurücksetzen");
        resetTable.setOnAction(e -> tableView.resetTable());
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().addAll(resetTable);
        return contextMenu;
    }

    private void addSearch() {
        btnLoad.setOnAction(a -> {
            backupInfoProp.get().runnerDto.initRunner();
            backupInfoProp.get().runnerDto.setRunnerText("Backup laden");
            new ToolFileHistoryInBackup(this,
                    backupInfoProp.get(), new AtomicBoolean(true)).search();
        });

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(new Label("Suchen: "), txtSearch, btnClear, P2GuiTools.getHBoxGrower(), btnLoad);
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
        hBoxProgress.getChildren().addAll(pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER);

        hBoxProgress.visibleProperty().bind(backupInfoProp.get().runnerDto.guiRunningProperty());
        return hBoxProgress;
    }

    private void initSum() {
        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(new Label("Anzahl: "), lblSumAll,
                P2GuiTools.getHBoxGrower(),
                new Label("Anzahl: "), lblSumFound);
        hBox.setAlignment(Pos.CENTER_LEFT);
        getVBoxCont().getChildren().add(hBox);
    }
}