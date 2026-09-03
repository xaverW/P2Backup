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
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.data.pathdata.PathDataFactory;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.table.CellPathButton;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BackupFromController extends VBox {

    private final ProgData progData;
    private final TableView<PathData> tableViewFrom = new TableView<>();
    private final TableView<PathData> tableViewExcludeDir = new TableView<>();
    private final TableView<PathData> tableViewExcludeFile = new TableView<>();
    private BackupInfo backupInfo = null;
    private final Accordion accordion = new Accordion();
    private final RadioButton rbNot = new RadioButton("Dateien ausschießen");
    private final RadioButton rbOnly = new RadioButton("Nur diese Dateien sichern");

    private final VBox vBoxTableAll = new VBox();
    private final VBox vBoxTableFrom = new VBox(P2LibConst.SPACING_VBOX);
    private final VBox vBoxTableDir = new VBox(P2LibConst.SPACING_VBOX);
    private final VBox vBoxTableFile = new VBox(P2LibConst.SPACING_VBOX);

    private final VBox vBoxTPaneFrom = new VBox();
    private final VBox vBoxTPaneDir = new VBox();
    private final VBox vBoxTPaneFile = new VBox();

    private final VBox vBoxContent = new VBox();

    public BackupFromController() {
        progData = ProgData.getInstance();

        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);
        getChildren().addAll(new TitleBox());
        getChildren().add(vBoxContent);

        init();
    }

    private void init() {
        progData.backupInfoProperty.addListener((u, o, n) -> {
            vBoxContent.disableProperty().unbind();
            vBoxContent.setDisable(progData.backupInfoProperty.get() == null);
            if (progData.backupInfoProperty.get() != null) {
                vBoxContent.disableProperty().bind(progData.backupInfoProperty.get().runnerDto.guiRunningProperty());
            }
        });

        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Was soll gesichert werden?"), vBoxTableAll);
        progData.backupInfoProperty.addListener((u, o, n) -> {
            setBackup();
        });
        setBackup();
        makeVboxFrom();
        makeVboxDir();
        makeVboxFile();
        initAccordion();
        setExtended();
//        ProgConfig.SYSTEM_ENHANCED.addListener((u, o, n) -> setExtended());
        VBox.setVgrow(vBoxTableAll, Priority.ALWAYS);
    }

    private void makeVboxFrom() {
        Button btnPath = new Button();
        btnPath.setGraphic(P2IconFactory.P2ICON.BTN_OPEN_DIR.getFontIcon());
        btnPath.setTooltip(new Tooltip("Den Ordner zum Sichern auswählen"));
        btnPath.setOnAction(event -> {
            PathDataFactory.addPath(backupInfo);
//            String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, ProgConfig.SYSTEM_FROM_PATH.get());
//            if (!path.isEmpty()) {
//                ProgConfig.SYSTEM_FROM_PATH.set(path);
//                backupInfo.getPathListFrom().add(new PathData(path));
//            }
        });
        Button btnHelp = PIconFactory.getHelpButton("Sichern", "Hier können die Ordner die gesichert " +
                "werden sollen, ausgewählt werden.");
        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(new Label("Ordner auswählen, die gesichert werden sollen:"),
                P2GuiTools.getHBoxGrower(), btnPath, btnHelp);

        final TableColumn<PathData, String> pathColumn = new TableColumn<>("Pfad");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        final TableColumn<PathData, String> delColumn = new TableColumn<>("");
        delColumn.setCellFactory(new CellPathButton<>(progData.backupInfoProperty, CellPathButton.FROM).cellFactory);
        delColumn.getStyleClass().add("alignCenter");

        pathColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.8));
        delColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.15));
        tableViewFrom.getColumns().addAll(pathColumn, delColumn);

        vBoxTableFrom.getChildren().addAll(hBox, tableViewFrom);
        VBox.setVgrow(tableViewFrom, Priority.ALWAYS);
    }

    private void makeVboxDir() {
        Button btnPath = new Button();
        btnPath.setGraphic(P2IconFactory.P2ICON.BTN_OPEN_DIR.getFontIcon());
        btnPath.setTooltip(new Tooltip("Ordner die von der Sicherung ausgeschlossen werden sollen"));
        btnPath.setOnAction(event -> {
            String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, "");
            if (!path.isEmpty()) {
                backupInfo.getPathListExcludeDir().add(new PathData(path));
            }
        });
        Button btnHelp = PIconFactory.getHelpButton("Ordner ausschließen",
                "Ordner die vom Backup ausgeschlossen werden sollen, " +
                        "werden hier ausgewählt.");

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(new Label("Ordner auswählen:"),
                P2GuiTools.getHBoxGrower(), btnPath, btnHelp);

        final TableColumn<PathData, String> pathColumn = new TableColumn<>("Pfad");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        final TableColumn<PathData, String> delColumn = new TableColumn<>("");
        delColumn.setCellFactory(new CellPathButton<>(progData.backupInfoProperty, CellPathButton.EXCLUDE_DIR).cellFactory);
        delColumn.getStyleClass().add("alignCenter");

        pathColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.8));
        delColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.15));
        tableViewExcludeDir.getColumns().addAll(pathColumn, delColumn);

        vBoxTableDir.getChildren().addAll(hBox, tableViewExcludeDir);
    }

    private void makeVboxFile() {
        TextField txtExclude = new TextField();
        HBox.setHgrow(txtExclude, Priority.ALWAYS);

        Button btnAddFile = new Button();
        btnAddFile.setGraphic(P2IconFactory.P2ICON.BTN_PLUS_OUTLINE.getFontIcon());
        btnAddFile.setTooltip(new Tooltip("Dateien die von der Sicherung ausgeschlossen werden sollen"));
        btnAddFile.setOnAction(event -> {
            String exclude = txtExclude.getText();
            if (exclude.equals("*")) {
                P2Alert.showErrorAlert("Stern", "Der Stern muss am Anfang oder " +
                        "Ende einer Suche stehen");
                return;
            }
            if (exclude.startsWith("*") && exclude.endsWith("*")) {
                P2Alert.showErrorAlert("Stern", "Der Stern darf nur am Anfang ODER " +
                        "am Ende stehen");
                return;
            }

            if (!exclude.isEmpty()) {
                backupInfo.getPathListExcludeFile().add(new PathData(exclude));
            }
        });
        btnAddFile.disableProperty().bind(txtExclude.textProperty().isEmpty());
        Button btnHelp = PIconFactory.getHelpButton("Dateien ausschließen",
                "Hier können Dateien vorgegeben werden, die vom Backup ausgeschlossen werden " +
                        "oder die ausschließlich gesichert werden.\n\n" +
                        "TEXT -> Der angegebene Text muss im Dateinamen vorkommen.\n" +
                        "*TEXT -> Der angegebene Text muss am Ende des Dateinamens sein.\n" +
                        "TEXT* -> Der angegebene Text muss am Anfang des Dateinamens sein.");
        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(new Label("Datei auswählen:"),
                txtExclude, btnAddFile, btnHelp);
        vBoxTableFile.getChildren().add(hBox);


        HBox hBoxRadio = new HBox(P2LibConst.SPACING_HBOX);
        hBoxRadio.setAlignment(Pos.CENTER_LEFT);
        hBoxRadio.getChildren().addAll(new Label("Dateien sichern: "), rbNot, rbOnly);
        ToggleGroup tg = new ToggleGroup();
        rbNot.setToggleGroup(tg);
        rbOnly.setToggleGroup(tg);
        vBoxTableFile.getChildren().add(hBoxRadio);

        final TableColumn<PathData, String> pathColumn = new TableColumn<>("Pfad");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        final TableColumn<PathData, String> delColumn = new TableColumn<>("");
        delColumn.setCellFactory(new CellPathButton<>(progData.backupInfoProperty, CellPathButton.EXCLUDE_FILE).cellFactory);
        delColumn.getStyleClass().add("alignCenter");

        pathColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.8));
        delColumn.prefWidthProperty().bind(tableViewFrom.widthProperty().multiply(0.15));
        tableViewExcludeFile.getColumns().addAll(pathColumn, delColumn);

        vBoxTableFile.getChildren().addAll(tableViewExcludeFile);
    }

    private void initAccordion() {
        TitledPane titledPaneFrom = new TitledPane();
        TitledPane titledPaneDir = new TitledPane();
        TitledPane titledPaneFile = new TitledPane();
        titledPaneFrom.setText("Meine Dateien die gesichert werden");
        titledPaneDir.setText("Ordner davon ausschließen");
        titledPaneFile.setText("Dateien davon ausschließen");

        titledPaneFrom.setContent(vBoxTPaneFrom);
        titledPaneDir.setContent(vBoxTPaneDir);
        titledPaneFile.setContent(vBoxTPaneFile);
        accordion.getPanes().add(titledPaneFrom);
        accordion.getPanes().add(titledPaneDir);
        accordion.getPanes().add(titledPaneFile);
        accordion.setExpandedPane(titledPaneFrom);
    }

    private void setExtended() {
//        if (ProgConfig.SYSTEM_ENHANCED.get()) {
        vBoxTPaneFrom.getChildren().setAll(vBoxTableFrom);
        vBoxTPaneDir.getChildren().setAll(vBoxTableDir);
        vBoxTPaneFile.getChildren().setAll(vBoxTableFile);
        vBoxTableAll.getChildren().setAll(accordion);
        VBox.setVgrow(accordion, Priority.ALWAYS);

        vBoxTableFrom.getStyleClass().remove("tableFrom");
        vBoxTableFrom.getStyleClass().add("titledPaneFrom");
        vBoxTableDir.getStyleClass().add("titledPaneFrom");
        vBoxTableFile.getStyleClass().add("titledPaneFrom");

//        } else {
//            vBoxTableAll.getChildren().setAll(vBoxTableFrom);
//            vBoxTableFrom.getStyleClass().remove("titledPaneFrom");
//            vBoxTableFrom.getStyleClass().add("tableFrom");
//            VBox.setVgrow(vBoxTableFrom, Priority.ALWAYS);
//            vBoxTableFrom.setStyle("-fx-border-color: transparent;");
//        }
    }

    private void setBackup() {
        if (backupInfo != null) {
            rbNot.selectedProperty().unbindBidirectional(backupInfo.fileFilterNotProperty());
            backupInfo = null;
        }
        if (progData.backupInfoProperty.get() != null) {
            backupInfo = progData.backupInfoProperty.get();

            rbNot.selectedProperty().bindBidirectional(backupInfo.fileFilterNotProperty());
            rbOnly.setSelected(!rbNot.isSelected());
            tableViewFrom.setItems(backupInfo.getPathListFrom());
            tableViewExcludeDir.setItems(backupInfo.getPathListExcludeDir());
            tableViewExcludeFile.setItems(backupInfo.getPathListExcludeFile());
        }
    }
}
