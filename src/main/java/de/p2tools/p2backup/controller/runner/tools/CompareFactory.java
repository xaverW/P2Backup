package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.FileHashFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CompareFactory {

    private CompareFactory(BackupInfo backupInfo) {
    }

    public static void compare(Stage stage,
                               FileDataList fileListData, FileDataList fileListBackup,
                               FileDataList resultList, boolean compare) {
        // beim Backup prüfen ist fileListData die aus der DB, fileListBackup die aus dem Backup-Ordner
        final HashMap<String, FileData> dataMap = new HashMap<>();
        fileListData.forEach(fileData -> {
            fileData.resetError();
            fileData.setExistInData(true);
            dataMap.put(fileData.getFilePathStr(), fileData);
            resultList.add(fileData);
            if (fileData.getHash().equals(FileFactory.HASH_ERROR)) {
                fileData.setErrorHash(true);
            }
        });

        fileListBackup.forEach(fileBackup -> {
            fileBackup.resetError();
            fileBackup.setExistInBackup(true);
        });

        // vergleichen
        fileListBackup.forEach(fileBackup -> {
            String path = fileBackup.getFilePathStr();
            FileData fileDb = dataMap.remove(path);

            if (fileDb == null) {
                // dann nur im Backup
                resultList.add(fileBackup);
                if (fileBackup.getHash().equals(FileFactory.HASH_ERROR)) {
                    fileBackup.setErrorHash(true);
                }

            } else {
                // dann in beiden
                fileDb.setExistInBackup(true);
                if (fileBackup.getHash().equals(FileFactory.HASH_ERROR)) {
                    fileDb.setErrorHash(true);

                } else if (!fileBackup.getHash().equals(fileDb.getHash())) {
                    fileDb.setErrorDiff(true);
                }
            }
        });

//        List<FileData> list = new ArrayList<>(dataMap.values());
//        // nur in den Daten
//        for (FileData fileDb : list) {
//            fileDb.setExistInData(true);
//        }

        resultList.sort(Comparator.comparing(FileDataProps::getFilePathStr));

        boolean found = false;
        System.out.println("====NOT====");
        for (FileData f : resultList) {
            if (f.isErrorDiff() || f.isOnlyInData() || f.isOnlyInBackup() || f.isErrorHash()) {
                found = true;
            }
        }

        if (compare) {
            if (!found) {
                // dann nur eine kurze Meldung
                P2AlertAppThread.infoAlert(stage,
                        "Vergleich", "Dateien und Backup sind identisch",
                        "Die Dateien im Backup sind identisch\n" +
                                "mit den Original-Dateien");
            }
        } else {
            if (resultList.isEmpty()) {
                P2AlertAppThread.infoAlert(stage,
                        "Prüfen", "Backup ist OK",
                        "Im Backup befinden sich keine Dateien.");

            } else {
                if (!found) {
                    // dann nur eine kurze Meldung
                    P2AlertAppThread.infoAlert(stage,
                            "Prüfen", "Backup ist OK",
                            "Das Backup ist unverändert. Es fehlt nichts " +
                                    "oder ist verändert.");
                }
            }
        }
    }

    public static void compareQuick(BackupInfo backupInfo,
                                    FileDataList fileListDataDb, FileDataList fileListBackup,
                                    FileDataList errorList) {
        final HashMap<String, FileData> baFileMap = new HashMap<>();
        for (FileData baFile : fileListBackup) {
            baFile.resetError();
            baFile.setExistInBackup(true);
            baFileMap.put(baFile.getFilePathStr(), baFile);
        }

        for (FileData dbFile : fileListDataDb) {
            String path = dbFile.getFilePathStr();
            dbFile.resetError();
            dbFile.setExistInData(true);

            FileData baFile = baFileMap.remove(path);
            if (baFile == null) {
                errorList.add(dbFile);
                continue;
            }

            dbFile.setExistInBackup(true);
            if (dbFile.getSize() != baFile.getSize()) {
                dbFile.setErrorDiff(true);
                errorList.add(dbFile);
                continue;
            }

            if (dbFile.getDate() != baFile.getDate()) {
                // wenn sich nur das Datum unterscheidet, können die Dateien doch gleich sein
                FileData baHash = FileHashFactory.getFileDataHash(backupInfo, "", false,
                        baFile.getBackupFilePath().toFile(), true);

                if (baHash == null || !dbFile.getHash().equals(baHash.getHash())) {
                    dbFile.setErrorDiff(true);
                    if (baHash != null && baHash.getHash().equals(FileFactory.HASH_ERROR)) {
                        dbFile.setErrorHash(true);
                    }
                    errorList.add(dbFile);
                }
            }

        }
        // und jetzt noch die baFiles die zuviel sind
        List<FileData> list = new ArrayList<>(baFileMap.values());
        errorList.addAll(list);
    }
}
