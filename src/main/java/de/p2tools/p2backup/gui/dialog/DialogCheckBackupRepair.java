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
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableBackupError;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DialogCheckBackupRepair extends P2DialogExtra {

    private final Button btnRepair = new Button("Dateien löschen");
    private final Button btnCancel = new Button("Abbrechen");
    private final FileDataList errorList;
    private TableBackupError tableView;
    private final ObjectProperty<Stage> stageProp = new SimpleObjectProperty<>();
    private boolean ok = false;

    public DialogCheckBackupRepair(Stage stage, FileDataList errorList) {
        super(stage, ProgConfig.BACKUP_ERROR_DIALOG_SIZE, "Backup prüfen",
                true, true, false, DECO.NO_BORDER);

        this.errorList = errorList;
        this.tableView = new TableBackupError(Table.TABLE_ENUM.BACKUP_ERROR, stageProp);
        initTable();
        init(true);
        stageProp.set(getStage());
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.BACKUP_ERROR);
        super.close();
    }

    @Override
    public void make() {
        Button btnHelp;
        btnHelp = P2IconFactory.getHelpButton(getStage(), "Backup reparieren",
                "Dateien im Backup sind verändert, fehlen oder sind zuviel. Das Backup kann " +
                        "repariert werden, die Dateien werden dann im Backup gelöscht.");
        Text text;
        text = P2Text.getTextBold("Dateien im Backup fehlerhaft");
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

        getVBoxCont().getChildren().addAll(hBoxText, P2GuiTools.getVDistance(5), hBoxCenter);
        getHboxLeft().getChildren().add(btnHelp);
        addOkCancelButtons(btnRepair, btnCancel);
        btnRepair.setOnAction(a -> {
            ok = true;
            close();
        });
        btnCancel.setOnAction(a -> {
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
