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

import de.p2tools.p2backup.controller.data.filedata.HistoryFileData;
import de.p2tools.p2lib.guitools.ptable.P2TableFactory;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class TableToolFileHistory extends PTable<HistoryFileData> {
    private final Stage stage;

    public TableToolFileHistory(Table.TABLE_ENUM table_enum, Stage stage) {
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

        final TableColumn<HistoryFileData, LocalDateTime> startDateColumn = new TableColumn<>("Datum");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setCellFactory(new CellBackupInfoStartDate().cellFactory);

//        final TableColumn<HistoryFileData, String> nameColumn = new TableColumn<>("Dateiname");
//        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileNameStr"));
//
//        final TableColumn<HistoryFileData, String> pathColumn = new TableColumn<>("Pfad");
//        pathColumn.setCellValueFactory(new PropertyValueFactory<>("corrParentFilePathStr"));

        final TableColumn<HistoryFileData, String> btnColumn = new TableColumn<>("Öffnen");
        btnColumn.setCellValueFactory(new PropertyValueFactory<>("toPathStr"));
        btnColumn.setCellFactory(new CellStartOpenHistoryFileButton<>(stage).cellFactory);

        startDateColumn.setPrefWidth(200);
//        nameColumn.setPrefWidth(200);
//        pathColumn.setPrefWidth(500);
        btnColumn.setPrefWidth(150);
        getColumns().addAll(startDateColumn, /*nameColumn, pathColumn,*/ btnColumn);
    }
}
