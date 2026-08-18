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

import de.p2tools.p2backup.controller.data.resetdata.CopyBackData;
import de.p2tools.p2lib.guitools.ptable.P2TableFactory;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class TableToolCopyBackBackup extends PTable<CopyBackData> {
    private final ObjectProperty<Stage> stage;

    public TableToolCopyBackBackup(Table.TABLE_ENUM table_enum, ObjectProperty<Stage> stage) {
        super(table_enum);
        this.table_enum = table_enum;
        this.stage = stage;

        initFileRunnerColumn();
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

        final TableColumn<CopyBackData, LocalDateTime> backupDataIdColumn = new TableColumn<>("Backup");
        backupDataIdColumn.setCellValueFactory(new PropertyValueFactory<>("backupSubPath"));

        final TableColumn<CopyBackData, LocalDateTime> fileNameColumn = new TableColumn<>("Datei");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        final TableColumn<CopyBackData, String> btnColumn = new TableColumn<>("Öffnen");
        btnColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        btnColumn.setCellFactory(new CellStartOpenCopyBackDataButton<>(stage).cellFactory);

        final TableColumn<CopyBackData, String> copyColumn = new TableColumn<>("Kopieren");
        copyColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        copyColumn.setCellFactory(new CellCopyFileButton<>(stage).cellCopyBackFactory);

        backupDataIdColumn.setPrefWidth(200);
        fileNameColumn.setPrefWidth(500);
        btnColumn.setPrefWidth(150);

        getColumns().addAll(backupDataIdColumn, fileNameColumn, btnColumn, copyColumn);
    }
}
