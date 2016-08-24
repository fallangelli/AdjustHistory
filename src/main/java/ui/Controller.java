package ui;

import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import service.BackupService;
import utils.TransLogger;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Controller {
  private static Logger logger = TransLogger.getLogger(Controller.class);

  private static int CHECK_CERTNO_PARTITION_COUNT = 64;
  private static int CHECK_CERTNO_THREAD_POOL_SIZE = 10;

  private static int MERGE_THREAD_PARTITION_COUNT = 10;
  private static int MERGE_THREAD_POOL_SIZE = 10;

  private static int CHECK_DIC_CODE_THREAD_PROCESS_COUNT = 1000;
  private static int CHECK_DIC_CODE_THREAD_POOL_SIZE = 20;

  @FXML //  fx:id="btnBackup"
  private Button btnBackup; // Value injected by FXMLLoader
  @FXML //  fx:id="btnAdjust"
  private Button btnAdjust; // Value injected by FXMLLoader
  @FXML //  fx:id="vboxBase"
  private VBox vboxBase; // Value injected by FXMLLoader
  @FXML //  fx:id="scrollPane"
  private ScrollPane scrollPane; // Value injected by FXMLLoader
  @FXML //  fx:id="vBoxItems"
  private VBox vBoxItems; // Value injected by FXMLLoader
  @FXML //  fx:id="labInfo"
  private Label labInfo; // Value injected by FXMLLoader
  @FXML //  fx:id="labRunning"
  private Label labRunning; // Value injected by FXMLLoader

  /**
   * Initializes the controller class.
   */
  @FXML
  // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert btnBackup != null : "fx:id=\"btnBackup\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert btnAdjust != null : "fx:id=\"btnAdjust\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert labInfo != null : "fx:id=\"labInfo\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert labRunning != null : "fx:id=\"labRunning\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert vboxBase != null : "fx:id=\"vboxBase\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";
    assert vBoxItems != null : "fx:id=\"vBoxItems\" was not injected: check your FXML file 'IssueTrackingLite.fxml'.";

    try {
      InputStream in = Controller.class.getClassLoader().getResourceAsStream("config.properties");
      Properties prop = new Properties();

    } catch (Exception e) {
      logger.severe(e.getMessage());
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("错误");
      alert.setHeaderText("读取配置文件config.properties 错误");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
      System.exit(0);
    }

  }


  @FXML
  void btnBackupFired(ActionEvent event) throws InterruptedException {
    try {
      vBoxItems.getChildren().clear();
      BackupService service = new BackupService();

      final Label label = new Label();
      label.setPrefWidth(600);
      label.setWrapText(true);
      label.textProperty().bind(service.titleProperty());

      final ProgressBar pb = new ProgressBar();
      pb.setPrefWidth(300);
      pb.setProgress(0);

      pb.progressProperty().bind(service.progressProperty());

      final VBox vb = new VBox();
      vb.setSpacing(15);
      vb.setAlignment(Pos.CENTER_LEFT);
      vb.getChildren().addAll(label, pb);

      bindButtonState(service);
      service.start();

      vBoxItems.getChildren().addAll(vb);

    } catch (Exception e) {
      logger.severe(e.getMessage());
    }

  }


  @FXML
  void btnAdjustFired(ActionEvent event) {

  }

  private void bindButtonState(Service<Integer> service) {
    btnBackup.disableProperty().bind(service.runningProperty());
    btnAdjust.disableProperty().bind(service.runningProperty());

    labRunning.textProperty().bind(service.runningProperty().asString());
  }

}
