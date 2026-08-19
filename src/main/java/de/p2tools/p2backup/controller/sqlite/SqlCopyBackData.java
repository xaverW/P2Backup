package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackData;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackDataList;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlCopyBackData {
    private SqlCopyBackData() {
    }

    public static boolean getCopyBackData(BackupInfo backupInfo, BackupData backupData, CopyBackDataList copyBackDataList) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }

        final String to = backupData.getToPathStr(backupInfo);
        final String sqlBackupInfo = "SELECT id, backupId, dataFile, " +
                "date, size, link, hash, error FROM " +
                "backupFiles WHERE backupId == ?";

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sqlBackupInfo)) {

            pstmt.setLong(1, backupData.getId());
            var rs = pstmt.executeQuery();
            while (rs.next()) {
                FileData fileData = new FileData();
                fileData.setId(rs.getLong("id"));
                fileData.setBackupId(rs.getLong("backupId"));
                fileData.setFilePathStr(rs.getString("dataFile"));
                fileData.setDate(rs.getLong("date"));
                fileData.setSize(rs.getLong("size"));
                fileData.setLink(rs.getBoolean("link"));
                fileData.setHash(rs.getString("hash"));
                fileData.setError(rs.getBoolean("error"));
                fileData.setToPathStr(to);

                copyBackDataList.add(new CopyBackData(fileData, backupData));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
}
