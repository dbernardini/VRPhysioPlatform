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

    Scene offlineScene, realtimeScene;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/OfflineScene.fxml"));
        offlineScene = new Scene(root);
        offlineScene.getStylesheets().add("/styles/OfflineScene.css");
        
        root = FXMLLoader.load(getClass().getResource("/fxml/RealtimeScene.fxml"));
        realtimeScene = new Scene(root);
        realtimeScene.getStylesheets().add("/styles/RealtimeScene.css");
        
        stage.setTitle("VRPhysioPlatform");
        stage.setScene(realtimeScene);
        stage.setMaximized(true);
        
        
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        super.stop(); 
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.load(getClass().getResource("/fxml/RealtimeScene.fxml").openStream());
        RealtimeFXMLController controller = (RealtimeFXMLController) fxmlLoader.getController();
        controller.stopAcquisitions();
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
