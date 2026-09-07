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

package de.p2tools.p2backup.gui.tools;


import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackData;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackDataList;
import de.p2tools.p2backup.controller.runner.copyrunner.CopyBackFactory;
import de.p2tools.p2backup.gui.guibig.PProgressBar;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolCopyBackBackup;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2ComboBoxString;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class DialogCopyBackBackup extends P2DialogExtra {

    private final BackupInfo backupInfo;
    private ObjectProperty<BackupData> backupDataProp = new SimpleObjectProperty<>(null);
    private final ListView<BackupData> listView = new ListView<>();
    private final VBox vBoxList = new VBox(P2LibConst.SPACING_VBOX);
    private final VBox vBoxCont = new VBox(P2LibConst.SPACING_VBOX);
    private final TableToolCopyBackBackup tableView;
    private final Button btnSearch = new Button();
    private final P2ComboBoxString cboDest = new P2ComboBoxString();
    private final Label lblName = new Label();
    private final Label lblSize = new Label();
    private final CopyBackDataList copyBackDataList = new CopyBackDataList();
    private final SortedList<CopyBackData> sortedList = new SortedList<>(new FilteredList<>(copyBackDataList, p -> true));

    private final ProgData progData;


    public DialogCopyBackBackup(BackupInfo backupInfo) {
        super(ProgData.getInstance().primaryStage, ProgConfig.SEARCH_DIALOG_SIZE, "Backup wieder herstellen",
                true, true, true, DECO.NO_BORDER);

        this.progData = ProgData.getInstance();
        this.backupInfo = backupInfo;
        this.tableView = new TableToolCopyBackBackup(Table.TABLE_ENUM.COPY_BACK_BACKUP, getStageProp());

        initTable();
        initList();
        initInfo();
        init(false);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);

        Button btnHelp = P2Button.helpButton(getStage(), "Backup wieder herstellen",
                "Damit kann man ein Backup wieder herstellen. Es werden die Daten aus einem " +
                        "Backup in einen Ordner der eigenen Wahl, kopiert.");

        vBoxList.setPadding(new Insets(5));
        vBoxCont.setPadding(new Insets(5));

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(vBoxList, vBoxCont);
        splitPane.getDividers().getFirst().positionProperty().bindBidirectional(ProgConfig.COPY_BACK_BACKUP_SPLIT_DIVIDER);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getVBoxCont().getChildren().addAll(splitPane);

        HBox hBox = addProgress();
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getHboxLeft().getChildren().addAll(hBox, btnHelp);
    }

    public void close() {
        Table.saveTable(tableView, Table.TABLE_ENUM.COPY_BACK_BACKUP);
        backupInfo.runnerDto.setStop();
        super.close();
    }

//    public void setResult(FileDataList fileDataList) {
//        Platform.runLater(() -> );
//    }

    private void initTable() {
        Table.setTable(tableView);
        tableView.setOnMousePressed(m -> {
            if (m.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu contextMenu = getContextMenu();
                tableView.setContextMenu(contextMenu);
            }
        });
        tableView.setItems(sortedList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
    }

    private ContextMenu getContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem resetTable = new MenuItem("Tabelle zurücksetzen");
        resetTable.setOnAction(e -> tableView.resetTable());
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().addAll(resetTable);
        return contextMenu;
    }

    private void initList() {
        listView.setItems(backupInfo.getBackupDataList().sorted(Comparator.naturalOrder()));
        listView.getSelectionModel().selectLast();
        listView.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            setInfo();
        });
        setInfo();

        HBox hBoxTop = new HBox(P2LibConst.SPACING_HBOX);
        hBoxTop.setAlignment(Pos.CENTER);
        hBoxTop.getStyleClass().add("infoDialogTop");
        hBoxTop.getChildren().add(new Label("Backups"));
        vBoxList.getChildren().addAll(hBoxTop, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private void initInfo() {
        HBox hBoxTop = new HBox(P2LibConst.SPACING_HBOX);
        hBoxTop.getStyleClass().add("infoDialogTop");
        hBoxTop.getChildren().addAll(new Label("Backup:"), lblName, P2GuiTools.getHBoxGrower(),
                new Label("Anzahl:"), lblSize);

        cboDest.init(ProgConfig.CBO_COPY_BACK_DIALOG_DEST_DIR, ProgConfig.COPY_BACK_DIALOG_DEST_DIR);
        cboDest.setMaxWidth(Double.MAX_VALUE);
        btnSearch.setTooltip(new Tooltip("Verzeichnis auswählen"));
        btnSearch.setGraphic(P2IconFactory.P2ICON.BTN_OPEN_DIR.getFontIcon());
        btnSearch.setOnAction(a -> {
            P2DirFileChooser.DirChooser(getStage(), cboDest);
        });

        Button btnStartCopy = new Button("Starten");
        btnStartCopy.setTooltip(new Tooltip("Das Kopieren des Backups starten"));
        btnStartCopy.setOnAction(a -> {
            CopyBackFactory.copyBackBackup(getStage(), backupInfo, copyBackDataList, ProgConfig.COPY_BACK_DIALOG_DEST_DIR.getValueSafe());
        });

        GridPane gridPane = new GridPane(5, 5);
        gridPane.add(new Label("Speicherziel"), 0, 0);
        gridPane.add(cboDest, 1, 0);
        gridPane.add(btnSearch, 2, 0);
        gridPane.add(btnStartCopy, 3, 0);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(), P2GridConstraints.getCcComputedSizeAndHgrowCenter(),
                P2GridConstraints.getCcPrefSize(), P2GridConstraints.getCcPrefSize());
        vBoxCont.getChildren().addAll(hBoxTop, tableView, gridPane);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    private void setInfo() {
        backupDataProp.set(listView.getSelectionModel().getSelectedItem());

        if (backupDataProp.get() == null) {
            lblName.setText("");
        } else {
            lblName.setText(backupDataProp.get().getSubPath());
            CopyBackFactory.getCopyBackDataList(backupInfo, backupDataProp.get(), copyBackDataList);
            lblSize.setText(copyBackDataList.getSize() + "");
        }
    }

    private HBox addProgress() {
        Button btnStop = new Button();
        btnStop.setGraphic(P2IconFactory.P2ICON.BTN_STOP.getFontIcon());
        btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.setPadding(new Insets(0, 10, 0, 10));
        PProgressBar pProgressBar = new PProgressBar(true, true);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);
        hBoxProgress.getChildren().addAll(/*P2GuiTools.getHBoxGrower(),*/ pProgressBar, btnStop);
        hBoxProgress.setAlignment(Pos.CENTER);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}