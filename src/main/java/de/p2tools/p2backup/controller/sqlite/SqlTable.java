package de.p2tools.p2backup.controller.sqlite;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.tools.duration.P2Duration;
import de.p2tools.p2lib.tools.log.P2Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlTable {
    private SqlTable() {
    }

    public static boolean makeBackupDb(BackupInfo backupInfos) {
        P2Duration.counterStart("makeBackupDb");
        String url = SqlFactory.getUrl(backupInfos);
        if (url.isEmpty()) {
            return false;
        }

        // zuerst die DB anlegen
        try (var conn = DriverManager.getConnection(url)) {
            P2Log.sysLog("Datenbank erstellt: " + url);

            // Disable auto-commit mode
            conn.setAutoCommit(false);

            if (!makeBackupDb(url, conn)) {
                conn.rollback();
            }


            // ========================
            // commit work
            conn.commit();
        } catch (SQLException e) {
            P2Log.errorLog(956521479, "Konnte die DB nicht anlegen: " + url);
            return false;
        }

        P2Duration.counterStop("makeBackupDb");
        return true;
    }

    public static boolean makeBackupDb(String url, Connection conn) throws SQLException {

        // Tabellen anlegen: backupInfo
        var sql = "CREATE TABLE IF NOT EXISTS backupInfo ("
                + "	id LONG PRIMARY KEY,"
                + " name STRING NOT NULL,"
                + " description STRING NOT NULL,"
                + " backupPath STRING NOT NULL,"
                + "	lastBackupId LONG NOT NULL,"
                + " lastStartDate STRING NOT NULL,"

                + " how INTEGER NOT NULL,"
                + " fileFilterNot BOOLEAN NOT NULL,"

                + " sumDay INTEGER NOT NULL,"
                + " sumWeek INTEGER NOT NULL,"
                + " sumMonth INTEGER NOT NULL,"

                + " genDate STRING NOT NULL"
                + ");";

        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(825632587, "Konnte die Tabelle (backupInfo) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: pathDate
        sql = "CREATE TABLE IF NOT EXISTS pathData ("
                + "	id LONG PRIMARY KEY,"
                + " backupInfoId LONG NOT NULL,"
                + " path STRING NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(623547815, "Konnte die Tabelle (pathData) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: pathDataExcludeDir
        sql = "CREATE TABLE IF NOT EXISTS pathDataExcludeDir ("
                + "	id LONG PRIMARY KEY,"
                + " backupInfoId LONG NOT NULL,"
                + " path STRING NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(645239010, "Konnte die Tabelle (pathDataExcludeDir) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: pathDataExcludeFile
        sql = "CREATE TABLE IF NOT EXISTS pathDataExcludeFile ("
                + "	id LONG PRIMARY KEY,"
                + " backupInfoId LONG NOT NULL,"
                + " path STRING NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(732560412, "Konnte die Tabelle (pathDataExcludeFile) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: backupDate
        sql = "CREATE TABLE IF NOT EXISTS backupData ("
                + "	id LONG PRIMARY KEY,"
                + "	backupInfoId NOT NULL,"
                + "	count INTEGER NOT NULL,"
                + "	startDate STRING NOT NULL,"
                + "	subPath STRING NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(845120375, "Konnte die Tabelle (backupData) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: dataFileData, vom Backup
        sql = "CREATE TABLE IF NOT EXISTS backupFiles ("
                + "	id LONG PRIMARY KEY,"
                + " backupId LONG NOT NULL,"
                + " dataFile STRING NOT NULL,"
                + " date LONG NOT NULL,"
                + " size LONG NOT NULL,"
                + " link BOOLEAN NOT NULL,"
                + " hash STRING NOT NULL,"
                + " error BOOLEAN NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(963256201, "Konnte die Tabelle (fileData) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }

        // Tabellen anlegen: dataFileData: sind die aktuellen DATEN
        sql = "CREATE TABLE IF NOT EXISTS dataFiles ("
                + "	id LONG PRIMARY KEY,"
                + " backupId LONG NOT NULL,"
                + " dataFile STRING NOT NULL,"
                + " date LONG NOT NULL,"
                + " size LONG NOT NULL,"
                + " link BOOLEAN NOT NULL,"
                + " hash STRING NOT NULL,"
                + " error BOOLEAN NOT NULL"
                + ");";
        try (var stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            P2Log.errorLog(745457854, "Konnte die Tabelle (fileData) nicht anlegen: " + url);
            conn.rollback();
            return false;
        }


        P2Duration.counterStop("makeBackupDb");
        return true;
    }
}
