package de.p2tools.p2backup.controller.data.filedata;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.gui.dialog.CopyFileErrorDialogController;
import de.p2tools.p2lib.tools.P2ToolsFactory;
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

    public static boolean goOnError(BackupInfo backupInfos, String file, boolean isFile) {
        if (backupInfos.runnerDto.getGoAlwaysOverError()) {
            return true;
        }

        BooleanProperty yesProp = new SimpleBooleanProperty(false);
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        Platform.runLater(() -> {
            // wird im GUI angezeigt
            new CopyFileErrorDialogController(backupInfos, file, yesProp, isFile);
            atomicBoolean.set(false);
        });
        while (atomicBoolean.get()) {
            P2ToolsFactory.pause(500);
        }

        return yesProp.get();
    }

    public static void cleanFileData(List<FileData> fileList, String toPath) {
        fileList.forEach(f -> f.setFilePathStr(cleanFileData(f, toPath)));
    }

    public static String cleanFileData(FileData fileData, String toPath) {
        String path = fileData.getFilePathStr();
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
            return File.separator + path.replace(":", ProgConst.WIN_REPLACE_PATH);

        } else {
            // /Documents/Newsletters/Summer2018.pdf
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
            // C:\Documents\Newsletters\Summer2018.pdf
            if (path.startsWith(File.separator)) {
                // \C__\Documents\Newsletters\Summer2018.pdf
                path = path.substring(1);
            }
            return path.replace(ProgConst.WIN_REPLACE_PATH, ":");

        } else {
            // /Documents/Newsletters/Summer2018.pdf
            // /__Documents/Newsletters/Summer2018.pdf
            return path.replaceFirst(ProgConst.WIN_REPLACE_PATH, "");
        }
    }

    public static String getCleanPath(String s) {
        if (s.isEmpty()) {
            return s;
        }
        if (!s.startsWith(File.separator)) {
            s = File.separator + s;
        }
        if (s.endsWith(File.separator)) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    // =======================
    // backup
    // =======================
    public static Path getBackupPath(BackupInfo backupInfos) {
        return Path.of(backupInfos.getBackupPath());
    }

    public static boolean backupPathExistAndNotEmpty(BackupInfo backupInfos) {
        Path backupPath = getBackupPath(backupInfos);
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

    public static String getBackupDbPath(BackupInfo backupInfos) {
        return Path.of(backupInfos.getBackupPath(), ProgConst.CONFIG_DB_FILE).toString();
    }

    // =======================
    // toPath
    // =======================
    public static Path getToPath(BackupInfo backupInfos, String subPath) {
        return subPath.isEmpty() ? null : Path.of(backupInfos.getBackupPath(), subPath);
    }

    public static String getToPathStr(BackupInfo backupInfos, String subPath) {
        if (subPath.isEmpty()) {
            return "";
        }
        if (backupInfos.getBackupPath().isEmpty()) {
            return "";
        }
        return Path.of(backupInfos.getBackupPath(), subPath).toString();
    }

    public static Path getToPath(BackupInfo backupInfos, BackupData backupData) {
        return Path.of(backupInfos.getBackupPath(), backupData.getSubPath());
    }

    public static String getToPathStr(BackupInfo backupInfos, BackupData backupData) {
        return Path.of(backupInfos.getBackupPath(), backupData.getSubPath()).toString();
    }

    public static String getToPathStr(BackupInfo backupInfos) {
        return backupInfos.runnerDto.getToPath().toString();
    }

    public static Path getToPath(BackupInfo backupInfos) {
        return Path.of(backupInfos.runnerDto.getToPath().toString());
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

//    public static String getLastSubPath(BackupInfo backupInfos) {
//        ObservableList<String> fileNameList = getAllSubPath(backupInfos);
//        if (!fileNameList.isEmpty()) {
//            return fileNameList.get(0);
//        } else {
//            return "";
//        }
//    }
//
//    public static ObservableList<String> getAllSubPath(BackupInfo backupInfos) {
//        ObservableList<String> fileNameList = FXCollections.observableArrayList();
//        Path path = null;
//        try {
//            for (BackupData backupData : backupInfos.getBackupDataList()) {
//                path = FileFactory.getToPath(backupInfos, backupData);
//                if (path.toFile().exists()) {
//                    fileNameList.add(path.getFileName().toString());
//                } else {
//                    P2AlertAppThread.showErrorAlert("Kann die Backup-Ordner nicht lesen!",
//                            "Kann Dateien des Ordners \"" + backupInfos.getBackupPath() + "\" nicht lesen");
//                }
//            }
//        } catch (Exception ex) {
//            P2AlertAppThread.showErrorAlert(ProgData.getInstance().primaryStage,
//                    "Kann die Backup-Ordner nicht lesen!",
//                    "Kann Dateien des Ordners \"" + backupInfos.getBackupPath() +
//                            (path != null ? (" - " + path) : "") +
//                            "\" nicht lesen");
//        }
//
//        fileNameList.sort(Collections.reverseOrder());
//        return fileNameList;
//    }
}
