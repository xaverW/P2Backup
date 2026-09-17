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

package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolBackupInfo;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class BackupInfoController extends VBox {

    private final ProgData progData;
    private final TextField txtName = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private final TextArea taDescription = new TextArea();
    private final TableToolBackupInfo tableView;
    private final Label lblInfoListSize = new Label();

    private BackupInfo backupInfo = null;
    private final VBox vBoxContent = new VBox();
    private final EventHandler eventHandler = new EventHandler() {
        @Override
        public void handle(Event event) {
            if (backupInfo != null) {
                Color color = colorPicker.getValue();
                backupInfo.setColor(color.toString());
            }
        }
    };


    public BackupInfoController() {
        progData = ProgData.getInstance();
        tableView = new TableToolBackupInfo(Table.TABLE_ENUM.BACKUP_INFO, progData.primaryStage, progData.backupInfoProperty);

        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);
        getChildren().addAll(new TitleBox());
        getChildren().add(vBoxContent);

        init();
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.BACKUP_INFO);
    }

    private void init() {
        colorPicker.getStyleClass().add("split-button");
        colorPicker.setMinHeight(Region.USE_PREF_SIZE);

        progData.backupInfoProperty.addListener((u, o, n) -> {
            vBoxContent.disableProperty().unbind();
            vBoxContent.setDisable(progData.backupInfoProperty.get() == null);
            if (progData.backupInfoProperty.get() != null) {
                vBoxContent.disableProperty().bind(progData.backupInfoProperty.get().runnerDto.guiRunningProperty());
            }
        });

        taDescription.setWrapText(true);
        taDescription.setMaxHeight(80);

        txtName.setStyle("-fx-border-color: red;");
        txtName.textProperty().addListener((u, o, n) -> {
            if (progData.backupInfoProperty.get() != null) {
                if (txtName.getText().isEmpty()) {
                    txtName.setStyle("-fx-border-color: red;");
                } else {
                    txtName.setStyle(null);
                }
            } else {
                txtName.setStyle(null);
            }
        });

        progData.backupInfoProperty.addListener((u, o, n) -> {
            setBackup();
        });
        setBackup();


        GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);

        int row = 0;
        gridPane.add(new Label("Name:"), 0, row);
        gridPane.add(txtName, 1, row);
        gridPane.add(colorPicker, 2, row);
        gridPane.add(new Label("Beschreibung"), 0, ++row);
        gridPane.add(taDescription, 1, row, 2, 1);

        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow(), P2GridConstraints.getCcPrefSize());

        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Beschreibung des Backup"), gridPane);

        // ====================
        // Table
        Table.setTable(tableView);
        setTableItems();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        vBoxContent.getChildren().addAll(P2GuiTools.getHDistance(20),
                BackupGuiFactory.getInfoPane("Gespeicherte Backups"), tableView);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnHelp = P2IconFactory.getHelpButton("Backup Infos",
                "Hier sind Infos zu dem oben ausgewählten Backup." +
                        "\n\n" +
                        "Es kann der Name des Backups festgelegt werden. Die Beschreibung " +
                        "dient für eigene Infos zum Backup." +
                        "\n\n" +
                        "Mit der Farbe kann man in der Backupliste (erster Tab: \"Backup\") " +
                        "eine Farbe dafür setzen.");
        hBox.getChildren().add(btnHelp);
        vBoxContent.getChildren().add(hBox);
    }

    private void setTableItems() {
        if (backupInfo != null) {
            FilteredList<BackupData> filteredList =
                    new FilteredList<>(backupInfo.getBackupDataList());
            SortedList<BackupData> sortedList = new SortedList<>(filteredList);
            tableView.setItems(sortedList);
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            lblInfoListSize.textProperty().bind(backupInfo.getBackupDataList().sizeProperty().asString());

        } else {
            tableView.setItems(null);
            lblInfoListSize.textProperty().unbind();
            lblInfoListSize.setText("");
        }

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

    private void setBackup() {
        if (backupInfo != null) {
            txtName.textProperty().unbindBidirectional(backupInfo.nameProperty());
            taDescription.textProperty().unbindBidirectional(backupInfo.descriptionProperty());
            backupInfo = null;
            setTableItems();
            colorPicker.setOnAction(null);
        }

        if (progData.backupInfoProperty.get() != null) {
            backupInfo = progData.backupInfoProperty.get();

            txtName.textProperty().bindBidirectional(backupInfo.nameProperty());
            taDescription.textProperty().bindBidirectional(backupInfo.descriptionProperty());
            setTableItems();

            Color c = Color.web(backupInfo.getColor());
            colorPicker.setValue(c);
            colorPicker.setOnAction(eventHandler);
        }
        colorPicker.setDisable(backupInfo == null);
        txtName.requestFocus();
    }
}
