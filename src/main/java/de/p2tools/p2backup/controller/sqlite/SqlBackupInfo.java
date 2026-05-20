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


            BackupInfo backupInfos = new BackupInfo();
            while (rs.next()) {
                backupInfos.setId(rs.getLong("id"));
                backupInfos.setName(rs.getString("name"));
                backupInfos.setDescription(rs.getString("description"));
                backupInfos.setBackupPath(rs.getString("backupPath"));
                backupInfos.setLastBackupId(rs.getLong("lastBackupId"));
                backupInfos.setLastStartDate(SqlFactory.getLocalDateTime(rs.getString("lastStartDate")));

                backupInfos.setHow(rs.getInt("how"));
                backupInfos.setFileFilterNot(rs.getBoolean("fileFilterNot"));

                backupInfos.setSumDay(rs.getInt("sumDay"));
                backupInfos.setSumWeek(rs.getInt("sumWeek"));
                backupInfos.setSumMonth(rs.getInt("sumMonth"));
                backupInfos.setGenDate(P2LDateFactory.fromString(rs.getString("genDate")));
            }

            if (!backupInfos.getBackupPath().equals(backupPath)) {
                // dann hat er sich geändert
                backupInfos.setBackupPath(backupPath);
            }

            // die Backups laden
            if (!SqlBackupData.readBackupDataList(backupInfos)) {
                conn.rollback();
            }


            // Path laden
            if (!readPathDataList(backupInfos)) {
                conn.rollback();
            }

            return backupInfos;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static boolean readPathDataList(BackupInfo backupInfos) {
        String url = SqlFactory.getUrl(backupInfos);
        if (url.isEmpty()) {
            return false;
        }

        String sql = "SELECT id, backupInfoId, path FROM pathData " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfos, url, sql, backupInfos.getPathListFrom())) {
            return false;
        }

        sql = "SELECT id, backupInfoId, path FROM pathDataExcludeDir " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfos, url, sql, backupInfos.getPathListExcludeDir())) {
            return false;
        }

        sql = "SELECT id, backupInfoId, path FROM pathDataExcludeFile " +
                "WHERE backupInfoId == ?";
        if (!readPath(backupInfos, url, sql, backupInfos.getPathListExcludeFile())) {
            return false;
        }

        return true;
    }

    private static boolean readPath(BackupInfo backupInfos,
                                    String url, String sql, PathDataList pathDataList) {

        try (var conn = DriverManager.getConnection(url);
             var pstmt = conn.prepareStatement(sql)) {

            long backupInfoId = backupInfos.getId();
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

    public static boolean delBackupInfo(BackupInfo backupInfos) {
        // BackupInfos
        P2Duration.counterStart("delBackupInfo");

        String url = SqlFactory.getUrl(backupInfos);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            long id = backupInfos.getId();

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
            for (BackupData backupData : backupInfos.getBackupDataList()) {
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

    public static boolean addUpdateBackupInfo(BackupInfo backupInfos) {
        // BackupInfos, BackupData updaten
        P2Duration.counterStart("updateBackupInfo");

        String url = SqlFactory.getUrl(backupInfos);
        if (url.isEmpty()) {
            return false;
        }
        try (var conn = DriverManager.getConnection(url)) {
            // Disable auto-commit mode
            conn.setAutoCommit(false);

            long id = backupInfos.getId();


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
            if (!writeBackupInfo(backupInfos, conn)) {
                conn.rollback();
            }

            for (BackupData backupData : backupInfos.getBackupDataList()) {
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

    private static boolean writeBackupInfo(BackupInfo backupInfos, Connection conn) throws SQLException {
        // BackupInfos und aktuelles Backup anlegen/updaten
        P2Duration.counterStart("addBackupInfo");

        // BackupInfo
        final String sqlBackupInfo = "INSERT OR REPLACE INTO backupInfo(id, name, description, backupPath," +
                "lastBackupId, lastStartDate, " +
                "how, fileFilterNot, " +
                "sumDay, sumWeek, sumMonth, " +
                "genDate) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlBackupInfo)) {
            pstmt.setLong(1, backupInfos.getId());
            pstmt.setString(2, backupInfos.getName());
            pstmt.setString(3, backupInfos.getDescription());
            pstmt.setString(4, backupInfos.getBackupPath());
            pstmt.setLong(5, backupInfos.getLastBackupId());
            pstmt.setString(6, SqlFactory.fromLocalDate(backupInfos.getLastStartDate()));

            pstmt.setInt(7, backupInfos.getHow());
            pstmt.setBoolean(8, backupInfos.isFileFilterNot());

            pstmt.setInt(9, backupInfos.getSumDay());
            pstmt.setInt(10, backupInfos.getSumWeek());
            pstmt.setInt(11, backupInfos.getSumMonth());
            pstmt.setString(12, backupInfos.getGenDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            conn.rollback();
            return false;
        }

        // PathDataList
        final String sqlPath = "INSERT OR REPLACE INTO pathData(id, backupInfoId, path) VALUES(?,?,?)";
        try (var pstmt = conn.prepareStatement(sqlPath)) {
            for (PathData path : backupInfos.getPathListFrom()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfos.getId());
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
            for (PathData path : backupInfos.getPathListExcludeDir()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfos.getId());
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
            for (PathData path : backupInfos.getPathListExcludeFile()) {
                pstmt.setLong(1, path.getId());
                pstmt.setLong(2, backupInfos.getId());
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
