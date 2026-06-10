package de.p2tools.p2backup.controller;

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfoList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.sqlite.SqlBackupInfo;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;

import java.nio.file.Path;

public class LoadFactory {
    private LoadFactory() {
    }

    public static void loadAllBackupInfo() {
        BackupInfoList tmp = new BackupInfoList();

        ProgData.getInstance().dbDataList.forEach(dbData -> {
            if (!dbData.getPath().isEmpty() &&
                    Path.of(dbData.getPath()).toFile().exists() &&
                    Path.of(FileFactory.getBackupDbPath(dbData.getPath())).toFile().exists()) {
                // sonst ists noch nicht gelaufen
                BackupInfo backupInfo = SqlBackupInfo.readBackupInfo(dbData.getPath());
                if (backupInfo != null) {
                    tmp.add(backupInfo);

                } else {
                    P2Alert.showErrorAlert(ProgData.getInstance().primaryStage, "Backup laden",
                            "Konnte die Backup-Einstellungen für:" +
                                    "\n\n" +
                                    dbData.getName() +
                                    "\n\n" +
                                    "nicht laden.");
                }

            } else {
                // dann ist es noch nicht gelaufen
                BackupInfo backupInfo = new BackupInfo();
                backupInfo.setName(dbData.getName());
                backupInfo.setBackupPath(dbData.getPath());
                backupInfo.setLastStartDate(dbData.getLastStartDate());
                tmp.add(backupInfo);
            }
        });

        ProgData.getInstance().backupInfoList.setAll(tmp);
    }

    public static void loadBackupInfo(BackupInfo backupInfo) {
        // Dialog zum Laden eines Backups
        String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, ProgConfig.SYSTEM_TO_PATH.get());
        if (path.isEmpty()) {
            return;
        }

        if (!Path.of(path).toFile().exists() ||
                !Path.of(FileFactory.getBackupDbPath(path)).toFile().exists()) {
            return;
        }

        BackupInfo backupInfoLoad = SqlBackupInfo.readBackupInfo(path);
        if (backupInfoLoad != null) {
            if (backupInfo != null) {
                ProgData.getInstance().backupInfoList.remove(backupInfo);
            }
            ProgData.getInstance().backupInfoList.add(backupInfoLoad);
            ProgData.getInstance().backupInfoProperty.set(backupInfoLoad);

        } else {
            P2Alert.showErrorAlert(ProgData.getInstance().primaryStage, "Backup laden",
                    "Konnte die Backup-Einstellungen von:" +
                            "\n\n" +
                            path +
                            "\n\n" +
                            "nicht laden.");

        }
    }

    public static void reLoadBackupInfo(BackupInfo backupInfo) {
        String path = backupInfo.getBackupPath();
        if (path.isEmpty()) {
            loadBackupInfo(backupInfo);
            return;
        }

        if (!Path.of(path).toFile().exists()) {
            loadBackupInfo(backupInfo);
            return;
        }

        if (!Path.of(FileFactory.getBackupDbPath(path)).toFile().exists()) {
            loadBackupInfo(backupInfo);
            return;
        }

        BackupInfo backupInfoLoad = SqlBackupInfo.readBackupInfo(path);
        if (backupInfoLoad != null) {
            ProgData.getInstance().backupInfoList.remove(backupInfo);
            ProgData.getInstance().backupInfoList.add(backupInfoLoad);
            ProgData.getInstance().backupInfoProperty.set(backupInfoLoad);

        } else {
            P2Alert.showErrorAlert(ProgData.getInstance().primaryStage, "Backup laden",
                    "Das Backup wurde gefunden. Die Backup-Einstellungen von:" +
                            "\n\n" +
                            path +
                            "\n\n" +
                            "konnten aber nicht geladen werden.");
        }
    }
}

