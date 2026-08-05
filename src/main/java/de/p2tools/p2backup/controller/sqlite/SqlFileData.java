package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.HistoryFileData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class SqlFileData {

    private SqlFileData() {

    }

    public static boolean readDataFileList(BackupInfo backupInfo, FileDataList fileDataList) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        final String sqlBackupInfo = "SELECT id, backupId, dataFile, " +
                "date, size, link, hash, error FROM " +
                "dataFiles";

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sqlBackupInfo)) {

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

                fileDataList.add(fileData);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean readBackupFileList(BackupInfo backupInfo, BackupData backupData,
                                             FileDataList fileDataList) {
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
                fileDataList.add(fileData);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean readFileHistoryList(BackupInfo backupInfo, String filePath,
                                              List<HistoryFileData> fileDataList) {

        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }

        final String sqlBackupInfo = "SELECT id, backupId, dataFile, " +
                "date, size, link, hash, error FROM backupFiles " +
                "WHERE dataFile == ? ";

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sqlBackupInfo)) {

            pstmt.setString(1, filePath);
            var rs = pstmt.executeQuery();
            while (rs.next()) {
                HistoryFileData fileData = new HistoryFileData();
                fileData.setId(rs.getLong("id"));
                fileData.setBackupId(rs.getLong("backupId"));
                fileData.setFilePathStr(rs.getString("dataFile"));
                fileData.setDate(rs.getLong("date"));
                fileData.setSize(rs.getLong("size"));
                fileData.setLink(rs.getBoolean("link"));
                fileData.setHash(rs.getString("hash"));
                fileData.setError(rs.getBoolean("error"));

                BackupData backupData = SqlBackupData.getBackupData(backupInfo, fileData.getBackupId());
                if (backupData != null) {
                    fileData.setToPathStr(backupData.getToPathStr(backupInfo));
                    fileData.setStartDate(backupData.getStartDate());
                    fileDataList.add(fileData);
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }


    public static boolean writeDataFileList(BackupInfo backupInfo) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            // zuerst mal alle von BackupInfo löschen (wenn vorhanden)
            for (BackupData backupData : backupInfo.getBackupDataList()) {
                final String sql = "DELETE FROM dataFiles WHERE backupId=?";
                try (var pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, backupData.getId());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                    conn.rollback();
                    return false;
                }
            }

            // dann schreiben
            if (!write(backupInfo.runnerDto.getBackupData().getId(), backupInfo.runnerDto.getDataFileList(), conn, false)) {
                conn.rollback();
                return false;
            }
            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean writeBackupFileList(BackupInfo backupInfo) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);
            if (!writeBackupFileList(backupInfo.runnerDto.getBackupData().getId(),
                    backupInfo.runnerDto.getDataFileList(), conn)) {
                conn.rollback();
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean writeBackupFileList(long backupId,
                                              FileDataList fileDataList, Connection conn) throws SQLException {
        // zuerst mal löschen (wenn vorhanden)
        final String sql = "DELETE FROM backupFiles WHERE backupId=?";
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, backupId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // dann schreiben
        if (!write(backupId, fileDataList, conn, true)) {
            conn.rollback();
            return false;
        }

        return true;
    }

    private static boolean write(long backupId, FileDataList fileDataList, Connection conn, boolean backup) {
        final String sql;
        sql = "INSERT INTO " + (backup ? "backupFiles" : "dataFiles") +
                "(id, backupId, " +
                "dataFile, " +
                "date, size, link, hash, error) VALUES(?,?,?,?,?,?,?,?)";

        try (var pstmt = conn.prepareStatement(sql)) {
            for (FileData fileData : fileDataList) {
                pstmt.setLong(1, fileData.getId());
                pstmt.setLong(2, backupId);
                pstmt.setString(3, fileData.getFilePathStr());
                pstmt.setLong(4, fileData.getDate());
                pstmt.setLong(5, fileData.getSize());
                pstmt.setBoolean(6, fileData.isLink());
                pstmt.setString(7, fileData.getHash());
                pstmt.setBoolean(8, fileData.isError());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean updateBackupFileList(BackupInfo backupInfo, long backupId, FileDataList fileDataList) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            if (!SqlFileData.deleteBackupFileList(backupInfo, backupId)) {
                conn.rollback();
                return false;
            }
            if (!write(backupId, fileDataList, conn, true)) {
                conn.rollback();
                return false;
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    private static boolean updateFileList(long backupId, FileDataList fileDataList, Connection conn, boolean backup) {
        final String sql;
        sql = "UPDATE " + (backup ? "backupFiles" : "dataFiles") +
                " SET backupId = ?, " +
                "dataFile = ?," +
                " date = ?, size = ?, link = ?, hash = ?, error = ? WHERE id = ?";

        try (var pstmt = conn.prepareStatement(sql)) {
            for (FileData fileData : fileDataList) {
                pstmt.setLong(1, backupId);
                pstmt.setString(2, fileData.getFilePathStr());
                pstmt.setLong(3, fileData.getDate());
                pstmt.setLong(4, fileData.getSize());
                pstmt.setBoolean(5, fileData.isLink());
                pstmt.setString(6, fileData.getHash());
                pstmt.setBoolean(7, fileData.isError());
                pstmt.setLong(8, fileData.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean deleteBackupFileList(BackupInfo backupInfo, List<FileData> list) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            String sql = "DELETE FROM backupFiles WHERE id=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                for (FileData fileData : list) {
                    pstmt.setLong(1, fileData.getId());
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean deleteBackupFileList(BackupInfo backupInfo, long backupId) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            String sql = "DELETE FROM backupFiles WHERE backupId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, backupId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean deleteFileList(BackupInfo backupInfo, BackupData backupData) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            String sql = "DELETE FROM dataFiles WHERE backupId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, backupData.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            sql = "DELETE FROM backupFiles WHERE backupId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, backupData.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
}