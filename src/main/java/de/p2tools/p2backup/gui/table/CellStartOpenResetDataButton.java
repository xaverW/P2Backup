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

import de.p2tools.p2backup.controller.data.resetdata.ResetData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.guitools.P2Open;
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

public class CellStartOpenResetDataButton<S, T> extends TableCell<S, T> {

    private final ObjectProperty<Stage> stage;

    public CellStartOpenResetDataButton(ObjectProperty<Stage> stage) {
        this.stage = stage;
    }

    public final Callback<TableColumn<ResetData, String>, TableCell<ResetData, String>> cellFactory
            = (final TableColumn<ResetData, String> param) -> {

        final TableCell<ResetData, String> cell = new TableCell<>() {

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

                ResetData resetData = getTableView().getItems().get(getIndex());

                final Button btnStart = new Button("");
                btnStart.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnStart.setTooltip(new Tooltip("Gespeicherte Datei öffnen"));
                btnStart.setGraphic(PIconFactory.PICON.TABLE_START.getFontIcon());

                btnStart.setOnAction((ActionEvent event) -> {
                    getTableView().getSelectionModel().clearSelection();
                    getTableView().getSelectionModel().select(getIndex());
                    P2Open.openFile(stage.get(), resetData.getFileData().getBackupFilePathStr());
                    getTableView().refresh();
                    getTableView().requestFocus();
                });


                final Button btnOpenDirectory;
                btnOpenDirectory = new Button();
                btnOpenDirectory.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnOpenDirectory.setTooltip(new Tooltip("Ordner mit der Datei öffnen"));
                btnOpenDirectory.setGraphic(PIconFactory.PICON.TABLE_DIR_OPEN.getFontIcon());

                btnOpenDirectory.setOnAction((ActionEvent event) -> {
                    getTableView().getSelectionModel().clearSelection();
                    getTableView().getSelectionModel().select(getIndex());
                    Path path = resetData.getFileData().getParentBackupFilePath();
                    if (path != null && path.toFile().exists() && path.toFile().isDirectory()) {
                        P2Open.openDir(stage.get(), path.toFile().toString());
                    }

                    getTableView().refresh();
                    getTableView().requestFocus();
                });

                btnStart.setMinHeight(18);
                btnStart.setMaxHeight(18);
                btnOpenDirectory.setMinHeight(18);
                btnOpenDirectory.setMaxHeight(18);
                hbox.getChildren().addAll(btnStart, btnOpenDirectory);
                setGraphic(hbox);
            }
        };
        return cell;
    };
}