package processor;

import utils.JdbcUtils;
import utils.TransLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2016/3/21.
 */
public class BackupPorcessor {
  private static Logger logger = TransLogger.getLogger(BackupPorcessor.class);
  public static List<String> TO_BACKUP_TABLE_NAMES = new ArrayList<String>();

  public enum SYSTEM_TYPE {
    EC, PC
  }


  public static boolean doBackup() {
    if (JdbcUtils.EC_HEAD_CODES == null || JdbcUtils.EC_HEAD_CODES.length() <= 0)
      return true;

    if (JdbcUtils.PC_HEAD_CODES == null || JdbcUtils.PC_HEAD_CODES.length() <= 0)
      return true;

    loadToBackupTableNames(SYSTEM_TYPE.EC);
    loadToBackupTableNames(SYSTEM_TYPE.PC);

    TO_BACKUP_TABLE_NAMES.add("VOUCHERS");
    backupTables();

    return true;
  }


  private static boolean loadToBackupTableNames(SYSTEM_TYPE type) {
    Connection conn = null;
    Statement statement = null;
    ResultSet result = null;
    String sql = "";
    try {
      conn = JdbcUtils.getOracleConnection();
      if (conn == null)
        return false;

      String headerCodes = "";
      if (type == SYSTEM_TYPE.EC)
        headerCodes = JdbcUtils.EC_HEAD_CODES;
      else if (type == SYSTEM_TYPE.PC)
        headerCodes = JdbcUtils.PC_HEAD_CODES;

      sql = "SELECT system_code||'_'||voucher_year||LPAD (voucher_month , 2 , '0') ym " +
        " FROM VOUCHERS where HEADQUARTER_ORGCODE in (" + headerCodes
        + ") group by voucher_year, voucher_month,system_code ";
      statement = conn.createStatement();
      statement.setQueryTimeout(43200);
      result = statement.executeQuery(sql);

      while (result.next()) {
        String tableName = "feelog_" + result.getString(1);
        TO_BACKUP_TABLE_NAMES.add(tableName.trim());
        tableName = "TRANSACTIONS_" + result.getString(1);
        TO_BACKUP_TABLE_NAMES.add(tableName.trim());
      }
      return true;
    } catch (SQLException e) {
      logger.warning(sql);
      e.printStackTrace();
    } catch (Exception e) {
      logger.warning(sql);
      e.printStackTrace();
    } finally {
      try {
        JdbcUtils.releaseStatement(statement, result);
        JdbcUtils.releaseConn(conn);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }


  private static boolean backupTables() {
    if (TO_BACKUP_TABLE_NAMES.size() <= 0)
      return true;

    Connection conn = null;
    Statement statement = null;
    ResultSet result = null;
    String sql = "";

    for (String tableName : TO_BACKUP_TABLE_NAMES) {
      try {
        conn = JdbcUtils.getOracleConnection();
        if (conn == null)
          return false;

        sql = "create table adjust_" + tableName + " as select * from " + tableName;
        statement = conn.createStatement();
        statement.setQueryTimeout(43200);
        result = statement.executeQuery(sql);
      } catch (SQLException e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
      } catch (Exception e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
      } finally {
        try {
          JdbcUtils.releaseStatement(statement, result);
          JdbcUtils.releaseConn(conn);
        } catch (Exception e) {
          logger.severe(e.getMessage());
        }
      }
    }
    return true;
  }
}
