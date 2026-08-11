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
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.tools.ToolListBlockFile;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolBlockedFile;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class DialogBlockedFiles extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private final Set<File> foundFileList = new HashSet<>();
    private final Set<File> blockedFileList = new HashSet<>();
    private final ObservableList<File> fileList = FXCollections.observableArrayList();
    private final FilteredList<File> filteredFileList;
    private final SortedList<File> sortedFileList;
    private final Label lblSum = new Label();
    private final ProgData progData;
    private final TextField txtSearch = new TextField();
    private final Button btnStart = new Button("Dateien laden");
    private final Button btnClear = new Button();
    private final TableToolBlockedFile tableView;
    private final RadioButton rbAll = new RadioButton("Alle");
    private final RadioButton rbFound = new RadioButton("Sichern");
    private final RadioButton rbBlock = new RadioButton("Geblockt");

    public DialogBlockedFiles(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.BLOCKED_FILE_DIALOG_SIZE, "In den Daten/Backup suchen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        tableView = new TableToolBlockedFile(Table.TABLE_ENUM.BLOCKED_FILE, getStageProp());

        filteredFileList = new FilteredList<>(fileList, p -> true);
        sortedFileList = new SortedList<>(filteredFileList);
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
        addTable();
        addSum();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.BLOCKED_FILE);
        backupInfo.runnerDto.setStop();
        super.close();
    }

    private void set() {
        fileList.clear();
        if (rbAll.isSelected()) {
            fileList.addAll(foundFileList);
            fileList.addAll(blockedFileList);
        } else if (rbFound.isSelected()) {
            fileList.addAll(foundFileList);
        } else {
            fileList.addAll(blockedFileList);
        }
        setPredicate();
    }

    public void setResult() {
        Platform.runLater(this::set);
    }

    private void addSearch() {
        btnStart.setOnAction(a -> {
            foundFileList.clear();
            blockedFileList.clear();
            backupInfo.runnerDto.initRunner();
            backupInfo.runnerDto.setRunnerText("Geblockte Dateien suchen");
            new ToolListBlockFile(this,
                    backupInfo, foundFileList, blockedFileList, new AtomicBoolean(true)).search();
        });
        btnClear.setGraphic(PIconFactory.PICON.BTN_CLEAR.getFontIcon());
        btnClear.setTooltip(new Tooltip("Suche löschen"));
        btnClear.setOnAction(a -> txtSearch.clear());

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getStyleClass().add("infoDialogTop");
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(new Label("Suchen:"), txtSearch, btnClear,
                P2GuiTools.getHBoxGrower(), btnStart);
        getVBoxCont().getChildren().add(hBox);
        txtSearch.textProperty().addListener((u, o, n) -> setPredicate());
    }

    private void setPredicate() {
        Predicate<File> pr = f -> f.getAbsolutePath().toLowerCase().contains(txtSearch.getText().toLowerCase());
        filteredFileList.setPredicate(pr);
        lblSum.setText("Anzahl: " + filteredFileList.size());
    }

    private void addSum() {
        ToggleGroup tg = new ToggleGroup();
        rbAll.setToggleGroup(tg);
        rbFound.setToggleGroup(tg);
        rbBlock.setToggleGroup(tg);
        rbAll.setSelected(true);

        rbAll.setOnAction(a -> set());
        rbFound.setOnAction(a -> set());
        rbBlock.setOnAction(a -> set());

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(rbAll, rbFound, rbBlock,
                P2GuiTools.getHBoxGrower(), lblSum);
        hBox.setAlignment(Pos.CENTER_LEFT);
        getVBoxCont().getChildren().add(hBox);
    }

    private void addTable() {
        Table.setTable(tableView);
        getVBoxCont().getChildren().addAll(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        tableView.setItems(sortedFileList);
        sortedFileList.comparatorProperty().bind(tableView.comparatorProperty());
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

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}