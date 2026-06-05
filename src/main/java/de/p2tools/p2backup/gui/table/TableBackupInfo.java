/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
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

package de.p2tools.p2backup.gui.table;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.guitools.ptable.P2CellCheckBox;
import de.p2tools.p2lib.guitools.ptable.P2TableFactory;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class TableBackupInfo extends PTable<BackupData> {

    private final Stage stage;
    private final ObjectProperty<BackupInfo> backupInfoProp;

    public TableBackupInfo(Table.TABLE_ENUM table_enum, Stage stage, ObjectProperty<BackupInfo> backupInfoProp) {
        super(table_enum);
        this.table_enum = table_enum;
        this.stage = stage;
        this.backupInfoProp = backupInfoProp;

        initFileRunnerColumn();
    }

    @Override
    public Table.TABLE_ENUM getTable() {
        return table_enum;
    }

    public void resetTable() {
        initFileRunnerColumn();
        Table.resetTable(this);
    }

    private void refreshTable() {
        P2TableFactory.refreshTable(this);
    }

    private void initFileRunnerColumn() {
        getColumns().clear();

        setTableMenuButtonVisible(true);
        setEditable(false);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        final TableColumn<BackupData, LocalDateTime> startDateColumn = new TableColumn<>("Startzeit");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setCellFactory(new CellBackupInfoStartDate().cellFactory);
        startDateColumn.getStyleClass().add("alignCenter");

        final TableColumn<BackupData, Boolean> okColumn = new TableColumn<>("Ok");
        okColumn.setCellValueFactory(new PropertyValueFactory<>("ok"));
        okColumn.setCellFactory(new P2CellCheckBox().cellFactory);

        final TableColumn<BackupData, Integer> countColumn = new TableColumn<>("Dateien");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        countColumn.getStyleClass().add("alignCenter");

        final TableColumn<BackupData, String> btnColumn = new TableColumn<>("");
        btnColumn.getStyleClass().add("alignCenter");
        btnColumn.setCellFactory(new CellOpenDelButton<>(stage, backupInfoProp).cellFactory);

        final TableColumn<BackupData, String> toPathColumn = new TableColumn<>("Pfad");
        toPathColumn.setCellValueFactory(new PropertyValueFactory<>("subPath"));
        toPathColumn.setCellFactory(new CellToPath<>(stage, backupInfoProp).cellFactory);

        startDateColumn.prefWidthProperty().bind(widthProperty().multiply(0.15));
        okColumn.prefWidthProperty().bind(widthProperty().multiply(0.05));
        countColumn.prefWidthProperty().bind(widthProperty().multiply(0.1));
        btnColumn.prefWidthProperty().bind(widthProperty().multiply(0.1));
        toPathColumn.prefWidthProperty().bind(widthProperty().multiply(0.5));

        getColumns().addAll(startDateColumn, okColumn, countColumn, btnColumn, toPathColumn);
    }
}
