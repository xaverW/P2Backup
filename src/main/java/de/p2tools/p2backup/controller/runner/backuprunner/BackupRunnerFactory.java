package de.p2tools.p2backup.controller.runner.backuprunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupdata.BackupDataList;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.runner.copyrunner.CopyAllFactory;
import de.p2tools.p2backup.controller.runner.copyrunner.CopyDiffFactory;
import de.p2tools.p2backup.controller.runner.copyrunner.CopyFactory;
import de.p2tools.p2backup.controller.runner.deleterunner.DeleteRunner;
import de.p2tools.p2backup.controller.runner.hashrunner.CreateDataHash;
import de.p2tools.p2backup.controller.sqlite.SqlBackupInfo;
import de.p2tools.p2backup.controller.sqlite.SqlFileData;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.dialogs.P2DirFileChooserAppThread;
import de.p2tools.p2lib.tools.P2Wait;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackupRunnerFactory {
    private BackupRunnerFactory() {
    }

    public static boolean collectInfos(BackupInfo backupInfos) {
        backupInfos.runnerDto.initRunner();

        if (!checkName(backupInfos)) {
            return false;
        }
        if (!checkFrom(backupInfos)) {
            return false;
        }
        if (!checkBackupPath(backupInfos)) {
            return false;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String subPath = FileFactory.initSubPath(localDateTime);
        Path toPath = FileFactory.getToPath(backupInfos, subPath);

        AtomicBoolean a = new AtomicBoolean(true);
        Platform.runLater(() -> {
            // ändert das GUI
            backupInfos.setLastStartDate(localDateTime);
            a.set(false);
        });
        while (a.get()) {
            P2Wait.pause(100);
        }

        backupInfos.runnerDto.setDataSubPath(subPath);
        backupInfos.runnerDto.setToPath(toPath);

        return true;
    }

    private static boolean checkName(BackupInfo backupInfos) {
        if (backupInfos.getName().isEmpty()) {
            P2AlertAppThread.getTextAlert("Backup", "Name fürs Backup",
                    "Bitte einen Namen für das Backup vergeben.",
                    "Name:", backupInfos.nameProperty());
            if (backupInfos.nameProperty().getValueSafe().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkFrom(BackupInfo backupInfos) {
        if (backupInfos.getPathListFrom().isEmpty()) {
            P2AlertAppThread.showErrorAlert("Backup",
                    "Es sind keine Verzeichnisse zum Sichern angeben.");
            return false;
        }

        for (PathData p : backupInfos.getPathListFrom()) {
            File path = new File(p.getPath());
            if (!path.exists()) {
                P2AlertAppThread.showErrorAlert("Backup",
                        "Das Verzeichnisse zum Sichern:\n" +
                                p.getPath() +
                                "\ngibt es nicht.");
                return false;
            }
        }
        return true;
    }

    private static boolean checkBackupPath(BackupInfo backupInfos) {
        if (backupInfos.getBackupPath().isEmpty()) {
            if (!P2AlertAppThread.showAlertOkCancel("Zielordner",
                    "Zielordner ist nicht angegeben!",
                    "Soll der Ordner angelegt \n" +
                            "oder das Backup abgebrochen werden?")) {
                return false;
            }

            String backupPath = P2DirFileChooserAppThread.DirChooser(ProgData.getInstance().primaryStage, "");
            if (backupInfos.getBackupPath().isEmpty()) {
                return false;
            }

            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            Platform.runLater(() -> {
                // wird im GUI angezeigt
                backupInfos.setBackupPath(backupPath);
                atomicBoolean.set(false);
            });
            while (atomicBoolean.get()) {
                P2Wait.pause(500);
            }
        }

        return true;
    }

    public static boolean makeBackupDirectory(BackupInfo backupInfos) {
        File backupPath = new File(backupInfos.getBackupPath());

        try {
            FileStore fileStore = Files.getFileStore(backupPath.toPath());
            String type = fileStore.type();
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("Type: " + type);
            System.out.println("========================");
            // "FAT32" oder "vfat"
            if (type.equals("FAT32") || type.equals("vfat")) {
                System.out.println("========================");
                System.out.println("Type: " + type);
                System.out.println("========================");
            }
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("========================");
            System.out.println("========================");
        } catch (IOException e) {
            System.out.println(e);
        }

        if (backupPath.exists() && backupPath.isDirectory()) {
            return true;
        }

        if (backupPath.exists() && !backupPath.isDirectory()) {
            P2AlertAppThread.showErrorAlert("Zielordner",
                    "Der Ordner für das Backup ist ist eine Datei und kein Verzeichnis.");
            return false;
        }

        try {
            if (backupPath.mkdirs() && backupPath.isDirectory()) {
                return true;
            } else {
                P2AlertAppThread.showErrorAlert("Anlegen des Zielordners",
                        "Der Zielordner konnte nicht angelegt werden");
                return false;
            }
        } catch (Exception ex) {
            P2AlertAppThread.showErrorAlert("Anlegen des Zielordners",
                    "Der Zielordner konnte nicht angelegt werden");
            return false;
        }
    }

    public static boolean makeToPathDirectory(BackupInfo backupInfos) {
        Path toPath = backupInfos.runnerDto.getToPath();

        try {
            if (Files.exists(toPath)) {
                P2AlertAppThread.showErrorAlert("Backupverzeichnis anlegen",
                        "Das Backupverzeichnis:\n" +
                                toPath + "\n" +
                                "existiert schon.");
                return false;

            } else {
                if (!toPath.toFile().mkdirs() ||
                        !toPath.toFile().isDirectory()) {

                    P2AlertAppThread.showErrorAlert("Backupverzeichnis anlegen",
                            "Kann das Backupverzeichnis:\n" +
                                    toPath.toString() + "\n" +
                                    "nicht anlegen.");
                    return false;
                }
            }

        } catch (final Exception ex) {
            P2Log.errorLog(914579541, ex);
            P2AlertAppThread.showErrorAlert("Backupverzeichnis anlegen",
                    "Kann das Backupverzeichnis:\n" +
                            toPath.toString() + "\n" +
                            "nicht anlegen.");
            return false;
        }

        return true;
    }

    public static boolean makeFromHash(BackupInfo backupInfos) {
        // Daten einlesen und ins DTO schreiben
        try {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            CreateDataHash.create(backupInfos,
                    backupInfos.runnerDto.getDirFileList(),
                    backupInfos.runnerDto.getDataFileList(),
                    false, false, atomicBoolean);

            while (atomicBoolean.get()) {
                P2Wait.pause(500);
            }

            // toPath eintragen
            final String toPath = backupInfos.runnerDto.getToPath().toString();
            for (FileData fileData : backupInfos.runnerDto.getDataFileList()) {
                fileData.setToPathStr(toPath);
            }

            if (backupInfos.runnerDto.isStop()) {
                return false;
            }

            // aktuellen Hash der DATEN in der Tabelle dataFiles sichern
            if (!SqlFileData.writeDataFileList(backupInfos)) {
                return false;
            }

            // und jetzt noch die fehlerhaften löschen
            ArrayList<FileData> removeList = new ArrayList<>();
            backupInfos.runnerDto.getDataFileList().forEach(f -> {
                if (f.isError()) {
                    removeList.add(f);
                }
            });
            if (!removeList.isEmpty()) {
                P2Log.errorLog(956232145, "Fehlerhafte Dateien: " + removeList.size());
                backupInfos.runnerDto.getDataFileList().removeAll(removeList);
            }

        } catch (Exception ex) {
            P2AlertAppThread.showErrorAlert("Hash erstellen ",
                    "Konnte den Hash der Dateien " +
                            "nicht erstellen.");
            return false;
        }

        return true;
    }

    public static boolean copyFilesToBackup(BackupInfo backupInfo) {
        boolean ret;

        // ====================
        // BackupPfad nochmal prüfen, ist doppelt, schadet aber nicht
        if (!CopyFactory.checkToPath(FileFactory.getToPath(backupInfo))) {
            return false;
        }

        if (backupInfo.getBackupDataList().isEmpty()) {
            // dann gibts keinen Vorgänger -> alles kopieren
            return CopyAllFactory.copyAllFilesToBackup(backupInfo);
        }

        switch (backupInfo.getHow()) {
            case ProgConst.BACKUP_ALL -> ret = CopyAllFactory.copyAllFilesToBackup(backupInfo);
            case ProgConst.BACKUP_DIFF -> ret = CopyDiffFactory.copyDiffFilesToBackup(backupInfo);
            case ProgConst.BACKUP_INTELLIGENT -> ret = CopyDiffFactory.copyDiffFilesToBackup(backupInfo);
            default -> ret = CopyAllFactory.copyAllFilesToBackup(backupInfo);
        }
        return ret;
    }

    public static boolean deleteBackupData(BackupInfo backupInfo) {
        boolean ret = true;
        BackupDataList backupDataList = backupInfo.getBackupDataList();
        int sum = backupInfo.getSumDay();
        while (backupDataList.size() > 1 && backupDataList.size() >= sum) {
            // dann die überzähligen löschen, das aktuelle ist ja noch nicht drin
            BackupData backupData = backupDataList.get(0);
            if (!new DeleteRunner(backupInfo, backupData).deleteBackupDoNotAsk()) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    public static boolean updateBackupData(BackupInfo backupInfo) {
        // BackupData in die Liste schreiben
        backupInfo.getBackupDataList().add(backupInfo.runnerDto.getBackupData());
        // und jetzt BackupInfo und alle BackupData in DB schreiben
        if (!SqlBackupInfo.addUpdateBackupInfo(backupInfo)) {
            return false;
        }
        return SqlFileData.writeBackupFileList(backupInfo);
    }
}
