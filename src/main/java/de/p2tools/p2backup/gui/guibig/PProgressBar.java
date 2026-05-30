package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
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
    private final boolean text;
    private final boolean name;

    public PProgressBar(boolean text, boolean name) {
        this.text = text;
        this.name = name;
        setProgress();
    }

    public PProgressBar() {
        text = true;
        name = false;
        setProgress();
    }

    private void setProgress() {
        lblText.getStyleClass().add("lblProgress");
        lblText.setTextAlignment(TextAlignment.LEFT);
        lblText.setMaxWidth(Double.MAX_VALUE);

        lblName.getStyleClass().add("lblProgress");
        lblName.setTextAlignment(TextAlignment.LEFT);
        lblName.setMaxWidth(Double.MAX_VALUE);

        HBox hBoxName = new HBox(10);
        hBoxName.getChildren().addAll(lblText, lblName);
//        HBox.setHgrow(lblName, Priority.ALWAYS);

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

        BackupInfo backupInfo = ProgData.getInstance().backupInfoProperty.get();
        setVisible(backupInfo != null);
        if (backupInfo != null) {
            visibleProperty().bind(backupInfo.runnerDto.guiRunningProperty());
            progressBar.progressProperty().bind(backupInfo.runnerDto.guiProgressProperty());
            lblText.textProperty().bind(backupInfo.runnerDto.guiTextProperty());
            lblName.textProperty().bind(backupInfo.runnerDto.guiFileNameProperty());

            lblText.setVisible(text);
            lblText.setManaged(text);
            lblName.setVisible(name);
            lblName.setManaged(name);
        }
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getLblText() {
        return lblText;
    }
}
