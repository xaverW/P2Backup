package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.tools.duration.P2Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlBackupData {
    private SqlBackupData() {
    }

    public static boolean readBackupDataList(BackupInfo backupInfo) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }

        final String sqlBackupInfo = "SELECT id, backupInfoId, count, startDate, subPath FROM backupData " +
                "WHERE backupInfoId == ?";
        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sqlBackupInfo)) {

            long backupInfoId = backupInfo.getId();
            pstmt.setLong(1, backupInfoId);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                BackupData backupData = new BackupData();
                backupData.setId(rs.getLong("id"));
                backupData.setBackupInfoId(backupInfoId);
                backupData.setCount(rs.getInt("count"));
                backupData.setStartDate(SqlFactory.getLocalDateTime(rs.getString("startDate")));
                backupData.setSubPath(rs.getString("subPath"));
                backupInfo.getBackupDataList().add(backupData);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean writeBackupData(BackupInfo backupInfo, BackupData backupData) {
        // BackupDate löschen
        P2Duration.counterStart("writeBackupData");

        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            if (!writeBackupData(backupData, conn)) {
                conn.rollback();
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        P2Duration.counterStop("writeBackupData");
        return true;
    }

    public static boolean writeBackupData(BackupData backupData, Connection conn) throws SQLException {
        // backupInfo und aktuelles Backup anlegen/updaten
        // BackupData
        final String sqlBackup = "INSERT OR REPLACE INTO backupData(id, backupInfoId, count, startDate, " +
                "subPath) VALUES(?,?,?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlBackup)) {
            pstmt.setLong(1, backupData.getId());
            pstmt.setLong(2, backupData.getBackupInfoId());
            pstmt.setInt(3, backupData.getCount());
            pstmt.setString(4, backupData.getStartDate().toString());

            pstmt.setString(4, SqlFactory.fromLocalDate(backupData.getStartDate()));
            pstmt.setString(5, backupData.getSubPath());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        return true;
    }

    public static boolean delBackupData(BackupInfo backupInfo, BackupData backupData) {
        // BackupDate löschen
        P2Duration.counterStart("delBackupData");

        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            if (!delBackupData(backupData, conn)) {
                conn.rollback();
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        P2Duration.counterStop("delBackupData");
        return true;
    }

    public static boolean delBackupData(BackupData backupData, Connection conn) throws SQLException {
        // BackupDate löschen
        P2Duration.counterStart("delBackupData");

        final long id = backupData.getId();
        // BackupData
        String sql = "DELETE FROM backupData WHERE id=?";

        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // FileData
        sql = "DELETE FROM backupFiles WHERE backupId=?";

        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // FileData
        sql = "DELETE FROM dataFiles WHERE backupId=?";

        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }


        P2Duration.counterStop("delBackupData");
        return true;
    }
}
