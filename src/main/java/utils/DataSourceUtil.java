package utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class DataSourceUtil.
 */
public class DataSourceUtil {
  private final static DataSourceUtil INSTANCE = new DataSourceUtil();
  public static Properties p = null;

  static {
    p = new Properties();
    InputStream inputStream = null;
    try {
      //java应用
      InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("druid.properties");

      p.load(in);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private DataSource dds = null;

  private DataSourceUtil() {
  }

  public static DataSourceUtil getInstance() {
    return INSTANCE;
  }

  public final DataSource getDataSource() throws Exception {
    if (dds == null)
      dds = DruidDataSourceFactory.createDataSource(p);
    return dds;
  }

}
