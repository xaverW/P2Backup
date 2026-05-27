package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import javafx.beans.property.BooleanProperty;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;

public class CompareFactory {
    private CompareFactory() {
    }

    public static void compare(Stage stage,
                               FileDataList fileListData, FileDataList fileListBackup,
                               FileDataList resultList, boolean compare) {
        final HashMap<String, FileData> dataMap = new HashMap<>();

        fileListData.forEach(data -> {
            data.setDiff(false);
            data.setExistData(false);
            data.setExistBackup(false);
        });
        fileListBackup.forEach(backup -> {
            backup.setDiff(false);
            backup.setExistData(false);
            backup.setExistBackup(false);
        });

        fileListData.forEach(file -> {
            file.setExistData(true);
            resultList.add(file);
            dataMap.put(file.getFilePathStr(), file);
        });

        fileListBackup.forEach(fileBackup -> {
            String path = fileBackup.getFilePathStr();
            FileData data = dataMap.get(path);
            if (data != null) {
                // dann in beiden
                data.setExistBackup(true);
                if (!fileBackup.getHash().equals(data.getHash())) {
                    data.setDiff(true);
                }

            } else {
                // dann nur im Backup
                fileBackup.setExistBackup(true);
                resultList.add(fileBackup);
            }
        });

        resultList.sort(Comparator.comparing(FileDataProps::getFilePathStr));

        boolean found = false;
        System.out.println("====NOT====");
        for (FileData f : resultList) {
            if (f.isDiff() || !f.isExistBackup() || !f.isExistData() || f.isError()) {
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

    public static void compareQuick(FileDataList fileListData, FileDataList fileListBackup,
                                    BooleanProperty foundError) {
        foundError.set(false);

        if (fileListData.size() != fileListBackup.size()) {
            // dann stimmt schon was nicht
            foundError.set(true);
            return;
        }

        final HashMap<String, FileData> dataMap = new HashMap<>();
        fileListData.forEach(file -> {
            dataMap.put(file.getFilePathStr(), file);
        });

        for (FileData f : fileListBackup) {
            String path = f.getFilePathStr();
            FileData data = dataMap.get(path);
            if (data == null) {
                foundError.set(true);
                break;
            }
        }
    }
}
