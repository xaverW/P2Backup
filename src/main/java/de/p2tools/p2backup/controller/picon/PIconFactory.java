package de.p2tools.p2backup.controller.picon;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.ikonli.IkonlyFactory;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class PIconFactory {

    public enum PICON implements P2IconFactory.P2Icon {
        BTN_START_ALL("mdi2s-share-all", 18),
        BTN_LOAD_BACKUP("mdoal-backup", 20),
        BTN_LOAD_BACKUP_BIG_50("mdoal-backup", 50),
        BTN_LOAD_REFRESH_BIG("mdi2f-folder-refresh-outline", 30);

        private final String literal;
        private int size = 18;

        PICON(String literal) {
            this.literal = literal;
        }

        PICON(String literal, int size) {
            this.literal = literal;
            this.size = size;
        }

        public String getLiteral() {
            return literal;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public FontIcon getFontIcon() {
            return P2IconFactory.getIcon(literal, size);
        }

        public FontIcon getFontIcon(int size) {
            this.size = size;
            return P2IconFactory.getIcon(literal, size);
        }
    }

    private PIconFactory() {
    }


    public static FontIcon getAttentionIcon(String literal) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(100);
        fontIcon.setIconColor(Paint.valueOf(Color.RED.toString()));
        fontIcon.setIconLiteral(literal);
        return fontIcon;
    }

    public static FontIcon getAttentionIconSmall(String literal) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(50);
        fontIcon.setIconColor(Paint.valueOf(Color.RED.toString()));
        fontIcon.setIconLiteral(literal);
        return fontIcon;
    }

    public static void setColor() {
        if (ProgData.getInstance().backupBigGui != null) {
            IkonlyFactory.getAllNodes(ProgData.getInstance().backupBigGui);
        }
        if (ProgData.getInstance().backupSmallGui != null) {
            IkonlyFactory.getAllNodes(ProgData.getInstance().backupSmallGui.getStage().getScene().getRoot());
        }
    }

    public static Button getHelpButton(String header, String helpText) {
        return P2Button.helpButton(P2IconFactory.P2ICON.BTN_HELP.getFontIcon(), header, helpText);
    }

    public static Button getHelpButton(Stage stage, String header, String helpText) {
        return P2Button.helpButton(stage, P2IconFactory.P2ICON.BTN_HELP.getFontIcon(), header, helpText);
    }
}
