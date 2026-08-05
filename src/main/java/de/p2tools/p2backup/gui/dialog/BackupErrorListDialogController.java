/*
 * MTViewer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
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

package de.p2tools.p2backup.gui.dialog;


import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableBackupError;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BackupErrorListDialogController extends P2DialogExtra {

    public enum ERROR {CANCEL, REPAIR, IGNORE}

    private final Button btnRepair = new Button("Reparieren");
    private final Button btnCancel = new Button("Abbrechen");
    private final Button btnIgnore = new Button("Ignorieren");
    private final BackupInfo backupInfo;
    private final FileDataList errorList;
    private final ObjectProperty<ERROR> errorEnum;
    private TableBackupError tableView;
    private final ObjectProperty<Stage> stageProp = new SimpleObjectProperty<>();

    public BackupErrorListDialogController(BackupInfo backupInfo, FileDataList errorList, ObjectProperty<ERROR> errorEnum) {
        super(ProgData.getInstance().primaryStage, ProgConfig.BACKUP_ERROR_DIALOG_SIZE, "Backup kontrollieren",
                true, true, false, DECO.NO_BORDER);

        this.backupInfo = backupInfo;
        this.errorList = errorList;
        this.errorEnum = errorEnum;
        this.tableView = new TableBackupError(Table.TABLE_ENUM.BACKUP_ERROR, stageProp);
        initTable();
        init(true);
        stageProp.set(getStage());
    }

    @Override
    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.BACKUP_ERROR);
        super.close();
    }

    @Override
    public void make() {
        Button btnHelp;
        btnHelp = PIconFactory.getHelpButton(getStage(), "Datei kopieren",
                "Dateien im Backup sind verändert oder fehlen. Das Backup kann " +
                        "abgebrochen werden oder die Dateien können aus dem Backup gelöscht werden. " +
                        "Ansonsten kann mit einem komplett neuen Backup wieder begonnen werden.");
        Text text;
        text = P2Text.getTextBold("Dateien im Backup fehlen oder sind verändert");
        HBox hBoxText = new HBox();
        hBoxText.setAlignment(Pos.CENTER);
        hBoxText.getChildren().add(text);
        hBoxText.getStyleClass().add("infoDialogTop");

        VBox vBoxTable = new VBox(5);
        vBoxTable.setAlignment(Pos.CENTER_LEFT);
        vBoxTable.getChildren().addAll(tableView);
        tableView.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Node errorNode = PIconFactory.getAttentionIcon("gmi-error-outline");

        HBox hBoxCenter = new HBox(10);
        hBoxCenter.getChildren().addAll(errorNode, vBoxTable);
        HBox.setHgrow(vBoxTable, Priority.ALWAYS);
        VBox.setVgrow(hBoxCenter, Priority.ALWAYS);

        Label lblRepair = P2Text.getLblTextBold("Backup reparieren, Fehler aus dem Backup löschen");
        Label lblCancel = P2Text.getLblTextBold("Backup abbrechen");
        Label lblIgnore = P2Text.getLblTextBold("Fehler ignorieren und ein Vollbackup machen");

        HBox hBoxRepair = new HBox(P2LibConst.SPACING_HBOX);
        hBoxRepair.setAlignment(Pos.CENTER_RIGHT);
        hBoxRepair.getChildren().addAll(lblRepair, P2GuiTools.getHBoxGrower(), btnHelp, btnRepair);

        HBox hBoxCancel = new HBox(P2LibConst.SPACING_HBOX);
        hBoxCancel.setAlignment(Pos.CENTER_RIGHT);
        hBoxCancel.getChildren().addAll(lblCancel, P2GuiTools.getHBoxGrower(), btnCancel);

        HBox hBoxIgnore = new HBox(P2LibConst.SPACING_HBOX);
        hBoxIgnore.setAlignment(Pos.CENTER_RIGHT);
        hBoxIgnore.getChildren().addAll(lblIgnore, P2GuiTools.getHBoxGrower(), btnIgnore);

        getVBoxCont().getChildren().addAll(hBoxText, P2GuiTools.getVDistance(5),
                hBoxCenter, P2GuiTools.getVDistance(50), hBoxRepair, hBoxIgnore, hBoxCancel);

        btnRepair.setOnAction(a -> {
            errorEnum.set(ERROR.REPAIR);
            close();
        });
        btnCancel.setOnAction(a -> {
            errorEnum.set(ERROR.CANCEL);
            close();
        });
        btnIgnore.setOnAction(a -> {
            errorEnum.set(ERROR.IGNORE);
            close();
        });
    }

    private void initTable() {
        tableView.setItems(errorList.getSortedList());
        Table.setTable(tableView);
        tableView.setOnMousePressed(m -> {
            if (m.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu contextMenu = getContextMenu();
                tableView.setContextMenu(contextMenu);
            }
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            FileData f = tableView.getSelectionModel().getSelectedItem();
            if (f == null) {
            } else {
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
}
