package utils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class JdbcUtils {
  public static String EC_HEAD_CODES = null;
  public static String PC_HEAD_CODES = null;
  private static Logger logger = TransLogger.getLogger(JdbcUtils.class);

  static {
    loadPorperties();
  }

  public static void loadPorperties() {
    try {
      //读取db.properties文件中的数据库连接信息
      InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("config.properties");
      Properties prop = new Properties();
      prop.load(in);

      EC_HEAD_CODES = prop.getProperty("ec_head_codes");
      PC_HEAD_CODES = prop.getProperty("pc_head_codes");
    } catch (Exception e) {
      e.printStackTrace();
      logger.severe(e.getMessage());
      throw new ExceptionInInitializerError(e);
    }
  }

  public static Connection getOracleConnection() throws InterruptedException {
    try {
      DataSource dataSource = DataSourceUtil.getInstance().getDataSource();
      Connection conn = dataSource.getConnection();
      return conn;
    } catch (Exception e) {
      logger.severe(e.getMessage());
      e.printStackTrace();
    }

    return null;
  }


  public static void releaseStatement(Statement st) {

    if (st != null) {
      try {
        //关闭负责执行SQL命令的Statement对象
        st.close();
      } catch (Exception e) {
        logger.warning(e.getMessage());
      }
    }

  }

  public static void releaseStatement(Statement st, ResultSet rs) {
    if (rs != null) {
      try {
        //关闭存储查询结果的ResultSet对象
        rs.close();
      } catch (Exception e) {
        logger.warning(e.getMessage());
      }
      rs = null;
    }
    if (st != null) {
      try {
        //关闭负责执行SQL命令的Statement对象
        st.close();
      } catch (Exception e) {
        logger.warning(e.getMessage());
      }
    }

  }


  public static void releaseConn(Connection conn) {
    if (conn != null) {
      try {
        //关闭Connection数据库连接对象
        conn.close();
      } catch (Exception e) {
        logger.warning(e.getMessage());
      }
    }
  }

}
