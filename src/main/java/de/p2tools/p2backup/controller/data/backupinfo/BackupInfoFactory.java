package de.p2tools.p2backup.controller.data.backupinfo;

import de.p2tools.p2backup.controller.ProgQuit;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2lib.tools.P2Wait;
import javafx.application.Platform;
import javafx.beans.property.*;

public class BackupInfoFactory {
    private BackupInfoFactory() {
    }

    public static boolean isRunning() {
        BooleanProperty running = new SimpleBooleanProperty(false);
        ProgData.getInstance().backupInfoList.forEach(bi -> {
            if (bi.runnerDto.isRunning()) {
                running.set(true);
            }
        });
        return running.get();
    }

    public static void stopAllAndQuitt(DoubleProperty property) {
        ProgData.getInstance().backupInfoList.forEach(bi -> {
            bi.runnerDto.setStop();
        });
        property.set(0);

        new Thread(() -> {

            IntegerProperty count = new SimpleIntegerProperty(0);
            final int MAX = 50;

            while (BackupInfoFactory.isRunning()) {
                count.set(count.get() + 1);
                if (count.get() > MAX) {
                    // -> 5s max Wartezeit
                    break;
                }

                Platform.runLater(() -> {
                    property.set(count.get() / (double) MAX);
                });
                P2Wait.pause(500);
            }
            Platform.runLater(ProgQuit::quitNow);

        }).start();
    }
}
