package de.p2tools.p2backup.controller.data.filedata;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.gui.dialog.CopyFileErrorDialogController;
import de.p2tools.p2lib.tools.P2Wait;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileFactory {
    // backupPath   /tmp/usb/backup
    // toPath       /tmp/usb/backup/2025-10-23__10-12-00
    // subPath      /2025-10-23__10-12-00
    // filePath     /home/emil/Desktop/daten/file1/1970/1960_05.jpg
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd__HH-mm-ss");
    public static final String HASH_ERROR = "-1";

    private FileFactory() {
    }

    public static boolean goOnError(BackupInfo backupInfo, String file, boolean isFile) {
        if (backupInfo.runnerDto.getGoAlwaysOverError()) {
            return true;
        }

        BooleanProperty yesProp = new SimpleBooleanProperty(false);
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        Platform.runLater(() -> {
            // wird im GUI angezeigt
            new CopyFileErrorDialogController(backupInfo, file, yesProp, isFile);
            atomicBoolean.set(false);
        });
        while (atomicBoolean.get()) {
            P2Wait.pause(500);
        }

        return yesProp.get();
    }

    public static void cleanFileData(List<FileData> fileList, String toPath) {
        fileList.forEach(f -> f.setFilePathStr(cleanFileData(f, toPath)));
    }

    public static String cleanFileData(FileData fileData, String toPath) {
        String path = fileData.getFilePathStr();
        if (!toPath.isEmpty() && path.startsWith(toPath)) {
            // path = path.replaceAll(toPath, ""); Win mal wieder
            path = path.substring(toPath.length());
        }
        if (!path.startsWith(File.separator)) {
            path = File.separator + path;
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        fileData.setToPathStr(toPath);

        return path;
    }

    public static String cleanFileData(String path, String toPath) {
        if (!toPath.isEmpty() && path.startsWith(toPath)) {
            path = path.replaceFirst(toPath, "");
        }
        if (!path.startsWith(File.separator)) {
            path = File.separator + path;
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static void setCorrPath(List<FileData> fileList) {
        fileList.forEach(f -> f.setFilePathStr(setCorrPath(f.getToPathStr())));
    }

    public static String setCorrPath(String path) {
        if (ProgData.getInstance().WINDOWS) {
            // Win macht einfach nur MIST!
            // C:\Documents\Newsletters\Summer2018.pdf
            // \C__\Documents\Newsletters\Summer2018.pdf
            return File.separator + path.replace(":", ProgConst.WIN_REPLACE_PATH);

        } else {
            // /Documents/Newsletters/Summer2018.pdf
            // /__Documents/Newsletters/Summer2018.pdf
            return File.separator + path.replaceFirst("/", ProgConst.WIN_REPLACE_PATH);
        }
    }

    public static void unSetCorrPath(List<FileData> fileList) {
        fileList.forEach(FileFactory::unSetCorrPath);
    }

    public static void unSetCorrPath(FileData fileData) {
        fileData.setFilePathStr(unSetCorrPath(fileData.getFilePathStr()));
    }

    public static String unSetCorrPath(String path) {
        if (ProgData.getInstance().WINDOWS) {
            // Win macht einfach nur MIST!
            // \C__\Documents\Newsletters\Summer2018.pdf
            if (path.startsWith(File.separator)) {
                // C__\Documents\Newsletters\Summer2018.pdf
                path = path.substring(1);
            }
            if (path.startsWith(ProgConst.WIN_REPLACE_PATH, 1)) {
                // C:\Documents\Newsletters\Summer2018.pdf
                // path = path.replace(ProgConst.WIN_REPLACE_PATH, ":"); sonst werden alle geändert!!
                path = path.charAt(0) + ":" + path.substring(1 + ProgConst.WIN_REPLACE_PATH.length());
            }
            return path;

        } else {
            // /__Documents/Newsletters/Summer2018.pdf
            // /Documents/Newsletters/Summer2018.pdf
            // return path.replaceFirst(ProgConst.WIN_REPLACE_PATH, ""); und wieder Win
            if (path.startsWith(File.separator)) {
                return File.separator + path.substring(ProgConst.WIN_REPLACE_PATH.length() + 1);
            } else {
                return File.separator + path.substring(ProgConst.WIN_REPLACE_PATH.length());
            }
        }
    }

//    public static String getCleanPath(String s) {
//        if (s.isEmpty()) {
//            return s;
//        }
//        if (!s.startsWith(File.separator)) {
//            s = File.separator + s;
//        }
//        if (s.endsWith(File.separator)) {
//            s = s.substring(0, s.length() - 1);
//        }
//        return s;
//    }

    // =======================
    // backup
    // =======================
    public static Path getBackupPath(BackupInfo backupInfo) {
        return Path.of(backupInfo.getBackupPath());
    }

    public static boolean backupPathExistAndNotEmpty(BackupInfo backupInfo) {
        Path backupPath = getBackupPath(backupInfo);
        if (backupPath.toFile().exists() &&
                backupPath.toFile().isDirectory() &&
                backupPath.toFile().listFiles() != null &&
                Objects.requireNonNull(backupPath.toFile().listFiles()).length > 0) {
            // dann gibts es und ist nicht leer
            return true;
        }

        return false;
    }

    // =======================
    // SQL
    // =======================
    public static String getBackupDbPath(String backupPath) {
        return Path.of(backupPath, ProgConst.CONFIG_DB_FILE).toString();
    }

    public static String getBackupDbPath(BackupInfo backupInfo) {
        return Path.of(backupInfo.getBackupPath(), ProgConst.CONFIG_DB_FILE).toString();
    }

    // =======================
    // toPath
    // =======================
    public static Path getToPath(BackupInfo backupInfo, String subPath) {
        return subPath.isEmpty() ? null : Path.of(backupInfo.getBackupPath(), subPath);
    }

    public static String getToPathStr(BackupInfo backupInfo, String subPath) {
        if (subPath.isEmpty()) {
            return "";
        }
        if (backupInfo.getBackupPath().isEmpty()) {
            return "";
        }
        return Path.of(backupInfo.getBackupPath(), subPath).toString();
    }

    public static Path getToPath(BackupInfo backupInfo, BackupData backupData) {
        return Path.of(backupInfo.getBackupPath(), backupData.getSubPath());
    }

    public static String getToPathStr(BackupInfo backupInfo, BackupData backupData) {
        return Path.of(backupInfo.getBackupPath(), backupData.getSubPath()).toString();
    }

    public static String getToPathStr(BackupInfo backupInfo) {
        return backupInfo.runnerDto.getToPath().toString();
    }

    public static Path getToPath(BackupInfo backupInfo) {
        return Path.of(backupInfo.runnerDto.getToPath().toString());
    }

    // =======================
    // dateSubPath
    // =======================
    public static String initSubPath(LocalDateTime localDateTime) {
        String strDate = localDateTime.format(DT_FORMATTER);

//        switch (backupTime) {
//            case DAY -> strPathName = DAY_NAME + "__" + strDate;
//            case WEEK -> strPathName = WEEK_NAME + "__" + strDate;
//            case MONTH -> strPathName = MONTH_NAME + "__" + strDate;
//            default -> strPathName = strDate;
//        }

        return strDate;
    }
}
