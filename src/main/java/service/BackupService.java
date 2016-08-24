package service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import processor.BackupPorcessor;
import utils.TransLogger;

import java.sql.SQLException;
import java.util.logging.Logger;

public class BackupService extends Service<Integer> {
  private static Logger logger = TransLogger.getLogger(BackupService.class);


  protected Task<Integer> createTask() {
    return new Task<Integer>() {
      @Override
      public Integer call() throws SQLException {
        logger.info("开始时间 : " + (new java.util.Date()).toString());
        updateProgress(0, 1);
        updateTitle("备份执行中...");
        
        BackupPorcessor.doBackup();

        updateProgress(1, 1);
        updateTitle("备份完成");


        return null;
      }
    };
  }
}
