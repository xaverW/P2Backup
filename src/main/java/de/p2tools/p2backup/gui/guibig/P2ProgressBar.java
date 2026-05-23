package de.p2tools.p2backup.gui.guibig;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class P2ProgressBar extends StackPane {
    private final ProgressBar progressBar = new ProgressBar();
    private final Label lblText = new Label();

    public P2ProgressBar() {
        setProgress();
    }

    private void setProgress() {
        lblText.getStyleClass().add("lblProgress");
        lblText.setTextAlignment(TextAlignment.LEFT);
        lblText.setMaxWidth(Double.MAX_VALUE);

        progressBar.setMinWidth(250);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(Double.MAX_VALUE);
        VBox vBoxProgress = new VBox();
        vBoxProgress.getChildren().add(progressBar);
        VBox.setVgrow(progressBar, Priority.ALWAYS);

        getChildren().setAll(vBoxProgress, lblText);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getLblText() {
        return lblText;
    }
}
