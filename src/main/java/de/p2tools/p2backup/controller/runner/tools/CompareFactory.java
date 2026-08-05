package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;

public class CompareFactory {
    private CompareFactory() {
    }

    public static void compare(Stage stage,
                               FileDataList fileListData, FileDataList fileListBackup,
                               FileDataList resultList, boolean compare) {
        // beim Backup prüfen ist fileListData die aus der DB, fileListBackup die aus dem Backup-Ordner
        final HashMap<String, FileData> dataMap = new HashMap<>();

        // init
        fileListData.forEach(data -> {
            data.setDiff(false);
            data.setExistInData(false);
            data.setExistInBackup(false);
            data.setError(false);
        });
        fileListBackup.forEach(backup -> {
            backup.setDiff(false);
            backup.setExistInData(false);
            backup.setExistInBackup(false);
            backup.setError(false);
        });

        // vergleichen
        fileListData.forEach(file -> {
            file.setExistInData(true);
            if (file.getHash().equals(FileFactory.HASH_ERROR)) {
                file.setError(true);
            }

            resultList.add(file);
            dataMap.put(file.getFilePathStr(), file);
        });

        fileListBackup.forEach(fileBackup -> {
            String path = fileBackup.getFilePathStr();
            FileData data = dataMap.get(path);
            if (data != null) {
                // dann in beiden
                data.setExistInBackup(true);
                if (fileBackup.getHash().equals(FileFactory.HASH_ERROR)) {
                    data.setError(true);
//                    data.setDiff(true);
                } else if (!fileBackup.getHash().equals(data.getHash())) {
                    data.setDiff(true);
                }

            } else {
                // dann nur im Backup
                fileBackup.setExistInBackup(true);
                resultList.add(fileBackup);
            }
        });

        resultList.sort(Comparator.comparing(FileDataProps::getFilePathStr));

        boolean found = false;
        System.out.println("====NOT====");
        for (FileData f : resultList) {
            if (f.isDiff() || !f.isExistInBackup() || !f.isExistInData() || f.isError()) {
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

    public static void compareQuick(FileDataList fileListDataDb, FileDataList fileListBackup,
                                    FileDataList errorList) {
        final HashMap<String, FileData> isDataMap = new HashMap<>();
        fileListBackup.forEach(file -> {
            isDataMap.put(file.getFilePathStr(), file);
        });

        for (FileData f : fileListDataDb) {
            String path = f.getFilePathStr();
            f.setDiff(false);
            f.setError(false);

            FileData dataBackup = isDataMap.get(path);
            if (dataBackup == null) {
                f.setError(true);
                errorList.add(f);
                continue;
            }

            if (f.getDate() != dataBackup.getDate()) {
                f.setDiff(true);
                errorList.add(f);
                continue;
            }
            if (f.getSize() != dataBackup.getSize()) {
                f.setDiff(true);
                errorList.add(f);
            }
        }
    }
}
