package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class TransLogger {

  private static Logger logger;

  static {
    InputStream is1 = null;
    InputStream is2 = null;
    try {
      // 读取配置文件,创建相应的文件目录
      String logFilePath = "";
      is1 = TransLogger.class.getClassLoader().getResourceAsStream("log.properties");

      Properties properties = new Properties();
      properties.load(is1);
      String pattern = properties
        .getProperty("java.util.logging.FileHandler.pattern");
      logFilePath = pattern.substring(0, pattern.lastIndexOf("/"));
      File file = new File(logFilePath);
      if (!file.exists()) {
        file.mkdirs();
      }
      // 重新初始化日志属性并重新读取日志配置
      is2 = TransLogger.class.getClassLoader().getResourceAsStream("log.properties");
      LogManager logManager = LogManager.getLogManager();
      logManager.readConfiguration(is2);
    } catch (SecurityException e) {
      logger.severe(e.getMessage());
    } catch (IOException e) {
      logger.severe(e.getMessage());
    } finally {
      try {
        is1.close();
        is2.close();
      } catch (IOException e) {
        logger.severe(e.getMessage());
      }
    }
  }

  /**
   * 获取日志对象
   *
   * @param clazz
   * @return
   */
  public static Logger getLogger(Class<?> clazz) {
    logger = Logger.getLogger(clazz.getName());
    return logger;
  }
}
