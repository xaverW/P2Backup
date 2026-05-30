package de.p2tools.p2backup.controller.runner.backuprunner;

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupdata.BackupDataList;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
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

    public static boolean collectInfos(BackupInfo backupInfo) {
        backupInfo.runnerDto.initRunner();

        if (!checkName(backupInfo)) {
            return false;
        }
        if (!checkFrom(backupInfo)) {
            return false;
        }
        if (!checkBackupPath(backupInfo)) {
            return false;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String subPath = FileFactory.initSubPath(localDateTime);
        Path toPath = FileFactory.getToPath(backupInfo, subPath);

        AtomicBoolean a = new AtomicBoolean(true);
        Platform.runLater(() -> {
            // ändert das GUI
            backupInfo.setLastStartDate(localDateTime);
            a.set(false);
        });
        while (a.get()) {
            P2Wait.pause(100);
        }

        backupInfo.runnerDto.setDataSubPath(subPath);
        backupInfo.runnerDto.setToPath(toPath);

        return true;
    }

    private static boolean checkName(BackupInfo backupInfo) {
        if (backupInfo.getName().isEmpty()) {
            P2AlertAppThread.getTextAlert("Backup", "Name fürs Backup",
                    "Bitte einen Namen für das Backup vergeben.",
                    "Name:", backupInfo.nameProperty());
            if (backupInfo.nameProperty().getValueSafe().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkFrom(BackupInfo backupInfo) {
        if (backupInfo.getPathListFrom().isEmpty()) {
            P2AlertAppThread.showErrorAlert("Backup",
                    "Es sind keine Verzeichnisse zum Sichern angeben.");
            return false;
        }

        for (PathData p : backupInfo.getPathListFrom()) {
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

    private static boolean checkBackupPath(BackupInfo backupInfo) {
        if (backupInfo.getBackupPath().isEmpty()) {
            if (!P2AlertAppThread.showAlertOkCancel("Zielordner",
                    "Zielordner ist nicht angegeben!",
                    "Soll der Ordner angelegt \n" +
                            "oder das Backup abgebrochen werden?")) {
                return false;
            }

            String backupPath = P2DirFileChooserAppThread.DirChooser(ProgData.getInstance().primaryStage, "");
            if (backupInfo.getBackupPath().isEmpty()) {
                return false;
            }

            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            Platform.runLater(() -> {
                // wird im GUI angezeigt
                backupInfo.setBackupPath(backupPath);
                atomicBoolean.set(false);
            });
            while (atomicBoolean.get()) {
                P2Wait.pause(500);
            }
        }

        return true;
    }

    public static boolean makeBackupDirectory(BackupInfo backupInfo) {
        File backupPath = new File(backupInfo.getBackupPath());

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

    public static boolean makeToPathDirectory(BackupInfo backupInfo) {
        Path toPath = backupInfo.runnerDto.getToPath();

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

    public static boolean makeFromHash(BackupInfo backupInfo) {
        // Daten einlesen und ins DTO schreiben
        try {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            CreateDataHash.create(backupInfo,
                    backupInfo.runnerDto.getDirFileList(),
                    backupInfo.runnerDto.getDataFileList(),
                    true, false, atomicBoolean);

            while (atomicBoolean.get()) {
                P2Wait.pause(500);
            }

            // toPath eintragen
            final String toPath = backupInfo.runnerDto.getToPath().toString();
            for (FileData fileData : backupInfo.runnerDto.getDataFileList()) {
                fileData.setToPathStr(toPath);
            }

            if (backupInfo.runnerDto.isStop()) {
                return false;
            }
            backupInfo.runnerDto.resetRunnerMax(backupInfo.runnerDto.getDataFileList().size());

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

        // wieder neu auf Anfang setzen
        backupInfo.runnerDto.resetRunnerMax(backupInfo.runnerDto.getDataFileList().getSize());
        // ====================
        // BackupPfad nochmal prüfen, ist doppelt, schadet aber nicht
        if (!CopyFactory.checkToPath(FileFactory.getToPath(backupInfo))) {
            return false;
        }

        if (backupInfo.getBackupDataList().isEmpty()) {
            // dann gibts keinen Vorgänger -> alles kopieren
            return CopyFactory.copyFiles(backupInfo, backupInfo.runnerDto.getDataFileList());
        }

        switch (backupInfo.getHow()) {
            case ProgConst.BACKUP_DIFF, ProgConst.BACKUP_INTELLIGENT ->
                    ret = CopyDiffFactory.copyDiffFilesToBackup(backupInfo);
            default -> ret = CopyFactory.copyFiles(backupInfo, backupInfo.runnerDto.getDataFileList());
        }

        backupInfo.runnerDto.setRunnerFileName("");
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

    public static boolean writeBackupData(BackupInfo backupInfo) {
        // aktuellen Hash der DATEN in der Tabelle dataFiles sichern
        if (!SqlFileData.writeDataFileList(backupInfo)) {
            return false;
        }

        // BackupData in die Liste schreiben
        backupInfo.getBackupDataList().add(backupInfo.runnerDto.getBackupData());

        // BackupInfo und alle BackupData in DB schreiben
        if (!SqlBackupInfo.addUpdateBackupInfo(backupInfo)) {
            return false;
        }

        // noch die fehlerhaften löschen
        ArrayList<FileData> removeList = new ArrayList<>();
        for (FileData fileData : backupInfo.runnerDto.getDataFileList()) {
            if (fileData.isError()) {
                removeList.add(fileData);
            }
        }

        if (!removeList.isEmpty()) {
            P2Log.errorLog(956232145, "Fehlerhafte Dateien: " + removeList.size());
            backupInfo.runnerDto.getDataFileList().removeAll(removeList);
        }
        backupInfo.runnerDto.getBackupData().setCount(backupInfo.runnerDto.getDataFileList().size());

        // BackupFile schreiben
        return SqlFileData.writeBackupFileList(backupInfo);
    }
}
