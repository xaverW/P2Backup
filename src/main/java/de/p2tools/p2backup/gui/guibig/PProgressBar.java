package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class PProgressBar extends StackPane {
    private final ProgressBar progressBar = new ProgressBar();
    private final Label lblText = new Label();
    private final Label lblName = new Label();
    private final Label lblToDo = new Label();
    private final boolean text;
    private final boolean name;
    private boolean indeterminate = false;
    private BackupInfo backupInfo = null;

    public PProgressBar() {
        text = true;
        name = false;
        setProgress();
    }

    public PProgressBar(boolean text, boolean name) {
        this.text = text;
        this.name = name;
        setProgress();
    }

    public PProgressBar(BackupInfo backupInfo, boolean text, boolean name, boolean indeterminate) {
        this.backupInfo = backupInfo;
        this.text = text;
        this.name = name;
        this.indeterminate = indeterminate;
        setProgress();
    }

    private void setProgress() {
        lblText.getStyleClass().add("lblProgress");
        lblText.setTextAlignment(TextAlignment.LEFT);
        lblText.setMaxWidth(Double.MAX_VALUE);

        lblName.getStyleClass().add("lblProgress");
        lblName.setTextAlignment(TextAlignment.LEFT);
        lblName.setMaxWidth(Double.MAX_VALUE);

        lblToDo.getStyleClass().add("lblProgress");
        lblToDo.setTextAlignment(TextAlignment.RIGHT);

        HBox hBoxName = new HBox(10);
        hBoxName.getChildren().addAll(lblText, lblName, P2GuiTools.getHBoxGrower(), lblToDo);

        progressBar.setMinWidth(250);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(Double.MAX_VALUE);
        VBox vBoxProgress = new VBox();
        vBoxProgress.getChildren().add(progressBar);
        VBox.setVgrow(progressBar, Priority.ALWAYS);

        getChildren().setAll(vBoxProgress, hBoxName);

        setProgressBarVisible();
        ProgData.getInstance().backupInfoProperty.addListener((u, o, n) -> {
            setProgressBarVisible();
        });

    }

    private void setProgressBarVisible() {
        visibleProperty().unbind();
        progressBar.progressProperty().unbind();
        lblText.textProperty().unbind();
        lblName.textProperty().unbind();
        lblToDo.textProperty().unbind();

        BackupInfo baInfo = backupInfo == null ? ProgData.getInstance().backupInfoProperty.get() : backupInfo;
        setVisible(baInfo != null);

        if (baInfo != null) {
            visibleProperty().bind(baInfo.runnerDto.guiRunningProperty());
            if (indeterminate) {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            } else {
                progressBar.progressProperty().bind(baInfo.runnerDto.guiProgressProperty());
            }
            lblText.textProperty().bind(baInfo.runnerDto.guiTextProperty());
            lblName.textProperty().bind(baInfo.runnerDto.guiFileNameProperty());
            lblToDo.textProperty().bind(baInfo.runnerDto.guiToDoProperty().asString());
            lblToDo.visibleProperty().bind(baInfo.runnerDto.guiToDoProperty().isNotEqualTo(0).and(lblName.visibleProperty()));
            lblText.setVisible(text);
            lblText.setManaged(text);
            lblName.setVisible(name);
            lblName.setManaged(name);
            lblToDo.setManaged(name);
        }
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getLblText() {
        return lblText;
    }
}
