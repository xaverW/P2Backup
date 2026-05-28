package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.FileHashFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    public static boolean copyFiles(BackupInfo backupInfo,
                                    List<FileData> fileDataList) {

        backupInfo.runnerDto.setRunnerText("Dateien kopieren");
        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        for (FileData fileData : fileDataList) {
            // toFilePath:  /tmp/usb/backup/2025-10-21__16-29-29/Daten/home/emil/Desktop/daten/file2/1972/bild.jpg
            // toPath:      /tmp/usb/backup/2025-10-23__10-12-00
            // filePath:    /home/emil/Desktop/daten/file1/1970/1960_05.jpg

            FileHashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setError(fileData.getHash().equals(FileFactory.HASH_ERROR));
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
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerDone();
                FileUtils.copyFile(fromPath.toFile(), toFilePath.toFile(), StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception ex) {
                if (!FileFactory.goOnError(backupInfo, fromPath.toString(), true)) {
                    return false;
                }
            }
        }

        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        return true;
    }

    public static boolean moveFiles(BackupInfo backupInfo, String oldToPath,
                                    FileDataList fileDataList) {
        // move
        for (FileData fileData : fileDataList) {

            FileHashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setError(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isError()) {
                continue;
            }

            File fromFile = fileData.getBackupFilePath(oldToPath).toFile();
            File toFile = fileData.getBackupFilePath().toFile();
            try {
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerDone();
                FileUtils.moveFileToDirectory(fromFile, toFile.getParentFile(), true);
            } catch (IOException e) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString(), true)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean linkFiles(BackupInfo backupInfo, String oldToPath,
                                    List<FileData> fileDataList) {
        // in der FileDataList sind die Dateien aus dem Backup die verlinkt werden
        for (FileData fileData : fileDataList) {

            FileHashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setError(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isError()) {
                continue;
            }

            Path fromFile = fileData.getBackupFilePath(oldToPath);
            Path toFile = fileData.getBackupFilePath();

            try {
                // create a hard link
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerDone();
                FileUtils.createParentDirectories(toFile.toFile());
                Files.createLink(toFile, fromFile);
            } catch (Exception ex) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString(), true)) {
                    return false;
                }
            }
        }

        return true;
    }
}
