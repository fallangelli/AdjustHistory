package processor;

import javafx.util.Pair;
import utils.JdbcUtils;
import utils.TransLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2016/3/21.
 */
public class AdjustPorcessor {
  private static final String INDEX_FORMAT = "0000000";
  public static List<String> TO_ADJUST_PC_FEELOG_TABLE_NAMES = new ArrayList<String>();
  public static Map<String, Pair<String, String>> TO_ADJUST_PC_TRANS_TABLES = new HashMap();
  public static List<String> TO_ADJUST_EC_FEELOG_TABLE_NAMES = new ArrayList<String>();
  public static Map<String, Pair<String, String>> TO_ADJUST_EC_TRANS_TABLES = new HashMap<>();
  private static Logger logger = TransLogger.getLogger(AdjustPorcessor.class);

  public static boolean doAdjust() {
    if (JdbcUtils.EC_HEAD_CODES == null || JdbcUtils.EC_HEAD_CODES.length() <= 0)
      return true;

    if (JdbcUtils.PC_HEAD_CODES == null || JdbcUtils.PC_HEAD_CODES.length() <= 0)
      return true;

    loadToAdjustTableNames(SYSTEM_TYPE.EC);
    loadToAdjustTableNames(SYSTEM_TYPE.PC);

    adjustFeelogTables(SYSTEM_TYPE.EC);
    adjustTransTables(SYSTEM_TYPE.EC);
    adjustFeelogTables(SYSTEM_TYPE.PC);
    adjustTransTables(SYSTEM_TYPE.PC);

    return true;
  }

  private static boolean adjustFeelogTables(SYSTEM_TYPE systemType) {
    List<String> toAdjustTableNames = new ArrayList<>();
    String headerCodes = "";
    if (systemType == SYSTEM_TYPE.EC) {
      toAdjustTableNames = TO_ADJUST_EC_FEELOG_TABLE_NAMES;
      headerCodes = JdbcUtils.EC_HEAD_CODES;
    } else if (systemType == SYSTEM_TYPE.PC) {
      toAdjustTableNames = TO_ADJUST_PC_FEELOG_TABLE_NAMES;
      headerCodes = JdbcUtils.PC_HEAD_CODES;
    }
    if (toAdjustTableNames.size() <= 0)
      return true;

    Connection conn = null;
    Statement statement = null;
    ResultSet result = null;
    String sql = "";

    try {
      conn = JdbcUtils.getOracleConnection();
      statement = conn.createStatement();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    for (String tableName : toAdjustTableNames) {
      try {
        sql = "update " + tableName + " set HEADQUARTER_ORGCODE = BRANCH_ORGCODE," +
          "HEADQUARTER_ORGNAME = BRANCH_ORGNAME, LEVEL1NAME=LEVEL2NAME " +
          "where HEADQUARTER_ORGCODE<>BRANCH_ORGCODE and HEADQUARTER_ORGCODE " +
          "in (" + headerCodes + " )";
        statement.setQueryTimeout(43200);
        result = statement.executeQuery(sql);
      } catch (SQLException e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
        continue;
      } catch (Exception e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
        continue;
      }
    }
    try {
      JdbcUtils.releaseStatement(statement, result);
      JdbcUtils.releaseConn(conn);
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return true;
  }

  private static boolean adjustTransTables(SYSTEM_TYPE systemType) {
    Object[] toAdjustTableNames = new Object[0];
    String headerCodes = "";
    if (systemType == SYSTEM_TYPE.EC) {
      toAdjustTableNames = TO_ADJUST_EC_TRANS_TABLES.keySet().toArray();
      headerCodes = JdbcUtils.EC_HEAD_CODES;
    } else if (systemType == SYSTEM_TYPE.PC) {
      toAdjustTableNames = TO_ADJUST_PC_TRANS_TABLES.keySet().toArray();
      headerCodes = JdbcUtils.PC_HEAD_CODES;
    }
    if (toAdjustTableNames.length <= 0)
      return true;

    Connection conn = null;
    Statement statSelect = null;
    Statement statVoucherNo = null;
    Statement statUpdateVoucher = null;
    Statement statUpdateTrans = null;
    Statement statValidVoucher = null;
    Statement statDeleteVoucher = null;
    ResultSet resultSelect = null;
    ResultSet resultVoucherNo = null;
    String sql = "";
    DecimalFormat df = new DecimalFormat(INDEX_FORMAT);
    try {
      conn = JdbcUtils.getOracleConnection();
      statSelect = conn.createStatement();
      statVoucherNo = conn.createStatement();
      statUpdateVoucher = conn.createStatement();
      statUpdateTrans = conn.createStatement();
      statValidVoucher = conn.createStatement();
      statDeleteVoucher = conn.createStatement();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Integer index = 0;
    for (Object tableName : toAdjustTableNames) {
      Pair<String, String> item = new Pair<>("", "");
      if (systemType == SYSTEM_TYPE.EC) {
        item = TO_ADJUST_EC_TRANS_TABLES.get(tableName);
      } else if (systemType == SYSTEM_TYPE.PC) {
        item = TO_ADJUST_PC_TRANS_TABLES.get(tableName);
      }

      String year = item.getKey();
      String month = item.getValue();

      try {
        sql = "SELECT headquarter_orgcode, BRANCH_ORGCODE,BRANCH_ORGNAME, COUNT (*) cou " +
          "    FROM " + tableName +
          "   WHERE     headquarter_orgcode IN (" + headerCodes + ") " +
          "         AND headquarter_orgcode <> BRANCH_ORGCODE " +
          "GROUP BY headquarter_orgcode, BRANCH_ORGCODE, BRANCH_ORGNAME";
        resultSelect = statSelect.executeQuery(sql);
        String baseHeadCode = "";
        while (resultSelect.next()) {
          String headerCode = baseHeadCode = resultSelect.getString("headquarter_orgcode");
          String branchCode = resultSelect.getString("BRANCH_ORGCODE");
          String branchName = resultSelect.getString("BRANCH_ORGNAME");
          Integer count = resultSelect.getInt("cou");
          sql = "select substr(voucher_no,1,12)||'9' VOUCHER_NO from vouchers " +
            " where HEADQUARTER_ORGCODE = '" + headerCode + "' " +
            " and voucher_year='" + year + "' " +
            " and voucher_month='" + month + "'";
          resultVoucherNo = statVoucherNo.executeQuery(sql);
          resultVoucherNo.next();

          String strIndex = df.format(++index);
          String voucherNo = resultVoucherNo.getString(1) + strIndex;

          sql = "insert into vouchers select '" + voucherNo +
            "', VOUCHER_TIME , '" + branchCode + "', '" + branchName + "' , SYSTEM_CODE ," +
            " PRODUCT_CODE  , PRODUCT_NAME  , PRODUCT_PRICE_ORIGINAL, PRODUCT_PRICE_DISCOUNTED," +
            count + "*PRODUCT_PRICE_ORIGINAL," +
            count + "*PRODUCT_PRICE_DISCOUNTED," +
            count + ", " + count + ", " +
            " DISCOUNT ,DISCOUNT_MEMO ,VOUCHER_YEAR,VOUCHER_MONTH,BILL_NO  ," +
            " COEF_CONTRIBUTE  ,COEF_QUERYAMOUNT ,1   ,STATUS from vouchers " +
            " where HEADQUARTER_ORGCODE = '" + headerCode + "' " +
            " and voucher_year='" + year + "' " +
            " and voucher_month='" + month + "'";
          statUpdateTrans.executeQuery(sql);

          sql = "update " + tableName + " set HEADQUARTER_ORGCODE = BRANCH_ORGCODE," +
            "HEADQUARTER_ORGNAME = BRANCH_ORGNAME, LEVEL1NAME=LEVEL2NAME," +
            "VOUCHER_NO= '" + voucherNo +
            "' where HEADQUARTER_ORGCODE = '" + headerCode + "' " +
            " and BRANCH_ORGCODE = '" + branchCode + "' ";
          statUpdateVoucher.executeQuery(sql);

          sql = "update  vouchers set VALID_FLAG = '0' " +
            " where HEADQUARTER_ORGCODE = '" + baseHeadCode + "' " +
            " and voucher_year='" + year + "' " +
            " and voucher_month='" + month + "'";

          statValidVoucher.executeQuery(sql);
        }
        sql = "delete  vouchers " +
          "   WHERE     headquarter_orgcode IN (" + headerCodes + ") " +
          " and voucher_year='" + year + "' " +
          " and voucher_month='" + month + "'" +
          " and  VALID_FLAG = '0'";

        statDeleteVoucher.executeQuery(sql);

      } catch (SQLException e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
        continue;
      } catch (Exception e) {
        logger.warning(sql);
        logger.severe(e.getMessage());
        continue;
      }
    }

    try {
      JdbcUtils.releaseStatement(statSelect, resultSelect);
      JdbcUtils.releaseStatement(statVoucherNo, resultVoucherNo);
      JdbcUtils.releaseStatement(statUpdateTrans);
      JdbcUtils.releaseStatement(statUpdateVoucher);
      JdbcUtils.releaseStatement(statValidVoucher);
      JdbcUtils.releaseStatement(statDeleteVoucher);
      JdbcUtils.releaseConn(conn);
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return true;
  }

  private static boolean loadToAdjustTableNames(SYSTEM_TYPE systemType) {
    Connection conn = null;
    Statement statement = null;
    ResultSet result = null;
    String sql = "";
    try {
      conn = JdbcUtils.getOracleConnection();
      if (conn == null)
        return false;

      String headerCodes = "";
      if (systemType == SYSTEM_TYPE.EC)
        headerCodes = JdbcUtils.EC_HEAD_CODES;
      else if (systemType == SYSTEM_TYPE.PC)
        headerCodes = JdbcUtils.PC_HEAD_CODES;

      sql = "SELECT system_code||'_'||voucher_year||LPAD (voucher_month , 2 , '0') ym,voucher_year,voucher_month " +
        " FROM VOUCHERS where HEADQUARTER_ORGCODE in (" + headerCodes
        + ") group by voucher_year, voucher_month,system_code ";
      statement = conn.createStatement();
      statement.setQueryTimeout(43200);
      result = statement.executeQuery(sql);

      while (result.next()) {
        String tableName = "feelog_" + result.getString("ym");
        if (systemType == SYSTEM_TYPE.EC)
          TO_ADJUST_EC_FEELOG_TABLE_NAMES.add(tableName.trim());
        else if (systemType == SYSTEM_TYPE.PC)
          TO_ADJUST_PC_FEELOG_TABLE_NAMES.add(tableName.trim());

        Pair<String, String> item = new Pair<>(result.getString("voucher_year"), result.getString("voucher_month"));
        tableName = "TRANSACTIONS_" + result.getString("ym");
        if (systemType == SYSTEM_TYPE.EC) {
          TO_ADJUST_EC_TRANS_TABLES.put(tableName, item);
        } else if (systemType == SYSTEM_TYPE.PC) {
          TO_ADJUST_PC_TRANS_TABLES.put(tableName, item);
        }
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

  public enum SYSTEM_TYPE {
    EC("EC"), PC("PC");
    String value;

    SYSTEM_TYPE(String type) {
      this.value = type;
    }

    public String getValue() {
      return this.value;
    }
  }

}
