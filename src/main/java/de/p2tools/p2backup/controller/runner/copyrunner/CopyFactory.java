package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;

public class CopyFactory {
    private CopyFactory() {
    }

    public static boolean checkToPath(Path toDataPath) {
        if (toDataPath == null ||
                !toDataPath.toFile().isDirectory() ||
                !toDataPath.toFile().exists()) {
            P2AlertAppThread.showErrorAlert("Dateien kopieren", "Der Zielordner\n" +
                    toDataPath + "\n" +
                    "kann nicht angelegt werden.");
            return false;
        }
        if (toDataPath.toFile().listFiles().length != 0) {
            P2AlertAppThread.showErrorAlert("Dateien kopieren", "Der Zielordner\n" +
                    toDataPath + "\n" +
                    "ist nicht leer und kann so nicht verwendet werden.");
            return false;
        }

        return true;
    }

    public static boolean makeDirsOfFile(List<FileData> fileDataList, Path toDataPath) {
        // erst mal die Dirs anlegen
        final HashSet<File> dirSet = new HashSet<>();
        for (FileData fileData : fileDataList) {
            // toFilePath:  /tmp/usb/backup/2025-10-21__16-29-29/__home/emil/Desktop/daten/file2/1972/bild.jpg
            // toPath:      /tmp/usb/backup/2025-10-23__10-12-00
            // filePath:    /home/emil/Desktop/daten/file1/1970/1960_05.jpg
            File parentDir = fileData.getParentBackupFilePath().toFile();
            dirSet.add(parentDir);
        }

        for (File f : dirSet) {
            if (!f.exists() && !f.mkdirs() && !f.isDirectory()) {
                P2AlertAppThread.showErrorAlert("Dateien kopieren", "Konnte den Zielordner\n" +
                        f + "\n" +
                        "nicht anlegen");
                return false;
            }
        }

        return true;
    }

    public static boolean copyFiles(BackupInfo backupInfo,
                                    List<FileData> fileDataList) {

        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        backupInfo.runnerDto.setRunnerMax(fileDataList.size());
        backupInfo.runnerDto.setRunnerDone(0);
        int ready = 0;

        for (FileData fileData : fileDataList) {
            // toFilePath:  /tmp/usb/backup/2025-10-21__16-29-29/Daten/home/emil/Desktop/daten/file2/1972/bild.jpg
            // toPath:      /tmp/usb/backup/2025-10-23__10-12-00
            // filePath:    /home/emil/Desktop/daten/file1/1970/1960_05.jpg

            if (fileData.isError()) {
                continue;
            }

            if (backupInfo.runnerDto.isStop()) {
                return false;
            }

            if (fileData.getFilePathStr().isEmpty() ||
                    fileData.getToPathStr().isEmpty()) {
                // dann nix
                continue;
            }

            Path fromPath = fileData.getFilePath(); // ist der aktuelle Pfad zum File
            Path toFilePath = fileData.getBackupFilePath(); // neue Speicherpfad

            try {
                // und jetzt den toData Pfad wieder setzen
                backupInfo.runnerDto.setRunnerFileName(fromPath.toString());
//                Files.copy(fromPath, toFilePath, StandardCopyOption.COPY_ATTRIBUTES);
                FileUtils.copyFile(fromPath.toFile(), toFilePath.toFile(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception ex) {
                if (!FileFactory.goOnError(backupInfo, fromPath.toString())) {
                    return false;
                }
            }

            ++ready;
            backupInfo.runnerDto.setRunnerDone(ready);
        }
        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        return true;
    }

    public static boolean moveFiles(BackupInfo backupInfo, String oldToPath,
                                    FileDataList fileDataList) {
        // move
        for (FileData f : fileDataList) {
            if (f.isError()) {
                continue;
            }

            File fromFile = f.getBackupFilePath(oldToPath).toFile();
            File toFile = f.getBackupFilePath().toFile();
            try {
                FileUtils.moveFileToDirectory(fromFile, toFile.getParentFile(), true);
            } catch (IOException e) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean linkFiles(BackupInfo backupInfo, String oldToPath,
                                    List<FileData> fileDataList) {
        // in der FileDataList sind die Dateien aus dem Backup die verlinkt werden
        for (FileData f : fileDataList) {
            if (f.isError()) {
                continue;
            }

            Path fromFile = f.getBackupFilePath(oldToPath);
            Path toFile = f.getBackupFilePath();

            try {
                // create a hard link
                FileUtils.createParentDirectories(toFile.toFile());
                Files.createLink(toFile, fromFile);
            } catch (Exception ex) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString())) {
                    return false;
                }
            }
        }
        return true;
    }
}
