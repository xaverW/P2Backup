package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.LoadFactory;
import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.controller.runner.backuprunner.BackupRunner;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.p2event.P2Listener;
import de.p2tools.p2lib.tools.P2ColorFactory;
import de.p2tools.p2lib.tools.date.P2LDateTimeFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class PaneGenerateBackupList extends VBox {
    private ProgData progData;

    public PaneGenerateBackupList() {
        this.progData = ProgData.getInstance();

        setSpacing(P2LibConst.SPACING_VBOX);
        init();

        progData.backupInfoList.addListener((u, o, n) -> init());
        progData.programState.addListener((u, o, n) -> init());
        progData.pEventHandler.addListener(new P2Listener(PEvents.EVENT_RUNNER_RUN) {
            @Override
            public void pingGui(P2Event event) {
                init();
            }
        });
    }

    private synchronized void init() {
        getChildren().clear();

        if (progData.backupInfoList.isEmpty()) {
            progData.backupInfoProperty.set(null);

        } else {
            if (progData.backupInfoProperty.get() == null) {
                progData.backupInfoProperty.set(progData.backupInfoList
                        .sorted(Comparator.comparing(BackupInfo::getName)).getFirst());
            }
            initBackupScroll();
        }
    }

    private void initBackupScroll() {
        progData.backupInfoList.forEach(b -> {
            b.getPathListFrom().addListener((ListChangeListener<PathData>)
                    change -> init()
            );
        });

        progData.backupInfoList.stream()
                .sorted(Comparator.comparing(BackupInfo::getName)).
                toList().forEach(this::addBackup);
    }

    private void addBackup(BackupInfo backupInfo) {
        // ==========
        // Backup suchen
        Button btnSearch = new Button("Das gespeicherte\nBackup suchen");
        btnSearch.setWrapText(true);
        btnSearch.setGraphic(PIconFactory.PICON.BTN_LOAD_REFRESH_BIG.getFontIcon());
        // nur wenn Pfad nicht verfügbar!
        btnSearch.visibleProperty().bind(backupInfo.notReadyProperty()
                .and(backupInfo.backupPathProperty().isEmpty().not()
                        .and(backupInfo.sqlLoadedProperty().not())));
        btnSearch.managedProperty().bind(btnSearch.visibleProperty());

        btnSearch.setTooltip(new Tooltip("Gespeichertes suchen"));
        btnSearch.setOnAction(a -> LoadFactory.reLoadBackupInfo(backupInfo));

        // ===========
        // From
        boolean done = false;
        Button btnFrom = new Button("");
        btnFrom.setTooltip(new Tooltip("Ändern"));
        btnFrom.getStyleClass().add("btnAdjust");
        btnFrom.setGraphic(PIconFactory.PICON.BTN_SHOW_FROM.getFontIcon());
        btnFrom.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            progData.programState.set(ProgConst.PROGRAM_STATE_FROM);
        });
        btnFrom.visibleProperty().bind(backupInfo.getPathListFrom().emptyProperty());
        btnFrom.managedProperty().bind(btnFrom.visibleProperty());

        // ===========
        // To
        Button btnTo = new Button("");
        btnTo.setTooltip(new Tooltip("Ändern"));
        btnTo.getStyleClass().add("btnAdjust");
        btnTo.setGraphic(PIconFactory.PICON.BTN_SHOW_FROM.getFontIcon());
        btnTo.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            progData.programState.set(ProgConst.PROGRAM_STATE_TO);
        });
        btnTo.visibleProperty().bind(backupInfo.backupPathProperty().isEmpty());
        btnTo.managedProperty().bind(btnTo.visibleProperty());

        // ===========
        // Start
        Button btnStart = new Button("Starten");
        btnStart.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            new BackupRunner(backupInfo).makeBackup();
        });
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.disableProperty().bind(backupInfo.runnerDto.guiRunningProperty()
                .or(backupInfo.notReadyProperty()));

        // ===========
        // Del
        Button btnDel = new Button("Ausblenden");
        btnDel.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            if (P2Alert.BUTTON.YES.equals(P2Alert.showAlert_yes_no("Backup entfernen",
                    "Soll das Backup nicht mehr angezeigt werden?",
                    "Die Dateien im Backup-Ordner " +
                            "werden damit nicht gelöscht! " +
                            "Die gesicherten Dateien müssen, " +
                            "wenn gewollt, selbst gelöscht werden."))) {
                progData.backupInfoProperty.set(null);
                progData.backupInfoList.remove(backupInfo);
            }
        });
        btnDel.setMaxWidth(Double.MAX_VALUE);


        final GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(1);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow());

        int row = 0;
        // ==========
        // Name
        Label lblName = new Label();
        lblName.textProperty().bind(backupInfo.nameProperty());
        backupInfo.colorProperty().addListener((u, o, n) -> {
            lblName.setStyle(getNameColor(backupInfo));
        });
        lblName.setStyle(getNameColor(backupInfo));

        Label lblN = new Label("Name:");
        lblN.setStyle("-fx-font-size: 1.1em; -fx-font-weight: bold;");

        gridPane.add(lblN, 0, row);
        gridPane.add(lblName, 2, row);

        // ==========
        // LastDate
//        if (!P2LDateTimeFactory.toString(backupInfo.getLastStartDate()).isEmpty()) {
        Label lblLastDate = new Label();
        lblLastDate.textProperty().bind(Bindings.createStringBinding(
                () -> P2LDateTimeFactory.toString(backupInfo.getLastStartDate()),
                backupInfo.lastStartDateProperty()));


//        lblLastDate.setText(P2LDateTimeFactory.toString(backupInfo.getLastStartDate()));
        gridPane.add(new Label("Letztes Backup:"), 0, ++row);
        gridPane.add(lblLastDate, 2, row);
//        }

        // ===========
        // To
        Label lblTo = new Label("Backup-Ordner:");
        gridPane.add(lblTo, 0, ++row);

        Label lblToPath = new Label();
        lblToPath.textProperty().bind(backupInfo.backupPathProperty());
        if (backupInfo.getBackupPath().isEmpty()) {
            gridPane.add(btnTo, 1, row);
            GridPane.setValignment(lblTo, VPos.CENTER);
            GridPane.setValignment(btnTo, VPos.CENTER);

        } else {
            gridPane.add(btnTo, 1, row);
            gridPane.add(lblToPath, 2, row);
        }

        if (backupInfo.isNotReady() && backupInfo.getBackupPath().isEmpty()) {
            lblToPath.getStyleClass().add("emptyBackupPath");
        } else {
            lblToPath.getStyleClass().remove("emptyBackupPath");
        }
        backupInfo.backupPathProperty().addListener((u, o, n) -> {
            if (backupInfo.isNotReady() && backupInfo.getBackupPath().isEmpty()) {
                lblToPath.getStyleClass().add("emptyBackupPath");
            } else {
                lblToPath.getStyleClass().remove("emptyBackupPath");
            }
        });


        // From
        Label lblFrom = new Label("Daten zum Sichern:");
        if (backupInfo.getPathListFrom().isEmpty()) {
            gridPane.add(lblFrom, 0, ++row);
            gridPane.add(btnFrom, 1, row);
            GridPane.setValignment(lblFrom, VPos.CENTER);
            GridPane.setValignment(btnFrom, VPos.CENTER);

        } else {
            for (PathData p : backupInfo.getPathListFrom()) {
                ++row;
                Label lblFromPath = new Label();
                lblFromPath.textProperty().bind(p.pathProperty());
                if (!done) {
                    done = true;
                    gridPane.add(lblFrom, 0, row);
                    gridPane.add(btnFrom, 1, row);
                }

                gridPane.add(lblFromPath, 2, row);
            }
        }


        VBox vBoxLoad = new VBox(5);
        vBoxLoad.setAlignment(Pos.CENTER);
        vBoxLoad.getChildren().addAll(btnSearch);
        VBox.setVgrow(vBoxLoad, Priority.ALWAYS);

        VBox vBoxButton = new VBox(5);
        vBoxButton.setAlignment(Pos.TOP_CENTER);
        vBoxButton.getChildren().addAll(btnStart, btnDel);

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(gridPane, vBoxLoad, vBoxButton);
        HBox.setHgrow(gridPane, Priority.ALWAYS);
        VBox.setVgrow(hBox, Priority.ALWAYS);

        VBox vBoxAll = new VBox();
        vBoxAll.setPadding(new Insets(P2LibConst.PADDING_HBOX));
        vBoxAll.setOnMouseClicked(mouseEvent -> {
            progData.backupInfoProperty.set(backupInfo);
            init();
        });
        this.progData.backupInfoProperty.addListener((u, o, n) -> setStyle(backupInfo, vBoxAll));
        setStyle(backupInfo, vBoxAll);
        vBoxAll.getChildren().addAll(hBox, addProgress(backupInfo));
        VBox.setVgrow(vBoxAll, Priority.ALWAYS);

        getChildren().addAll(vBoxAll);
    }

    private String getNameColor(BackupInfo backupInfo) {
        String color = P2ColorFactory.getColor(backupInfo.getColor());
        return "-fx-font-size: 1.1em; -fx-font-weight: bold; -fx-text-fill: " + color + ";";
    }

    private void setStyle(BackupInfo backupInfo, VBox vBoxAll) {
        if (progData.backupInfoProperty.get() != null && progData.backupInfoProperty.get().getId() == backupInfo.getId()) {
            // dann ists ausgewählt
            if (backupInfo.runnerDto.getGuiRunning()) {
                vBoxAll.setStyle("-fx-border-color: red; -fx-border-width: 4px;");
            } else {
                if (progData.backupInfoList.size() <= 1) {
                    vBoxAll.setStyle("-fx-border-color: green; -fx-border-width: 2px; -fx-border-insets: 2px;");
                } else {
                    vBoxAll.setStyle("-fx-border-color: green; -fx-border-width: 4px;");
                }
            }

        } else {
            if (backupInfo.runnerDto.getGuiRunning()) {
                vBoxAll.setStyle("-fx-border-color: red; -fx-border-width: 1px; -fx-border-insets: 3px;");
            } else {
                vBoxAll.setStyle("-fx-border-color: grey; -fx-border-width: 2px; -fx-border-insets: 2px;");
            }
        }
    }


    private HBox addProgress(BackupInfo backupInfo) {

        Button btnStop = new Button();
        btnStop.setGraphic(PIconFactory.PICON.TABLE_FILE_DEL.getFontIcon());
        btnStop.setOnAction(a -> backupInfo.runnerDto.setStop());

        final PProgressBar pProgressBar = new PProgressBar(backupInfo, true, true, false);
        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.setPadding(new Insets(10, 0, 0, 0));
        hBoxProgress.getChildren().addAll(pProgressBar, btnStop);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);
        hBoxProgress.setAlignment(Pos.CENTER);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        hBoxProgress.managedProperty().bind(backupInfo.runnerDto.guiRunningProperty());
        return hBoxProgress;
    }
}
