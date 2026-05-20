/*
 * MTViewer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
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


package de.p2tools.p2backup.controller.config;

import de.p2tools.p2lib.configfile.ConfigFile;
import de.p2tools.p2lib.configfile.pdata.P2DataProgConfig;
import de.p2tools.p2lib.mediathek.download.GetProgramStandardPath;
import de.p2tools.p2lib.tools.P2InfoFactory;
import javafx.beans.property.*;

public class ProgConfig extends P2DataProgConfig {
    private static ProgConfig instance;

    private ProgConfig() {
        super("ProgConfig");
    }

    public static final ProgConfig getInstance() {
        return instance == null ? instance = new ProgConfig() : instance;
    }

    public static void addConfigData(ConfigFile configFile) {
        ProgData progData = ProgData.getInstance();

        // Configs der Programmversion, nur damit sie (zur Update-Suche) im Config-File stehen
        ProgConfig.SYSTEM_PROG_VERSION.set(P2InfoFactory.getProgVersion());
        ProgConfig.SYSTEM_PROG_BUILD_NO.set(P2InfoFactory.getBuildNo());
        ProgConfig.SYSTEM_PROG_BUILD_DATE.set(P2InfoFactory.getBuildDateR());

        configFile.addConfigs(ProgConfig.getInstance());
        configFile.addConfigs(progData.dbDataList);
        configFile.addConfigs(progData.usedToPathList);
    }

    // Programm-Configs, änderbar nur im Konfig-File
    // ============================================
    // 250 Sekunden, wie bei Firefox
    public static int SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SECOND_INIT = 250;
    public static IntegerProperty SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SECOND = addIntProp("__system-parameter__download-timeout-second_250__", SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SECOND_INIT);
    // max. Startversuche für fehlgeschlagene Downloads (insgesamt: restart * restart_http Versuche)
    public static int SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_INIT = 3;
    public static IntegerProperty SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART = addIntProp("__system-parameter__download-max-restart_5__", SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_INIT);
    // max. Startversuche für fehlgeschlagene Downloads, direkt beim Download
    public static int SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP_INIT = 5;
    public static IntegerProperty SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP = addIntProp("__system-parameter__download-max-restart-http_10__", SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP_INIT);
    // Beim Dialog "Download weiterführen" wird nach dieser Zeit der Download weitergeführt
    public static int SYSTEM_PARAMETER_DOWNLOAD_CONTINUE_IN_SECONDS_INIT = 60;
    public static IntegerProperty SYSTEM_PARAMETER_DOWNLOAD_CONTINUE_IN_SECONDS = addIntProp("__system-parameter__download-continue-second_60__", SYSTEM_PARAMETER_DOWNLOAD_CONTINUE_IN_SECONDS_INIT);
    // Beim Dialog "Automode" wird nach dieser Zeit der das Programm beendet
    public static int SYSTEM_PARAMETER_AUTOMODE_QUITT_IN_SECONDS_INIT = 15;
    public static IntegerProperty SYSTEM_PARAMETER_AUTOMODE_QUITT_IN_SECONDS = addIntProp("__system-parameter__automode-quitt-second_60__", SYSTEM_PARAMETER_AUTOMODE_QUITT_IN_SECONDS_INIT);
    // Downloadfehlermeldung wird xx Sedunden lang angezeigt
    public static int SYSTEM_PARAMETER_DOWNLOAD_ERRORMSG_IN_SECOND_INIT = 30;
    public static IntegerProperty SYSTEM_PARAMETER_DOWNLOAD_ERRORMSG_IN_SECOND = addIntProp("__system-parameter__download-errormsg-in-second_30__", SYSTEM_PARAMETER_DOWNLOAD_ERRORMSG_IN_SECOND_INIT);
    // Downloadprogress im Terminal anzeigen
    public static BooleanProperty SYSTEM_PARAMETER_DOWNLOAD_PROGRESS = addBoolProp("__system-parameter__download_progress_", Boolean.TRUE);

    // ===============================================================
    // ====== SYSTEM =================================================
    // ===============================================================
    static {
        addComment("Prog-Version");
    }

    // ===========================================
    // Configs der Programmversion, nur damit sie (zur Update-Suche) im Config-File stehen
    public static StringProperty SYSTEM_PROG_VERSION = addStrProp("system-prog-version", P2InfoFactory.getProgVersion());
    public static StringProperty SYSTEM_PROG_BUILD_NO = addStrProp("system-prog-build-no", P2InfoFactory.getBuildNo());
    public static StringProperty SYSTEM_PROG_BUILD_DATE = addStrProp("system-prog-build-date", P2InfoFactory.getBuildDateR());//z.B.: 27.07.2

    // ===============================================================
    // ====== Shortcuts ===============================================
    static {
        addComment("Shortcuts");
    }

    // Shortcuts, Programmweit
    public static String SHORTCUT_CENTER_INIT = "Ctrl+W";
    public static StringProperty SHORTCUT_CENTER_GUI = addStrProp("SHORTCUT_CENTER_GUI", SHORTCUT_CENTER_INIT);

    public static String SHORTCUT_MINIMIZE_INIT = "Alt+M";
    public static StringProperty SHORTCUT_MINIMIZE_GUI = addStrProp("SHORTCUT_MINIMIZE_GUI", SHORTCUT_MINIMIZE_INIT);

    // Shorcuts Hauptmenü
    public static String SHORTCUT_QUIT_PROGRAM_INIT = "Ctrl+Q";
    public static StringProperty SHORTCUT_QUIT_PROGRAM = addStrProp("SHORTCUT_QUIT_PROGRAM", SHORTCUT_QUIT_PROGRAM_INIT);

    public static String SHORTCUT_QUIT_PROGRAM_WAIT_INIT = "Ctrl+Shift+Q";
    public static StringProperty SHORTCUT_QUIT_PROGRAM_WAIT = addStrProp("SHORTCUT_QUIT_PROGRAM_WAIT", SHORTCUT_QUIT_PROGRAM_WAIT_INIT);


    static {
        addComment("ProgrammUpdateSuche");
    }

    // Configs zum Aktualisieren beim Programmupdate
    public static StringProperty SYSTEM_SEARCH_UPDATE_TODAY_DONE = addStrProp("system-update-date"); // Datum der letzten Prüfung
    public static StringProperty SYSTEM_UPDATE_DATE = addStrProp("system-update-date"); // Datum der letzten Prüfung
    public static BooleanProperty SYSTEM_UPDATE_SEARCH_ACT = addBoolProp("system-update-search-act", Boolean.TRUE); //Infos und Programm
    public static StringProperty SYSTEM_SEARCH_UPDATE_LAST_DATE = addStrProp("system-search-update-last-date"); // Datum der letzten Prüfung
    public static BooleanProperty SYSTEM_SEARCH_UPDATE = addBoolProp("system-search-update", Boolean.TRUE); // nach einem Update suchen
    public static BooleanProperty SYSTEM_UPDATE_SEARCH_BETA = addBoolProp("system-update-search-beta", Boolean.FALSE); //beta suchen
    public static BooleanProperty SYSTEM_UPDATE_SEARCH_DAILY = addBoolProp("system-update-search-daily", Boolean.FALSE); //daily suchen

    static {
        addEmptyLine();
    }

    // ConfigDialog, Dialog nach Start immer gleich öffnen
    public static IntegerProperty SYSTEM_CONFIG_DIALOG_TAB = new SimpleIntegerProperty(0);
    public static IntegerProperty SYSTEM_CONFIG_DIALOG_CONFIG = new SimpleIntegerProperty(-1);

    // Configs
    public static StringProperty SYSTEM_PROG_OPEN_URL = addStrProp("system-prog-open-url");
    public static StringProperty SYSTEM_PROG_PLAY = addStrProp("system-prog-play", GetProgramStandardPath.getTemplatePathVlc());
    public static String SYSTEM_PROG_SAVE_INIT = GetProgramStandardPath.getTemplatePathFFmpeg();
    public static StringProperty SYSTEM_PROG_SAVE = addStrProp("system-prog-save", SYSTEM_PROG_SAVE_INIT);
    public static BooleanProperty SYSTEM_BLACK_WHITE_ICON_START = addBoolProp("system-black-white-icon-start", Boolean.FALSE);
    public static BooleanProperty SYSTEM_SMALL_BACKUP = addBoolProp("system-small-backup", false);
    public static BooleanProperty SYSTEM_ENHANCED = addBoolProp("system-enhanced", false);

    public static StringProperty SYSTEM_LOG_DIR = addStrProp("system-log-dir", "");
    public static BooleanProperty SYSTEM_LOG_ON = addBoolProp("system-log-on", Boolean.TRUE);
    public static BooleanProperty SYSTEM_BLACK_WHITE_ICON = addBoolProp("system-black-white-icon", Boolean.FALSE);
    public static BooleanProperty TIP_OF_DAY_SHOW = addBoolProp("tip-of-day-show", Boolean.TRUE);//Tips anzeigen
    public static StringProperty TIP_OF_DAY_WAS_SHOWN = addStrProp("tip-of-day-was-shown");//bereits angezeigte Tips
    public static StringProperty TIP_OF_DAY_DATE = addStrProp("tip-of-day-date"); //Datum des letzten Tips
    public static StringProperty SYSTEM_DOWNLOAD_DIR_NEW_VERSION = addStrProp("system-download-dir-new-version", "");


    // DialogHowHelp
    public static StringProperty DIALOG_HOW_HELP_SIZE = addStrProp("dialog-how-help-size", "900:700");

    // CompareDirDialog
    public static StringProperty COMPARE_DIALOG_SIZE = addStrProp("compare-dialog-size", "800:500");
    public static StringProperty COMPARE_DIALOG_TABLE_WIDTH = addStrProp("compare-dialog-table-width");
    public static StringProperty COMPARE_DIALOG_TABLE_SORT = addStrProp("compare-dialog-table-sort");
    public static StringProperty COMPARE_DIALOG_TABLE_UP_DOWN = addStrProp("compare-dialog-table-up-down");
    public static StringProperty COMPARE_DIALOG_TABLE_VIS = addStrProp("compare-dialog-table-vis");
    public static StringProperty COMPARE_DIALOG_TABLE_ORDER = addStrProp("compare-dialog-table-order");

    // SearchDirDialog
    public static StringProperty SEARCH_DIALOG_SIZE = addStrProp("search-dialog-size", "800:500");
    public static StringProperty SEARCH_DIALOG_TABLE_WIDTH = addStrProp("search-dialog-table-width");
    public static StringProperty SEARCH_DIALOG_TABLE_SORT = addStrProp("search-dialog-table-sort");
    public static StringProperty SEARCH_DIALOG_TABLE_UP_DOWN = addStrProp("search-dialog-table-up-down");
    public static StringProperty SEARCH_DIALOG_TABLE_VIS = addStrProp("search-dialog-table-vis");
    public static StringProperty SEARCH_DIALOG_TABLE_ORDER = addStrProp("search-dialog-table-order");

    // InfoBackupDialog
    public static StringProperty BACKUP_INFO_DIALOG_SIZE = addStrProp("backup-info-dialog-size", "500:500");

    // BlockedFileDialog
    public static StringProperty BLOCKED_FILE_DIALOG_SIZE = addStrProp("blocked-file-dialog-size", "800:500");
    public static StringProperty BLOCKED_FILE_DIALOG_TABLE_WIDTH = addStrProp("blocked-file-dialog-table-width");
    public static StringProperty BLOCKED_FILE_DIALOG_TABLE_SORT = addStrProp("blocked-file-dialog-table-sort");
    public static StringProperty BLOCKED_FILE_DIALOG_TABLE_UP_DOWN = addStrProp("blocked-file-dialog-table-up-down");
    public static StringProperty BLOCKED_FILE_DIALOG_TABLE_VIS = addStrProp("blocked-file-dialog-table-vis");
    public static StringProperty BLOCKED_FILE_DIALOG_TABLE_ORDER = addStrProp("blocked-file-dialog-table-order");

    // CheckBackup
    public static StringProperty CHECK_BACKUP_DIALOG_SIZE = addStrProp("chack-backup-dialog-size", "800:500");
    public static DoubleProperty CHECK_BACKUP_SPLIT_DIVIDER = addDoubleProp("check-backup-split-divider", 0.3);
    public static StringProperty CHECK_FILES_TABLE_WIDTH = addStrProp("check-files-table-width");
    public static StringProperty CHECK_FILES_TABLE_SORT = addStrProp("check-files-table-sort");
    public static StringProperty CHECK_FILES_TABLE_UP_DOWN = addStrProp("check-files-table-up-down");
    public static StringProperty CHECK_FILES_TABLE_VIS = addStrProp("check-files-table-vis");
    public static StringProperty CHECK_FILES_TABLE_ORDER = addStrProp("check-files-table-order");

    // SearchInBackup
    public static DoubleProperty SHOW_BACKUP_SPLIT_DIVIDER = addDoubleProp("show-backup-split-divider", 0.3);
    public static StringProperty BACKUP_FILES_TABLE_WIDTH = addStrProp("backup-files-table-width");
    public static StringProperty BACKUP_FILES_TABLE_SORT = addStrProp("backup-files-table-sort");
    public static StringProperty BACKUP_FILES_TABLE_UP_DOWN = addStrProp("backup-files-table-up-down");
    public static StringProperty BACKUP_FILES_TABLE_VIS = addStrProp("backup-files-table-vis");
    public static StringProperty BACKUP_FILES_TABLE_ORDER = addStrProp("backup-files-table-order");

    // BackupInfo
    public static StringProperty BACKUP_INFO_TABLE_WIDTH = addStrProp("backup-info-table-width");
    public static StringProperty BACKUP_INFO_TABLE_SORT = addStrProp("backup-info-table-sort");
    public static StringProperty BACKUP_INFO_TABLE_UP_DOWN = addStrProp("backup-info-table-up-down");
    public static StringProperty BACKUP_INFO_TABLE_VIS = addStrProp("backup-info-table-vis");
    public static StringProperty BACKUP_INFO_TABLE_ORDER = addStrProp("backup-info-table-order");

    // SmallBackupDialog
    public static StringProperty SYSTEM_SIZE_SMALL_GUI = addStrProp("system-size-small-gui");
    public static BooleanProperty SYSTEM_SMALL_GUI_SHOW_START_HELP = addBoolProp("system-small-gui-show-start-help", false);

    // BigBackupDialog
    public static StringProperty SYSTEM_SIZE_BIG_GUI = addStrProp("system-size-big-gui", "1000:800");


    // ConfigDialog
    public static StringProperty CONFIG_DIALOG_SIZE = addStrProp("config-dialog-size", "900:700");
    public static BooleanProperty CONFIG_DIALOG_ACCORDION = addBoolProp("config_dialog-accordion", Boolean.TRUE);


    // CSS-Color
    public static BooleanProperty SYSTEM_CHANGE_THEME_TIME = addBoolProp("system-change-theme-time", Boolean.FALSE);
    public static IntegerProperty SYSTEM_CHANGE_TO_DARK_THEME_HOUR = addIntProp("system-change-to-dark-theme-hour", 20);
    public static IntegerProperty SYSTEM_CHANGE_TO_DARK_THEME_MINUTE = addIntProp("system-change-to-dark-theme-minute", 0);

    public static IntegerProperty SYSTEM_FONT_SIZE = addIntProp("system-style-size", 0);
    public static BooleanProperty SYSTEM_FONT_SIZE_CHANGE = addBoolProp("system-font-size-change", Boolean.FALSE); // für die Schriftgröße

    public static BooleanProperty SYSTEM_DARK_THEME = addBoolProp("system-dark-theme", Boolean.FALSE); // DARK oder LIGHT
    public static BooleanProperty SYSTEM_GUI_THEME_1 = addBoolProp("system-gui-theme-1", Boolean.FALSE); // Theme 1 oder 2
    public static StringProperty SYSTEM_CSS_ADDER = addStrProp("system-css-adder");

    public static BooleanProperty SYSTEM_DARK_START = addBoolProp("system-dark-theme-start", Boolean.FALSE);
    public static BooleanProperty SYSTEM_GUI_THEME_1_START = addBoolProp("system-gui-theme-1-start", Boolean.FALSE);

    public static BooleanProperty SYSTEM_THEME_CHANGED = addBoolProp("system-theme-changed"); // hat sich geändert
    public static StringProperty SYSTEM_ICON_COLOR = addStrProp("system-icon-color", ProgConst.ICON_COLOR_LIGHT_1); // die aktuelle Icon-Farbe
    public static StringProperty SYSTEM_GUI_COLOR = addStrProp("system-gui-color", ProgConst.GUI_COLOR_LIGHT_1); // die aktuelle GUI-Farbe
    public static StringProperty SYSTEM_BACKGROUND_COLOR = addStrProp("system-backup-color", ProgConst.GUI_BACKGROUND_LIGHT_1); // die aktuelle Hintergrund-Farbe
    public static StringProperty SYSTEM_TITLE_BAR_COLOR = addStrProp("system-backup-color", ProgConst.GUI_TITLE_BAR_LIGHT_1); // die aktuelle Hintergrund-Farbe
    public static StringProperty SYSTEM_TITLE_BAR_SEL_COLOR = addStrProp("system-backup-color", ProgConst.GUI_TITLE_BAR_SEL_LIGHT_1); // die aktuelle Hintergrund-Farbe

    public static StringProperty SYSTEM_ICON_THEME_DARK_1 = addStrProp("system-icon-theme-dark-1", ProgConst.ICON_COLOR_DARK_1);
    public static StringProperty SYSTEM_ICON_THEME_DARK_2 = addStrProp("system-icon-theme-dark-2", ProgConst.ICON_COLOR_DARK_2);
    public static StringProperty SYSTEM_ICON_THEME_LIGHT_1 = addStrProp("system-icon-theme-light-1", ProgConst.ICON_COLOR_LIGHT_1);
    public static StringProperty SYSTEM_ICON_THEME_LIGHT_2 = addStrProp("system-icon-theme-light-2", ProgConst.ICON_COLOR_LIGHT_2);

    public static StringProperty SYSTEM_GUI_THEME_DARK_1 = addStrProp("system-gui-theme-dark-1", ProgConst.GUI_COLOR_DARK_1);
    public static StringProperty SYSTEM_GUI_THEME_DARK_2 = addStrProp("system-gui-theme-dark-2", ProgConst.GUI_COLOR_DARK_2);
    public static StringProperty SYSTEM_GUI_THEME_LIGHT_1 = addStrProp("system-gui-theme-light-1", ProgConst.GUI_COLOR_LIGHT_1);
    public static StringProperty SYSTEM_GUI_THEME_LIGHT_2 = addStrProp("system-gui-theme-light-2", ProgConst.GUI_COLOR_LIGHT_2);

    public static BooleanProperty SYSTEM_GUI_BACKGROUND_TRANSPARENT_DARK_1 = addBoolProp("system-gui-background-transparent-dark-1", ProgConst.GUI_BACKGROUND_TRANSPARENT_DARK_1);
    public static BooleanProperty SYSTEM_GUI_BACKGROUND_TRANSPARENT_DARK_2 = addBoolProp("system-gui-background-transparent-dark-2", ProgConst.GUI_BACKGROUND_TRANSPARENT_DARK_2);
    public static BooleanProperty SYSTEM_GUI_BACKGROUND_TRANSPARENT_LIGHT_1 = addBoolProp("system-gui-background-transparent-light-1", ProgConst.GUI_BACKGROUND_TRANSPARENT_LIGHT_1);
    public static BooleanProperty SYSTEM_GUI_BACKGROUND_TRANSPARENT_LIGHT_2 = addBoolProp("system-gui-background-transparent-light-2", ProgConst.GUI_BACKGROUND_TRANSPARENT_LIGHT_2);

    public static StringProperty SYSTEM_GUI_BACKGROUND_DARK_1 = addStrProp("system-gui-background-dark-1", ProgConst.GUI_BACKGROUND_DARK_1);
    public static StringProperty SYSTEM_GUI_BACKGROUND_DARK_2 = addStrProp("system-gui-background-dark-2", ProgConst.GUI_BACKGROUND_DARK_2);
    public static StringProperty SYSTEM_GUI_BACKGROUND_LIGHT_1 = addStrProp("system-gui-background-light-1", ProgConst.GUI_BACKGROUND_LIGHT_1);
    public static StringProperty SYSTEM_GUI_BACKGROUND_LIGHT_2 = addStrProp("system-gui-background-light-2", ProgConst.GUI_BACKGROUND_LIGHT_2);

    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_TRANSPARENT_DARK_1 = addBoolProp("system-gui-title-bar-transparent-dark-1", ProgConst.GUI_TITLE_BAR_TRANSPARENT_DARK_1);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_TRANSPARENT_DARK_2 = addBoolProp("system-gui-title-bar-transparent-dark-2", ProgConst.GUI_TITLE_BAR_TRANSPARENT_DARK_2);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_TRANSPARENT_LIGHT_1 = addBoolProp("system-gui-title-bar-transparent-light-1", ProgConst.GUI_TITLE_BAR_TRANSPARENT_LIGHT_1);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_TRANSPARENT_LIGHT_2 = addBoolProp("system-gui-title-bar-transparent-light-2", ProgConst.GUI_TITLE_BAR_TRANSPARENT_LIGHT_2);

    public static StringProperty SYSTEM_GUI_TITLE_BAR_DARK_1 = addStrProp("system-gui-title-bar-dark-1", ProgConst.GUI_TITLE_BAR_DARK_1);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_DARK_2 = addStrProp("system-gui-title-bar-dark-2", ProgConst.GUI_TITLE_BAR_DARK_2);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_LIGHT_1 = addStrProp("system-gui-title-bar-light-1", ProgConst.GUI_TITLE_BAR_LIGHT_1);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_LIGHT_2 = addStrProp("system-gui-title-bar-light-2", ProgConst.GUI_TITLE_BAR_LIGHT_2);

    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_1 = addBoolProp("system-gui-title-bar-sel-transparent-dark-1", ProgConst.GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_1);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_2 = addBoolProp("system-gui-title-bar-sel-transparent-dark-2", ProgConst.GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_2);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_1 = addBoolProp("system-gui-title-bar-sel-transparent-light-1", ProgConst.GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_1);
    public static BooleanProperty SYSTEM_GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_2 = addBoolProp("system-gui-title-bar-sel-transparent-light-2", ProgConst.GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_2);

    public static StringProperty SYSTEM_GUI_TITLE_BAR_SEL_DARK_1 = addStrProp("system-gui-title-bar-sel-dark-1", ProgConst.GUI_TITLE_BAR_SEL_DARK_1);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_SEL_DARK_2 = addStrProp("system-gui-title-bar-sel-dark-2", ProgConst.GUI_TITLE_BAR_SEL_DARK_2);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_SEL_LIGHT_1 = addStrProp("system-gui-title-bar-sel-light-1", ProgConst.GUI_TITLE_BAR_SEL_LIGHT_1);
    public static StringProperty SYSTEM_GUI_TITLE_BAR_SEL_LIGHT_2 = addStrProp("system-gui-title-bar-sel-light-2", ProgConst.GUI_TITLE_BAR_SEL_LIGHT_2);
    // Einstellungen Filmliste
    public static BooleanProperty SYSTEM_LOAD_FILMS_ON_START = addBoolProp("system-load-films-on-start", Boolean.TRUE);
    public static StringProperty SYSTEM_LOAD_NOT_SENDER = addStrProp("system-load-not-sender", "");
    public static IntegerProperty SYSTEM_LOAD_FILMLIST_MAX_DAYS = addIntProp("system-load-filmlist-max-days", 0); //es werden nur die x letzten Tage geladen
    public static IntegerProperty SYSTEM_LOAD_FILMLIST_MIN_DURATION = addIntProp("system-load-filmlist-min-duration", 0); //es werden nur Filme mit mind. x Minuten geladen


    static {
        addComment("Shortcuts");
    }

    // =====================================================
    // =====================================================
    static {
        check(SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SECOND, SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SECOND_INIT, 5, 200);
        check(SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART, SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_INIT, 0, 10);
        check(SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP, SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP_INIT, 0, 10);
        check(SYSTEM_PARAMETER_DOWNLOAD_CONTINUE_IN_SECONDS, SYSTEM_PARAMETER_DOWNLOAD_CONTINUE_IN_SECONDS_INIT, 5, 200);
        check(SYSTEM_PARAMETER_AUTOMODE_QUITT_IN_SECONDS, SYSTEM_PARAMETER_AUTOMODE_QUITT_IN_SECONDS_INIT, 5, 200);
        check(SYSTEM_PARAMETER_DOWNLOAD_ERRORMSG_IN_SECOND, SYSTEM_PARAMETER_DOWNLOAD_ERRORMSG_IN_SECOND_INIT, 5, 200);
    }

    private static synchronized void check(IntegerProperty mlConfigs, int init, int min, int max) {
        final int v = mlConfigs.getValue();
        if (v < min || v > max) {
            mlConfigs.setValue(init);
        }
    }
}
