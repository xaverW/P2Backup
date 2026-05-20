package de.p2tools.p2backup.controller.worker;

import de.p2tools.p2backup.controller.runner.FileRunner;
import de.p2tools.p2lib.tools.duration.P2Duration;
import de.p2tools.p2lib.tools.log.P2Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RunnerTest {
    private RunnerTest() {
    }


    public static void runTest() {
        File from = new File("/mnt/lager/p2BackupTest_1/");
        List<File> fileList = new ArrayList<>();

        //Verzeichnis ablaufen und Dateien suchen
        try {
            new FileRunner() {
                @Override
                public void workFile(File file) {
                    fileList.add(file);
                }
            }.recDir(from, true);
        } catch (Exception ex) {
            P2Log.errorLog(975102364, ex, "CreateHash.run - " + from.getPath());
        }


        try {
            FileUtils.deleteDirectory(Path.of("/mnt/lager/p2BackupTest_2/").toFile());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        // copy
        P2Duration.counterStart("copy");
        fileList.forEach(f -> {
            String path = f.getPath();
            path = path.replace("/mnt/lager/p2BackupTest_1/", "/mnt/lager/p2BackupTest_2/");
            File to = Path.of(path).toFile();

            try {
                FileUtils.copyFile(f, to, true);
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
        });
        P2Duration.counterStop("copy");


        try {
            FileUtils.deleteDirectory(Path.of("/mnt/lager/p2BackupTest_2/").toFile());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        // move
        P2Duration.counterStart("move");
        fileList.forEach(f -> {
            String path = f.getPath();
            path = path.replace("/mnt/lager/p2BackupTest_1/", "/mnt/lager/p2BackupTest_2/");
            File to = Path.of(path).toFile();
            try {
                FileUtils.moveFileToDirectory(f, to.getParentFile(), true);
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
        });
        P2Duration.counterStop("move");

        System.out.println("Anzahl: " + fileList.size());
    }
}
