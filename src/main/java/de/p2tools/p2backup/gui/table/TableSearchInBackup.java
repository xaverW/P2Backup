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
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2lib.guitools.ptable.P2TableFactory;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TableSearchInBackup extends PTable<FileData> {

    private final Stage stage;
    private final ObjectProperty<BackupInfo> backupInfoProp;
    private final ObjectProperty<BackupData> backupDataProp;

    public TableSearchInBackup(Table.TABLE_ENUM table_enum, Stage stage,
                               ObjectProperty<BackupInfo> backupInfoProp,
                               ObjectProperty<BackupData> backupDataProp) {
        super(table_enum);
        this.table_enum = table_enum;
        this.stage = stage;
        this.backupInfoProp = backupInfoProp;
        this.backupDataProp = backupDataProp;

        initFileRunnerColumn();
    }

    @Override
    public Table.TABLE_ENUM getETable() {
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

        final TableColumn<FileData, String> pathFileColumn = new TableColumn<>("Dateiname");
        pathFileColumn.setCellValueFactory(new PropertyValueFactory<>("fileNameStr"));

        final TableColumn<FileData, Boolean> errorColumn = new TableColumn<>("Fehler");
        errorColumn.setCellValueFactory(new PropertyValueFactory<>("error"));
        TableFactory.columnFactoryBoolean(errorColumn);

        final TableColumn<FileData, String> btnColumn = new TableColumn<>("Öffnen");
        btnColumn.setCellValueFactory(new PropertyValueFactory<>("toPathStr"));
        btnColumn.setCellFactory(new CellStartOpenFileButton<>(stage).cellFactory);

        pathFileColumn.setPrefWidth(500);
        btnColumn.setPrefWidth(150);
        getColumns().addAll(pathFileColumn, errorColumn, btnColumn);
    }
}
