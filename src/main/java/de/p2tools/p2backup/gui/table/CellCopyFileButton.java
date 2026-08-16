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

import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.HistoryFileData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.dialog.DialogCopyFileController;
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

public class CellCopyFileButton<S, T> extends TableCell<S, T> {

    private final ObjectProperty<Stage> stage;

    public CellCopyFileButton(ObjectProperty<Stage> stage) {
        this.stage = stage;
    }

    public final Callback<TableColumn<FileData, String>, TableCell<FileData, String>> cellFileFactory
            = (final TableColumn<FileData, String> param) -> {

        final TableCell<FileData, String> cell = new TableCell<>() {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                final HBox hbox = new HBox();
                hbox.setSpacing(5);
                hbox.setAlignment(Pos.CENTER);
                hbox.setPadding(new Insets(0, 2, 0, 2));

                FileData fileData = getTableView().getItems().get(getIndex());

                final Button btnCopy = new Button("");
                btnCopy.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnCopy.setTooltip(new Tooltip("Gespeicherte Datei kopieren"));
                btnCopy.setGraphic(PIconFactory.PICON.TABLE_COPY.getFontIcon());
                btnCopy.setDisable(fileData.isErrorHash() || fileData.isOnlyInData());

                btnCopy.setOnAction((ActionEvent event) -> {
                    getTableView().getSelectionModel().clearSelection();
                    getTableView().getSelectionModel().select(getIndex());
                    new DialogCopyFileController(stage.get(), fileData.getBackupFilePathStr(), true);
                    getTableView().refresh();
                    getTableView().requestFocus();
                });

                btnCopy.setMinHeight(18);
                btnCopy.setMaxHeight(18);
                hbox.getChildren().addAll(btnCopy);
                setGraphic(hbox);
            }
        };
        return cell;
    };

    public final Callback<TableColumn<HistoryFileData, String>, TableCell<HistoryFileData, String>> cellHistoryFactory
            = (final TableColumn<HistoryFileData, String> param) -> {

        final TableCell<HistoryFileData, String> cell = new TableCell<>() {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                final HBox hbox = new HBox();
                hbox.setSpacing(5);
                hbox.setAlignment(Pos.CENTER);
                hbox.setPadding(new Insets(0, 2, 0, 2));

                HistoryFileData fileData = getTableView().getItems().get(getIndex());

                final Button btnCopy = new Button("");
                btnCopy.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnCopy.setTooltip(new Tooltip("Gespeicherte Datei kopieren"));
                btnCopy.setGraphic(PIconFactory.PICON.TABLE_COPY.getFontIcon());
                btnCopy.setDisable(fileData.isErrorHash());

                btnCopy.setOnAction((ActionEvent event) -> {
                    getTableView().getSelectionModel().clearSelection();
                    getTableView().getSelectionModel().select(getIndex());
                    new DialogCopyFileController(stage.get(), fileData.getBackupFilePathStr(), true);
                    getTableView().refresh();
                    getTableView().requestFocus();
                });

                btnCopy.setMinHeight(18);
                btnCopy.setMaxHeight(18);
                hbox.getChildren().addAll(btnCopy);
                setGraphic(hbox);
            }
        };
        return cell;
    };
}