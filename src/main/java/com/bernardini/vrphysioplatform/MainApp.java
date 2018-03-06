package com.bernardini.vrphysioplatform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainApp extends Application {

    private Scene offlineScene, realtimeScene;
    private RealtimeFXMLController realtimeController; 
    private OfflineFXMLController offlineController; 
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader offlineFxmlLoader = new FXMLLoader(getClass().getResource("/fxml/OfflineScene.fxml"));
        Parent offlineRoot = offlineFxmlLoader.load();
        offlineController = offlineFxmlLoader.getController();
        offlineScene = new Scene(offlineRoot);
        offlineScene.getStylesheets().add("/styles/OfflineScene.css");
        
        FXMLLoader realtimeFxmlLoader = new FXMLLoader(getClass().getResource("/fxml/RealtimeScene.fxml"));
        Parent realtimeRoot = realtimeFxmlLoader.load();
        realtimeController = realtimeFxmlLoader.getController();
        realtimeScene = new Scene(realtimeRoot);
        realtimeScene.getStylesheets().add("/styles/RealtimeScene.css");
        
        stage.setTitle("VRPhysioPlatform");
        stage.setScene(realtimeScene);
        stage.setMaximized(true);
        
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        super.stop(); 
        realtimeController.stopAcquisitions();
    }

    /** 
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
