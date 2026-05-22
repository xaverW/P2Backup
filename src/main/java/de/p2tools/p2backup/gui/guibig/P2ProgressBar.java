package de.p2tools.p2backup.gui.guibig;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class P2ProgressBar extends StackPane {
    private final ProgressBar progressBar = new ProgressBar();
    private final Text text = new Text();

    public P2ProgressBar() {
        setProgress();
    }

    private void setProgress() {
        getChildren().setAll(progressBar, text);
        progressBar.setMinHeight(text.getBoundsInLocal().getHeight() + 5);
        progressBar.setMinWidth(text.getBoundsInLocal().getWidth() + 5);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Text getText() {
        return text;
    }
}
