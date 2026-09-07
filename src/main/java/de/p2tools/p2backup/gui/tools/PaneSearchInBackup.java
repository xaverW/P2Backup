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
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.gui.dialog.DialogCopyFileController;
import de.p2tools.p2backup.gui.table.Table;
import de.p2tools.p2backup.gui.table.TableToolSearchInBackup;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Open;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Predicate;

public class PaneSearchInBackup extends HBox {

    private final ObservableList<String> foundDirList = FXCollections.observableArrayList(); // Tree
    private final FileDataList foundFileList = new FileDataList();
    private final IntegerProperty sizeProp = new SimpleIntegerProperty(0); // Anzahl der Trees
    private final ArrayList<String> backupInfoFromPathList = new ArrayList<>(); // Liste der FROM-Paths

    private final TableToolSearchInBackup tableViewFile;
    private final TreeView<String> treeView = new TreeView<>();

    private final ObjectProperty<BackupInfo> backupInfoProp = new SimpleObjectProperty<>(null);
    private final ObjectProperty<BackupData> backupDataProp;
    private final ProgData progData;
    private final ObjectProperty<Stage> stage;
    private final VBox vBoxTable = new VBox();
    private final VBox vBoxTree = new VBox();

    public PaneSearchInBackup(ObjectProperty<Stage> stage, BackupInfo backupInfo, ObjectProperty<BackupData> backupDataProp) {
        this.progData = ProgData.getInstance();
        this.backupInfoProp.set(backupInfo);
        this.backupDataProp = backupDataProp;
        this.stage = stage;

        this.tableViewFile = new TableToolSearchInBackup(Table.TABLE_ENUM.SHOW_BACKUP_FILES,
                stage,
                backupInfoProp, backupDataProp);
        make();
    }

    private void make() {
        initTable();
        initTree();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(vBoxTree, vBoxTable);
        splitPane.getDividers().getFirst().positionProperty().bindBidirectional(ProgConfig.SHOW_BACKUP_SPLIT_DIVIDER);
        SplitPane.setResizableWithParent(treeView, false);
        getChildren().add(splitPane);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
    }

    public void close() {
        Table.saveTable(tableViewFile, Table.TABLE_ENUM.SHOW_BACKUP_FILES);
        backupInfoProp.get().runnerDto.setStop();
    }

    // ===============
    // Tree
    public IntegerProperty getSizeProp() {
        return sizeProp;
    }

    public void clearTree() {
        foundDirList.clear();
        foundFileList.clear();
        treeView.setRoot(new TreeItem<>(backupInfoProp.get().getName()));
    }

    public void makeTree(FileDataList fileDataList) {
        if (backupInfoProp.get() == null || backupDataProp.get() == null) {
            return;
        }

        clearTree();
        HashSet<String> dirHashSet = new HashSet<>();
        FileDataList tmpFileList = new FileDataList();
        fileDataList.forEach(f -> {
            String dir = f.getParentFilePathStr();
            if (!dir.isEmpty()) {
                // dann ist was faul??
                dirHashSet.add(dir);
                tmpFileList.add(f);
            }
        });
        this.foundDirList.setAll(dirHashSet.stream().toList());
        this.foundDirList.sort(Comparator.naturalOrder());

        tmpFileList.sort(Comparator.nullsFirst(
                Comparator.comparing(FileData::getFileNameStr, String.CASE_INSENSITIVE_ORDER)));
        this.foundFileList.getFilteredList().setPredicate(p -> Boolean.FALSE);
        this.foundFileList.setAll(tmpFileList);

        treeView.setRoot(new TreeItem<>(backupInfoProp.get().getName()));

        backupInfoFromPathList.clear();
        backupInfoProp.get().getPathListFrom().forEach(pathData -> {
            String path = pathData.getPath();
            backupInfoFromPathList.add(path);
            TreeItem<String> treeItemPath = new TreeItem<>(path);
            treeView.getRoot().getChildren().add(treeItemPath);
            addSubNode(treeItemPath, path);
        });
        treeView.getRoot().setExpanded(true);
        treeView.getSelectionModel().select(treeView.getRoot());
    }

    private void addSubNode(TreeItem<String> start, String subDir) {
        HashSet<String> subList = new HashSet<>();
        foundDirList.forEach(s -> {
            final String sPath = subDir + File.separator; // damit auch wirklich ein DIR
            if (!s.equals(subDir) && s.startsWith(sPath)) {
                // dann ein SubDir
                String sub = s.replace(subDir, "");
                if (sub.startsWith(File.separator)) {
                    sub = sub.substring(1);
                }
                if (sub.contains(File.separator)) {
                    sub = sub.substring(0, sub.indexOf(File.separator));
                }
                subList.add(sub);
            }
        });
        subList.forEach(s -> {
            String sub = Path.of(subDir, s).toString();
            TreeItem<String> tree = new TreeItem<>(sub);
            start.getChildren().add(tree);
            addSubNode(tree, sub);
        });
    }

    private void initTable() {
        final Label lblPath = new Label("");
        final Label lblFilePath = new Label("");
        final Button btnOpenDirectory = new Button();
        btnOpenDirectory.getStyleClass().addAll("buttonVeryLow");
        btnOpenDirectory.setTooltip(new Tooltip("Ordner mit der Datei öffnen"));
        btnOpenDirectory.setGraphic(P2IconFactory.P2ICON.BTN_OPEN_DIR.getFontIcon());
        btnOpenDirectory.setOnAction(a -> {
            FileData fileData = tableViewFile.getSelectionModel().getSelectedItem();
            if (fileData == null) {
                return;
            }
            Path path = fileData.getParentBackupFilePath();
            if (path != null && path.toFile().exists() && path.toFile().isDirectory()) {
                P2Open.openDir(stage.get(), path.toFile().toString());
            }
        });
        btnOpenDirectory.visibleProperty().bind(lblFilePath.textProperty().isEmpty().not());

        Table.setTable(tableViewFile);
        tableViewFile.setItems(foundFileList.getSortedList());
        foundFileList.getSortedList().comparatorProperty().bind(tableViewFile.comparatorProperty());

        tableViewFile.setOnMousePressed(m -> {
            if (m.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu contextMenu = getContextMenuFile();
                tableViewFile.setContextMenu(contextMenu);
            }
        });
        tableViewFile.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            FileData fileData = tableViewFile.getSelectionModel().getSelectedItem();
            if (fileData == null) {
                lblFilePath.setText("");
            } else {
                String path = fileData.getCorrFilePathStr();
                lblFilePath.setText(path);
            }
        });


        HBox hBoxPath = new HBox();
        hBoxPath.setPadding(new Insets(5, 5, 5, 5));
        hBoxPath.getChildren().addAll(P2Text.getLblTextBold("Backup-Ordner:  "), lblPath);

        HBox hBoxFilePath = new HBox();
        hBoxFilePath.setPadding(new Insets(5, 5, 5, 5));
        hBoxFilePath.getChildren().addAll(P2Text.getLblTextBold("Datei:   "), lblFilePath,
                P2GuiTools.getHBoxGrower(), btnOpenDirectory);

        vBoxTable.getChildren().addAll(tableViewFile, hBoxPath, hBoxFilePath);
        VBox.setVgrow(tableViewFile, Priority.ALWAYS);
    }

    private ContextMenu getContextMenuFile() {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem resetTable = new MenuItem("Tabelle zurücksetzen");
        resetTable.setOnAction(e -> tableViewFile.resetTable());
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().addAll(resetTable);
        return contextMenu;
    }

    private void initTree() {
        final Label lblPath = new Label("");

        final Button btnOpenDirectory = new Button();
        btnOpenDirectory.getStyleClass().addAll("buttonVeryLow");
        btnOpenDirectory.setTooltip(new Tooltip("Ordner mit der Datei öffnen"));
        btnOpenDirectory.setGraphic(P2IconFactory.P2ICON.BTN_COPY.getFontIcon());
        btnOpenDirectory.setOnAction(a -> {
            TreeItem<String> tree = treeView.getSelectionModel().getSelectedItem();
            if (tree == null) {
                return;
            }
            String pStr = tree.getValue();
            if (pStr.isEmpty()) {
                return;
            }

            pStr = FileFactory.cleanFileData(pStr); // Pfade anpassen
            pStr = FileFactory.setCorrPath(pStr); // Pfade anpassen

            Path path = Path.of(backupDataProp.getValue().getToPathStr(backupInfoProp.get()), pStr);
            if (!path.toFile().exists() || !path.toFile().isDirectory()) {
                return;
            }

            new DialogCopyFileController(stage.get(), path.toString(), false);
        });
        btnOpenDirectory.visibleProperty().bind(lblPath.textProperty().isEmpty().not());

        treeView.setRoot(new TreeItem<>(backupInfoProp.get().getName()));
        treeView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                expandTreeView(treeView.getRoot(), !treeView.getRoot().isExpanded());
            }
        });
        treeView.setCellFactory(new TreeViewTreeCellCallback());
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();
            if (treeItem != null && !treeItem.getValue().isEmpty()) {
                if (backupDataProp.get() != null) {
                    String treeFileData = treeItem.getValue();
                    if (treeFileData.contains(File.separator)) {
                        final String path = treeFileData.substring(treeFileData.lastIndexOf(File.separator) + 1);
                        lblPath.setText(path);
                    } else {
                        lblPath.setText("");
                    }

                    if (!treeView.getRoot().equals(treeItem)) {
                        Predicate<FileData> pr = f -> {
                            String path = f.getParentFilePathStr();
                            return path.equals(treeFileData);
                        };
                        foundFileList.getFilteredList().setPredicate(pr);
                    } else {
                        foundFileList.getFilteredList().setPredicate(p -> Boolean.TRUE);
                    }
                }
            } else {
                lblPath.setText("");
            }
            sizeProp.set(foundFileList.getFilteredList().size());
        });

        HBox hBoxPath = new HBox();
        hBoxPath.setPadding(new Insets(5, 5, 5, 5));
        hBoxPath.getChildren().addAll(P2Text.getLblTextBold("Ordner kopieren:  "),
                lblPath, P2GuiTools.getHBoxGrower(), btnOpenDirectory);

        vBoxTree.getChildren().addAll(treeView, hBoxPath);
        VBox.setVgrow(treeView, Priority.ALWAYS);
    }

    private void expandTreeView(TreeItem<?> item, boolean expand) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(expand);
            for (TreeItem<?> child : item.getChildren()) {
                expandTreeView(child, expand);
            }
        }
    }

    private class TreeViewTreeCellCallback implements Callback<TreeView<String>, TreeCell<String>> {
        @Override
        public TreeCell<String> call(TreeView<String> param) {
            return new TreeCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (backupInfoFromPathList.contains(item)) {
                        // ist ein FROM-Pfad
                        setText(FileFactory.setCorrPath(item));
                        return;
                    }

                    if (item.endsWith(File.separator)) {
                        item = item.substring(0, item.length() - 1);
                    }
                    if (item.contains(File.separator)) {
                        // dann nur den letzten SUB anzeigen
                        String sub = item.substring(item.lastIndexOf(File.separator) + 1);
                        setText(sub);

                    } else {
                        // dann ist nur noch ein SUB
                        setText(item);
                    }
                }
            };
        }
    }
}