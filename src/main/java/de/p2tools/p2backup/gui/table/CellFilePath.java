/*
 * P2tools Copyright (C) 2022 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de/
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
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.io.File;

public class CellFilePath<S, T> extends TableCell<S, T> {

    private final ObjectProperty<BackupInfo> backupInfoProps;
    private final ObjectProperty<BackupData> backupDataProps;

    public CellFilePath(ObjectProperty<BackupInfo> backupInfoProps,
                        ObjectProperty<BackupData> backupDataProp) {
        this.backupInfoProps = backupInfoProps;
        this.backupDataProps = backupDataProp;
    }

    public final Callback<TableColumn<FileData, String>, TableCell<FileData, String>> cellFactory
            = (final TableColumn<FileData, String> param) -> {

        final TableCell<FileData, String> cell = new TableCell<>() {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                FileData fileData = getTableView().getItems().get(getIndex());
                String filePath = fileData.getFilePathStr();
                filePath = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
                setText(filePath);
                setGraphic(null);
            }
        };
        return cell;
    };
}