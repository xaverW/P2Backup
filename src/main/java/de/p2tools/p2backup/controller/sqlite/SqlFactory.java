package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SqlFactory {
    //    public static final DateTimeFormatter DT_FORMATTER_SQL = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS"); // kommts beim .MIN zum Überlauf
    public static final DateTimeFormatter DT_FORMATTER_SQL = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

    private SqlFactory() {
    }

    public static String getUrl(String backupPath) {
        if (backupPath.isEmpty()) {
            P2AlertAppThread.showErrorAlert("Datenbank anlegen",
                    "Kann die Backup-Datenbank nicht speichern");
            return "";
        } else {
            String path = FileFactory.getBackupDbPath(backupPath);
            return "jdbc:sqlite:" + path;
        }
    }

    public static String getUrl(BackupInfo backupInfo) {
        String path;
        if (backupInfo.getBackupPath().isEmpty()) {
            P2AlertAppThread.showErrorAlert("Datenbank anlegen",
                    "Kann die Backup-Datenbank nicht speichern");
            return "";
        } else {
            path = FileFactory.getBackupDbPath(backupInfo);
            return "jdbc:sqlite:" + path;
        }
    }

    public static LocalDateTime getLocalDateTime(String strDate) {
        if (strDate == null || strDate.isEmpty()) {
            return LocalDateTime.MIN;
        }

        try {
            return LocalDateTime.parse(strDate, DT_FORMATTER_SQL);
        } catch (final Exception ex) {
        }

        return LocalDateTime.MIN;
    }

    public static String fromLocalDate(LocalDateTime localDateTime) {
        try {
            return localDateTime.format(DT_FORMATTER_SQL);
        } catch (final Exception ex) {
        }

        return "";
    }

}
