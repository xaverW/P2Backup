package de.p2tools.p2backup.controller.picon;

import de.p2tools.p2backup.controller.config.ProgConfig;
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
        BTN_HELP("mdi-help"),
        BTN_FILE_OPEN("mdi2f-folder-open-outline", 18),
        BTN_DIR_OPEN("mdi2f-folder-open-outline", 18),
        BTN_PLUS("mdi-plus-circle-outline", 18),
        BTN_MINUS("mdi-minus-circle-outline", 18),
        BTN_NEXT("mdi-chevron-double-right", 18),
        BTN_PREV("mdi-chevron-double-left", 18),
        BTN_COPY("mdoal-file_copy", 18),
        BTN_QUITT("gmi-power-settings-new", 18),
        BTN_CLEAR("gmi-clear", 18),
        BTN_RANDOM("mdi-rotate-3d", 18),
        BTN_RESET_1("gmi-radio-button-on", 25),
        BTN_RESET_2("gmi-rotate-right", 30),
        BTN_SHOW_FROM("mdmz-play_arrow", 15),


        BTN_LOAD_BACKUP("mdoal-backup", 20),
        BTN_LOAD_BACKUP_BIG_30("mdoal-backup", 30),
        BTN_LOAD_BACKUP_BIG_50("mdoal-backup", 50),
        BTN_LOAD_REFRESH_BIG("mdi2f-folder-refresh-outline", 30),

        BTN_ADD_BACKUP("gmi-control-point", 20),
        BTN_ADD_BACKUP_BIG("gmi-control-point", 50),

        BTN_ATTENTION_DIALOG("gmi-error-outline", 100),
        BTN_ERROR_DIALOG("mdoal-error", 100),
        BTN_ERROR_DIALOG_SMALL("mdoal-error", 50),


        BTN_START_BACKUP("gmi-double-arrow", 14),
        BTN_STOP_BACKUP("mdmz-stop", 14),
        BTN_DIR_OPEN_DIALOG("mdi2f-folder-open-outline", 14),

        TABLE_FILE_DEL("gmi-clear", 15),
        TABLE_DIR_OPEN("mdi2f-folder-open-outline", 16),
        TABLE_START("mdomz-play_arrow", 20),
        TABLE_COPY("mdoal-file_copy", 15),

        TOOLBAR_BTN_FORWARD("gmi-navigate-next", 25),
        TOOLBAR_BTN_BACKWARD("gmi-navigate-before", 25),

        ATTENTION("mdomz-report", 80),
        SMALL_ICON_BIG("gmi-blur-on", 30),
        SMALL_ICON_SMALL("gmi-blur-on", 20),
        MENU("gmi-menu", 25);


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
            return getIcon(literal, size);
        }

        public FontIcon getFontIcon(int size) {
            this.size = size;
            return getIcon(literal, size);
        }

        @Override
        public String toString() {
            return literal;
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

    public static FontIcon getIcon(String literal, int size) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(size);
        fontIcon.setIconColor(Paint.valueOf(ProgConfig.SYSTEM_ICON_COLOR.getValueSafe()));
        fontIcon.setIconLiteral(literal);
        return fontIcon;
    }

    public static Button getHelpButton(String header, String helpText) {
        return P2Button.helpButton(P2IconFactory.P2ICON.BTN_HELP.getFontIcon(), header, helpText);
    }

    public static Button getHelpButton(Stage stage, String header, String helpText) {
        return P2Button.helpButton(stage, P2IconFactory.P2ICON.BTN_HELP.getFontIcon(), header, helpText);
    }
}
