package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.LoadFactory;
import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgConfig;
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
import de.p2tools.p2lib.tools.date.P2LDateTimeFactory;
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

public class GenerateBackupListPane extends VBox {
    private ProgData progData;

    public GenerateBackupListPane() {
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
        // Refresh
        Button btnRefresh = new Button("Das gespeicherte\nBackup suchen");
        btnRefresh.setWrapText(true);
        btnRefresh.setGraphic(PIconFactory.PICON.BTN_LOAD_REFRESH_BIG.getFontIcon());
        btnRefresh.visibleProperty().bind(backupInfo.notReadyProperty().and(backupInfo.backupPathProperty().isEmpty().not()));
        btnRefresh.managedProperty().bind(backupInfo.notReadyProperty().and(backupInfo.backupPathProperty().isEmpty().not()));
        btnRefresh.setTooltip(new Tooltip("Gespeichertes suchen"));
        btnRefresh.setOnAction(a -> LoadFactory.reLoadBackupInfo(backupInfo));

        // ==========
        // Load stored
        Button btnLoad = new Button("Ein gespeichertes\nBackup laden");
        btnLoad.setWrapText(true);
        btnLoad.setGraphic(PIconFactory.PICON.BTN_LOAD_BACKUP_BIG_30.getFontIcon());
        btnLoad.visibleProperty().bind(backupInfo.notReadyProperty());
        btnLoad.managedProperty().bind(backupInfo.notReadyProperty());
        btnLoad.setTooltip(new Tooltip("Ein gespeichertes Backup auswählen und laden"));
        btnLoad.setOnAction(a -> LoadFactory.loadBackupInfo(backupInfo));

        // ===========
        // From
        boolean done = false;
        Button btnFrom = new Button("");
        btnFrom.visibleProperty().bind(ProgConfig.SYSTEM_ENHANCED);
        btnFrom.managedProperty().bind(ProgConfig.SYSTEM_ENHANCED);
        btnFrom.setTooltip(new Tooltip("Ändern"));
        btnFrom.getStyleClass().add("btnAdjust");
        btnFrom.setGraphic(PIconFactory.PICON.TABLE_START.getFontIcon());
        btnFrom.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            progData.programState.set(ProgConst.PROGRAM_STATE_FROM);
        });

        // ===========
        // To
        Button btnTo = new Button("");
        btnTo.visibleProperty().bind(ProgConfig.SYSTEM_ENHANCED);
        btnTo.managedProperty().bind(ProgConfig.SYSTEM_ENHANCED);
        btnTo.setTooltip(new Tooltip("Ändern"));
        btnTo.getStyleClass().add("btnAdjust");
        btnTo.setGraphic(PIconFactory.PICON.TABLE_START.getFontIcon());
        btnTo.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            progData.programState.set(ProgConst.PROGRAM_STATE_TO);
        });

        // ===========
        // Start
        Button btnStart = new Button("Starten");
        btnStart.setOnAction(a -> {
            progData.backupInfoProperty.set(backupInfo);
            new BackupRunner(backupInfo).makeBackup();
        });
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.disableProperty().bind(backupInfo.runnerDto.runningProperty()
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
        Label lblN = new Label("Name:");
        gridPane.add(lblN, 0, row);
        gridPane.add(lblName, 2, row);
        lblN.setStyle("-fx-font-size: 1.1em; -fx-font-weight: bold;");
        lblName.setStyle("-fx-font-size: 1.1em; -fx-font-weight: bold;");

        // ==========
        // LastDate
        if (!P2LDateTimeFactory.toString(backupInfo.getLastStartDate()).isEmpty()) {
            Label lblLastDate = new Label();
            lblLastDate.setText(P2LDateTimeFactory.toString(backupInfo.getLastStartDate()));
            gridPane.add(new Label("Letztes Backup:"), 0, ++row);
            gridPane.add(lblLastDate, 2, row);
        }

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

        HBox hBoxRefresh = new HBox();
        hBoxRefresh.getChildren().add(btnRefresh);
        HBox.setHgrow(btnRefresh, Priority.ALWAYS);
        btnRefresh.setMaxWidth(Double.MAX_VALUE);

        HBox hBoxLoad = new HBox();
        hBoxLoad.getChildren().add(btnLoad);
        HBox.setHgrow(btnLoad, Priority.ALWAYS);
        btnLoad.setMaxWidth(Double.MAX_VALUE);

        VBox vBoxLoad = new VBox(5);
        vBoxLoad.setAlignment(Pos.TOP_CENTER);
        vBoxLoad.getChildren().addAll(hBoxLoad, hBoxRefresh);

        VBox vBoxButton = new VBox(5);
        vBoxButton.setAlignment(Pos.TOP_CENTER);
        vBoxButton.getChildren().addAll(btnStart, btnDel);

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(gridPane, vBoxLoad, vBoxButton);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        VBox vBoxAll = new VBox();
        vBoxAll.setPadding(new Insets(P2LibConst.PADDING_HBOX));
        vBoxAll.setOnMouseClicked(mouseEvent -> {
            progData.backupInfoProperty.set(backupInfo);
            init();
        });
        this.progData.backupInfoProperty.addListener((u, o, n) -> setStyle(backupInfo, vBoxAll));
        setStyle(backupInfo, vBoxAll);

        vBoxAll.getChildren().addAll(hBox, addProgress(backupInfo));
        VBox.setVgrow(hBox, Priority.ALWAYS);
        getChildren().addAll(vBoxAll);
    }

    private void setStyle(BackupInfo backupInfo, VBox vBoxAll) {
        if (progData.backupInfoProperty.get() != null && progData.backupInfoProperty.get().equals(backupInfo)) {
            // dann ists ausgewählt
            if (backupInfo.runnerDto.isRunning()) {
                vBoxAll.setStyle("-fx-border-color: red; -fx-border-width: 4px;");
            } else {
                if (progData.backupInfoList.size() <= 1) {
                    vBoxAll.setStyle("-fx-border-color: green; -fx-border-width: 2px; -fx-border-insets: 2px;");
                } else {
                    vBoxAll.setStyle("-fx-border-color: green; -fx-border-width: 4px;");
                }
            }

        } else {
            if (backupInfo.runnerDto.isRunning()) {
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

        final PProgressBar pProgressBar = new PProgressBar(true, true);
        HBox hBoxProgress = new HBox(P2LibConst.SPACING_HBOX);
        hBoxProgress.setPadding(new Insets(10, 0, 0, 0));
        hBoxProgress.getChildren().addAll(/*lblText, lblFileName, P2GuiTools.getHBoxGrower(),*/ pProgressBar, btnStop);
        HBox.setHgrow(pProgressBar, Priority.ALWAYS);
        hBoxProgress.setAlignment(Pos.CENTER);

        hBoxProgress.visibleProperty().bind(backupInfo.runnerDto.runningProperty());
        hBoxProgress.managedProperty().bind(backupInfo.runnerDto.runningProperty());
        return hBoxProgress;
    }
}
