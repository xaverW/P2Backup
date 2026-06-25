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

public class ProgConst {

    public static final String PROGRAM_NAME = "P2Backup";
    // settings file
    public static final String CONFIG_FILE = "p2backup.xml";
    public static final String CONFIG_FILE_COPY = "p2backup.xml_copy_";
    public static final String CONFIG_DIRECTORY = "P2Backup"; // im Homeverzeichnis
    public static final String CONFIG_DB_FILE = "p2backup.db"; // Name der DB

    public static final String LOG_DIR = "Log";
    public static final int BACUP_VERSION = 1;

    // Website
    public static final String URL_WEBSITE = "https://www.p2tools.de/";
    public static final String URL_WEBSITE_DOWNLOAD = "https://www.p2tools.de/mtviewer/download/";
    public static final String URL_WEBSITE_HELP = "https://www.p2tools.de/mtviewer/manual/";

    // Dateien/Verzeichnisse
    public final static int MAX_COPY_OF_BACKUPFILE = 5; // Maximum number of backup files to be stored.

    public static final int MIN_TABLE_HEIGHT = 200;
    public static final int MIN_TEXTAREA_HEIGHT_LOW = 50;

    //Startnummer/Filmnummer/... wenn nicht vorhanden
    public static final int NUMBER_NOT_EXISTS = Integer.MAX_VALUE;
    public static final int PATH_ID_NOT_EXISTS = -1;


    public static final int PROGRAM_STATE_MIN = 0;
    public static final int PROGRAM_STATE_BACKUP = 0;
    public static final int PROGRAM_STATE_INFO = 1;
    public static final int PROGRAM_STATE_FROM = 2;
    public static final int PROGRAM_STATE_TO = 3;
    public static final int PROGRAM_STATE_HOW = 4;
    public static final int PROGRAM_STATE_TOOL = 5;
    public static final int PROGRAM_STATE_MAX = 5;

    public static final String WIN_REPLACE_PATH = "__";

    public static String DATA_CONST = "Meine Daten"; // im Combo für die DATEN
    public final static int BACKUP_ALL = 0;
    public final static int BACKUP_DIFF = 1;
    public final static int BACKUP_INTELLIGENT = 2;

    public final static String HOW_HELP_IMAGE_ALL = "de/p2tools/p2backup/res/how/copyAll.png";
    public final static String HOW_HELP_IMAGE_ONLY = "de/p2tools/p2backup/res/how/copyOnly.png";
    public final static String HOW_HELP_IMAGE_INTELLIGENT = "de/p2tools/p2backup/res/how/copyIntelligent.png";

    public static final String ICON_COLOR_DARK_1 = "#ffffff";
    public static final String ICON_COLOR_DARK_2 = "#000080";
    public static final String ICON_COLOR_LIGHT_1 = "#333333";
    public static final String ICON_COLOR_LIGHT_2 = "#4d66cc";

    public static final String GUI_COLOR_DARK_1 = "#cccccc";
    public static final String GUI_COLOR_DARK_2 = "#000080";
    public static final String GUI_COLOR_LIGHT_1 = "#666666";
    public static final String GUI_COLOR_LIGHT_2 = "#4d66cc";

    public static final String GUI_BACKGROUND_DARK_1 = "#333333";
    public static final String GUI_BACKGROUND_DARK_2 = "#a1a1a1";
    public static final String GUI_BACKGROUND_LIGHT_1 = "#cccccc";
    public static final String GUI_BACKGROUND_LIGHT_2 = "#d8d8d8";

    public static final String GUI_TITLE_BAR_DARK_1 = "#666666";
    public static final String GUI_TITLE_BAR_DARK_2 = "#000080";
    public static final String GUI_TITLE_BAR_LIGHT_1 = "#d8d8d8";
    public static final String GUI_TITLE_BAR_LIGHT_2 = "#8096ee";

    public static final String GUI_TITLE_BAR_SEL_DARK_1 = "#333333";
    public static final String GUI_TITLE_BAR_SEL_DARK_2 = "#414180";
    public static final String GUI_TITLE_BAR_SEL_LIGHT_1 = "#999999";
    public static final String GUI_TITLE_BAR_SEL_LIGHT_2 = "#4d66cc";

    public static final boolean GUI_BACKGROUND_TRANSPARENT_DARK_1 = true;
    public static final boolean GUI_BACKGROUND_TRANSPARENT_DARK_2 = false;
    public static final boolean GUI_BACKGROUND_TRANSPARENT_LIGHT_1 = true;
    public static final boolean GUI_BACKGROUND_TRANSPARENT_LIGHT_2 = false;

    public static final boolean GUI_TITLE_BAR_TRANSPARENT_DARK_1 = true;
    public static final boolean GUI_TITLE_BAR_TRANSPARENT_DARK_2 = false;
    public static final boolean GUI_TITLE_BAR_TRANSPARENT_LIGHT_1 = true;
    public static final boolean GUI_TITLE_BAR_TRANSPARENT_LIGHT_2 = false;

    public static final boolean GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_1 = false;
    public static final boolean GUI_TITLE_BAR_SEL_TRANSPARENT_DARK_2 = false;
    public static final boolean GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_1 = false;
    public static final boolean GUI_TITLE_BAR_SEL_TRANSPARENT_LIGHT_2 = false;

}
