package de.p2tools.p2backup.controller.data.pathdata;

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class PathDataFactory {
    private PathDataFactory() {
    }

    public static boolean addPath(BackupInfo backupInfo) {
        String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, ProgConfig.SYSTEM_FROM_PATH.get());
        if (path.trim().isEmpty()) {
            return false;
        }

        final String from = FileFactory.cleanFileData(path.trim(), "");
        if (backupInfo.getPathListFrom().isEmpty()) {
            backupInfo.getPathListFrom().add(new PathData(from));
            ProgConfig.SYSTEM_FROM_PATH.set(from);
            return true;
        }

        List<PathData> remove = new ArrayList<>();
        BooleanProperty add = new SimpleBooleanProperty(false);
        backupInfo.getPathListFrom().forEach(p -> {
            if (from.startsWith(p.getPath())) {
                P2Alert.showErrorAlert("Ordner hinzufügen", "Der bereits enthaltene Ordner:" +
                        "\n\n" +
                        p.getPath() +
                        "\n\n" +
                        "enthält die Dateien bereits");
            } else if (p.getPath().startsWith(from)) {
                P2Alert.showErrorAlert("Ordner hinzufügen", "Der neue Ordner deckt auch die Dateien " +
                        "des bereits enthaltenen Ordners:" +
                        "\n\n" +
                        p.getPath() +
                        "\n\n" +
                        " ab. Dieser bereits enthaltene Ordner wird entfernt");
                remove.add(p);
                add.set(true);
            } else {
                add.set(true);
            }
        });
        if (!remove.isEmpty()) {
            remove.forEach(p -> backupInfo.getPathListFrom().remove(p));
        }
        if (add.getValue()) {
            backupInfo.getPathListFrom().add(new PathData(from));
            ProgConfig.SYSTEM_FROM_PATH.set(from);
            return true;
        }

        return false;
    }
}
