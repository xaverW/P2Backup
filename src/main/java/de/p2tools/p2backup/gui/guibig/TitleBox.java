package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfosProps;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class TitleBox extends VBox {
    private final StringProperty nameProp = new SimpleStringProperty("");
    private final Label lblTop = new Label("");
    private final ComboBox<BackupInfo> cboBackup = new ComboBox<>();
    private final ProgData progData;

    public TitleBox() {
        this.progData = ProgData.getInstance();
        this.visibleProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1));
        this.managedProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1));
        initTitleBox();
    }

    private void initTitleBox() {
        progData.backupInfoProperty.addListener((u, o, n) -> {
            set();
        });
        set();

        nameProp.addListener((u, o, n) -> setName());
        cboBackup.setItems(ProgData.getInstance().backupInfoList
                .sorted(Comparator.comparing(BackupInfosProps::getName)));
        cboBackup.getSelectionModel().selectedItemProperty().addListener((u, o, n) -> {
            BackupInfo b = cboBackup.getSelectionModel().getSelectedItem();
            if (b != null) {
                progData.backupInfoProperty.set(b);
            } else {
                cboBackup.getSelectionModel().clearSelection();
            }
        });
        cboBackup.visibleProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1));
        cboBackup.managedProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1));
        lblTop.visibleProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1).not());
        lblTop.managedProperty().bind(ProgData.getInstance().backupInfoList.sizeProperty().greaterThan(1).not());

        HBox hBox = new HBox();
        hBox.getStyleClass().add("titlePane");
        hBox.setPadding(new Insets(0, 5, 0, 5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(new Label("Backup:"), cboBackup, lblTop,
                P2GuiTools.getHBoxGrower(), new PProgressBar());
        getChildren().addAll(hBox, P2GuiTools.getHDistance(5));
    }

    private void setName() {
        if (progData.backupInfoProperty.get() != null) {
            cboBackup.getSelectionModel().clearSelection();
            cboBackup.getSelectionModel().select(progData.backupInfoProperty.get());
        }
    }

    private void set() {
        BackupInfo backupInfos;
        if (progData.backupInfoProperty.get() == null) {
            nameProp.unbind();
            lblTop.textProperty().unbind();
            lblTop.setText("");
            cboBackup.getSelectionModel().clearSelection();
        } else {
            backupInfos = progData.backupInfoProperty.get();
            nameProp.bind(backupInfos.nameProperty());
            lblTop.textProperty().bind(backupInfos.nameProperty());
            cboBackup.getSelectionModel().select(backupInfos);
        }
    }
}
