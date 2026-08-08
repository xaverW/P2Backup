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
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
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

public class ToolFileHistoryDialogController extends P2DialogExtra {

    private final Label lblSum = new Label();
    private final Button btnLoad = new Button("Dateien laden");

    private final ObjectProperty<BackupInfo> backupInfoProp = new SimpleObjectProperty<>(null);
    private ObjectProperty<BackupData> backupDataProp = new SimpleObjectProperty<>(null);

    private final ListView<FileData> listViewFile = new ListView<>();
    private final TableToolFileHistory tableView;
    private final TextField txtSearch = new TextField();
    private final FileDataList fileDataList = new FileDataList();
    private final Label lblToPath = new Label();
    private final Label lblFilePath = new Label();
    private final Label lblFileName = new Label();

    public ToolFileHistoryDialogController(ProgData progData) {
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
        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().add(hBox);

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
            lblSum.setText(tableView.getItems().size() + "");
            setPred();
        });
    }

    private void initGui() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);

        gridPane.add(P2Text.getLblTextBold("Dateiname:"), 0, 0);
        gridPane.add(lblFileName, 1, 0);
        gridPane.add(P2Text.getLblTextBold("Dateipfad:"), 0, 1);
        gridPane.add(lblFilePath, 1, 1);
        gridPane.add(P2Text.getLblTextBold("Backupordner:"), 0, 2);
        gridPane.add(lblToPath, 1, 2);

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
        txtSearch.textProperty().addListener((u, o, n) -> setPred());
    }

    private void setPred() {
        Predicate<FileData> pred = (p -> true);
        pred = pred.and(f -> f.getFileNameStr().toLowerCase().contains(txtSearch.getText().toLowerCase()));
        fileDataList.getFilteredList().setPredicate(pred);
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
        lblSum.setText(tableView.getItems().size() + "");
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
        hBox.getChildren().addAll(new Label("Suchen: "), txtSearch, P2GuiTools.getHBoxGrower(), btnLoad);
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
        hBox.getChildren().addAll(P2GuiTools.getHBoxGrower(), new Label("Anzahl: "), lblSum);
        hBox.setAlignment(Pos.CENTER_LEFT);
        getVBoxCont().getChildren().add(hBox);
    }
}