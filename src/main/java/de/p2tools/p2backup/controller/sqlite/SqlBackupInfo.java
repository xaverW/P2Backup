package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.data.pathdata.PathDataList;
import de.p2tools.p2lib.tools.date.P2LDateFactory;
import de.p2tools.p2lib.tools.duration.P2Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlBackupInfo {
    private SqlBackupInfo() {
    }

    public static BackupInfo readBackupInfo(/*long id,*/ String backupPath) {
        String url = SqlFactory.getUrl(backupPath);
        if (url.isEmpty()) {
            return null;
        }

        final String sqlBackupInfo = "SELECT id, name, description, backupPath, " +
                "lastBackupId, lastStartDate, " +
                "how, fileFilterNot, " +
                "sumDay, sumWeek, sumMonth, " +
                "genDate " +
//                "FROM backupInfo WHERE id == ?";
                "FROM backupInfo"; // kann ja nur eine in der DB geben

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sqlBackupInfo)) {

//            pstmt.setLong(1, id);
            var rs = pstmt.executeQuery();


            BackupInfo backupInfo = new BackupInfo();
            while (rs.next()) {
                backupInfo.setId(rs.getLong("id"));
                backupInfo.setName(rs.getString("name"));
                backupInfo.setDescription(rs.getString("description"));
                backupInfo.setBackupPath(rs.getString("backupPath"));
                backupInfo.setLastBackupId(rs.getLong("lastBackupId"));
                backupInfo.setLastStartDate(SqlFactory.getLocalDateTime(rs.getString("lastStartDate")));

                backupInfo.setHow(rs.getInt("how"));
                backupInfo.setFileFilterNot(rs.getBoolean("fileFilterNot"));

                backupInfo.setSumDay(rs.getInt("sumDay"));
                backupInfo.setSumWeek(rs.getInt("sumWeek"));
                backupInfo.setSumMonth(rs.getInt("sumMonth"));
                backupInfo.setGenDate(P2LDateFactory.fromString(rs.getString("genDate")));
            }

            if (!backupInfo.getBackupPath().equals(backupPath)) {
                // dann hat er sich geändert
                backupInfo.setBackupPath(backupPath);
            }

            // die Backups laden
            if (!SqlBackupData.readBackupDataList(backupInfo)) {
                conn.rollback();
            }


            // Path laden
            if (!readPathDataList(backupInfo)) {
                conn.rollback();
            }

            return backupInfo;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static boolean readPathDataList(BackupInfo backupInfo) {
        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }

        String sql = "SELECT id, backupInfoId, path FROM pathData " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfo, url, sql, backupInfo.getPathListFrom())) {
            return false;
        }

        sql = "SELECT id, backupInfoId, path FROM pathDataExcludeDir " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfo, url, sql, backupInfo.getPathListExcludeDir())) {
            return false;
        }

        sql = "SELECT id, backupInfoId, path FROM pathDataExcludeFile " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfo, url, sql, backupInfo.getPathListExcludeFile())) {
            return false;
        }

        return true;
    }

    private static boolean readPath(BackupInfo backupInfo,
                                    String url, String sql, PathDataList pathDataList) {

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sql)) {

            long backupInfoId = backupInfo.getId();
            pstmt.setLong(1, backupInfoId);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                PathData pathData = new PathData();
                pathData.setId(rs.getLong("id"));
                pathData.setBackupInfoId(backupInfoId);
                pathData.setPath(rs.getString("path"));
                pathDataList.add(pathData);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean delBackupInfo(BackupInfo backupInfo) {
        // backupInfo
        P2Duration.counterStart("delBackupInfo");

        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            long id = backupInfo.getId();

            // BackupInfo
            String sql = "DELETE FROM backupInfo WHERE id=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // BackupData
            for (BackupData backupData : backupInfo.getBackupDataList()) {
                // BackupData
                if (!SqlBackupData.delBackupData(backupData, conn)) {
                    conn.rollback();
                }
            }

            // PathData
            sql = "DELETE FROM pathData WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ExcludeDir
            sql = "DELETE FROM pathDataExcludeDir WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ExcludeFile
            sql = "DELETE FROM pathDataExcludeFile WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
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

        P2Duration.counterStop("delBackupInfo");
        return true;
    }

    public static boolean addUpdateBackupInfo(BackupInfo backupInfo) {
        // backupInfo, BackupData updaten
        P2Duration.counterStart("updateBackupInfo");

        String url = SqlFactory.getUrl(backupInfo);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            long id = backupInfo.getId();


//            // zuerst DB erstellen
//            if (!SqlTable.makeBackupDb(url, conn)) {
//                conn.rollback();
//            }

            // dann alte PATH löschen
            // PathData
            String sql = "DELETE FROM pathData WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ExcludeDir
            sql = "DELETE FROM pathDataExcludeDir WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // ExcludeFile
            sql = "DELETE FROM pathDataExcludeFile WHERE backupInfoId=?";
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                conn.rollback();
                return false;
            }

            // die BACKUP_INFO und BACKUP_DATA schreiben
            if (!writeBackupInfo(backupInfo, conn)) {
                conn.rollback();
            }

            for (BackupData backupData : backupInfo.getBackupDataList()) {
                // BackupData
                if (!SqlBackupData.writeBackupData(backupData, conn)) {
                    conn.rollback();
                }
            }

            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        P2Duration.counterStop("updateBackupInfo");
        return true;
    }

    private static boolean writeBackupInfo(BackupInfo backupInfo, Connection conn) throws SQLException {
        // backupInfo und aktuelles Backup anlegen/updaten
        P2Duration.counterStart("addBackupInfo");

        // BackupInfo
        final String sqlBackupInfo = "INSERT OR REPLACE INTO backupInfo(id, name, description, backupPath," +
                "lastBackupId, lastStartDate, " +
                "how, fileFilterNot, " +
                "sumDay, sumWeek, sumMonth, " +
                "genDate) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlBackupInfo)) {
            pstmt.setLong(1, backupInfo.getId());
            pstmt.setString(2, backupInfo.getName());
            pstmt.setString(3, backupInfo.getDescription());
            pstmt.setString(4, backupInfo.getBackupPath());
            pstmt.setLong(5, backupInfo.getLastBackupId());
            pstmt.setString(6, SqlFactory.fromLocalDate(backupInfo.getLastStartDate()));

            pstmt.setInt(7, backupInfo.getHow());
            pstmt.setBoolean(8, backupInfo.isFileFilterNot());

            pstmt.setInt(9, backupInfo.getSumDay());
            pstmt.setInt(10, backupInfo.getSumWeek());
            pstmt.setInt(11, backupInfo.getSumMonth());
            pstmt.setString(12, backupInfo.getGenDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // PathDataList
        final String sqlPath = "INSERT OR REPLACE INTO pathData(id, backupInfoId, path) VALUES(?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlPath)) {
            for (PathData path : backupInfo.getPathListFrom()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfo.getId());
                pstmt.setString(3, path.getPath());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // PathDataList excludeDir
        final String sqlPathExcludeDir = "INSERT OR REPLACE INTO pathDataExcludeDir(id, backupInfoId, path) VALUES(?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlPathExcludeDir)) {
            for (PathData path : backupInfo.getPathListExcludeDir()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfo.getId());
                pstmt.setString(3, path.getPath());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // PathDataList excludeFile
        final String sqlPathExcludeFile = "INSERT OR REPLACE INTO pathDataExcludeFile(id, backupInfoId, path) VALUES(?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlPathExcludeFile)) {
            for (PathData path : backupInfo.getPathListExcludeFile()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfo.getId());
                pstmt.setString(3, path.getPath());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        P2Duration.counterStop("addBackupInfo");
        return true;
    }
}
