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

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class CellPathButton<S, T> extends TableCell<S, T> {

    public static int FROM = 0;
    public static int EXCLUDE_DIR = 1;
    public static int EXCLUDE_FILE = 2;

    private final int what; // 0: fromList, 1: excludeDir, 2: excludeFile
    private final ObjectProperty<BackupInfo> backupDataProps;

    public CellPathButton(ObjectProperty<BackupInfo> backupInfosObjectProperty, int what) {
        this.backupDataProps = backupInfosObjectProperty;
        this.what = what;
    }

    public final Callback<TableColumn<PathData, String>, TableCell<PathData, String>> cellFactory
            = (final TableColumn<PathData, String> param) -> {

        final TableCell<PathData, String> cell = new TableCell<>() {

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

                PathData pathData = getTableView().getItems().get(getIndex());
                final Button btnDel;
                btnDel = new Button("");
                btnDel.setTooltip(new Tooltip("Pfad löschen"));
                btnDel.getStyleClass().addAll("btnFunction", "btnFuncTable");
                btnDel.setGraphic(PIconFactory.PICON.TABLE_FILE_DEL.getFontIcon());
                btnDel.setOnAction(a -> {
                    if (what == FROM) {
                        backupDataProps.get().getPathListFrom().remove(pathData);
                    } else if (what == EXCLUDE_DIR) {
                        backupDataProps.get().getPathListExcludeDir().remove(pathData);
                    } else {
                        backupDataProps.get().getPathListExcludeFile().remove(pathData);
                    }
                    getTableView().refresh();
                    getTableView().requestFocus();
                });

                btnDel.setMaxHeight(18);
                btnDel.setMinHeight(18);

                hbox.getChildren().addAll(btnDel);
                setGraphic(hbox);
            }
        };
        return cell;
    };
}