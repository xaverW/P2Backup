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
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.deleterunner.DeleteRunner;
import de.p2tools.p2lib.guitools.P2Open;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.nio.file.Path;

public class CellOpenDelButton<S, T> extends TableCell<S, T> {

    private final Stage stage;
    private final ObjectProperty<BackupInfo> backupInfoProps;

    public CellOpenDelButton(Stage stage, ObjectProperty<BackupInfo> backupInfoProps) {
        this.stage = stage;
        this.backupInfoProps = backupInfoProps;
    }

    public final Callback<TableColumn<BackupData, String>, TableCell<BackupData, String>> cellFactory
            = (final TableColumn<BackupData, String> param) -> {

        final TableCell<BackupData, String> cell = new TableCell<>() {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                final HBox hbox = new HBox();
                hbox.setSpacing(4);
                hbox.setAlignment(Pos.CENTER);
                hbox.setPadding(new Insets(0, 2, 0, 2));

                BackupData backupData = getTableView().getItems().get(getIndex());
                Path path = FileFactory.getToPath(backupInfoProps.get(), backupData);

                final Button btnOpenDirectory;
                btnOpenDirectory = new Button();
                btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnOpenDirectory.setTooltip(new Tooltip("Ordner öffnen"));
                btnOpenDirectory.setGraphic(P2IconFactory.P2ICON.P2_BTN_OPEN_DIR.getFontIcon());
                btnOpenDirectory.setOnAction((ActionEvent event) -> {
                    getTableView().getSelectionModel().clearSelection();
                    getTableView().getSelectionModel().select(getIndex());
                    if (path.toFile().exists() && path.toFile().isDirectory()) {
                        P2Open.openDir(stage, path.toFile().toString());
                    }
                    getTableView().refresh();
                    getTableView().requestFocus();
                });
                btnOpenDirectory.disableProperty().bind(backupData.subPathProperty().isEmpty());
                btnOpenDirectory.setMaxHeight(18);
                btnOpenDirectory.setMinHeight(18);

                final Button btnDel;
                btnDel = new Button("");
                btnDel.setTooltip(new Tooltip("Backup löschen"));
                btnDel.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnDel.setGraphic(P2IconFactory.P2ICON.BTN_CLEAR.getFontIcon());
                btnDel.setOnAction(a -> {
                    new DeleteRunner(backupInfoProps.get(), backupData).deleteBackup();
                    getTableView().refresh();
                    getTableView().requestFocus();
                });
                btnDel.disableProperty().bind(backupData.subPathProperty().isEmpty());
                btnDel.setMaxHeight(18);
                btnDel.setMinHeight(18);


                hbox.getChildren().addAll(btnOpenDirectory, btnDel);
                setGraphic(hbox);
            }
        };
        return cell;
    };
}