package service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import utils.TransLogger;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WapperService extends Service<Integer> {
  private static Logger logger = TransLogger.getLogger(WapperService.class);

  private ExecutorService es;
  private BooleanProperty runningFlag = new SimpleBooleanProperty(false);

  public ExecutorService getEs() {
    return es;
  }

  public void setEs(ExecutorService es) {
    this.es = es;
  }

  public final Boolean getRunningFlag() {
    return runningFlag.get();
  }

  public final void setRunningFlag(Boolean value) {
    runningFlag.set(value);
  }

  public final BooleanProperty runningFlagProperty() {
    return runningFlag;
  }

  protected Task<Integer> createTask() {
    return new Task<Integer>() {
      @Override
      public Integer call() throws SQLException {
        try {
          es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
          setRunningFlag(false);
          e.printStackTrace();
        }
        setRunningFlag(false);
        return -1;
      }
    };
  }
}
