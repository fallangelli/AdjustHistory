package service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import processor.AdjustPorcessor;
import utils.TransLogger;

import java.sql.SQLException;
import java.util.logging.Logger;

public class AdjustService extends Service<Integer> {
  private static Logger logger = TransLogger.getLogger(AdjustService.class);


  protected Task<Integer> createTask() {
    return new Task<Integer>() {
      @Override
      public Integer call() throws SQLException {
        logger.info("开始时间 : " + (new java.util.Date()).toString());
        updateProgress(0, 1);
        updateTitle("数据修正执行中...");

        AdjustPorcessor.doAdjust();

        updateProgress(1, 1);
        updateTitle("数据修正完成");


        return null;
      }
    };
  }
}
