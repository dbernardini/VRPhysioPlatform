
package com.bernardini.vrphysioplatform;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import plux.newdriver.bioplux.*;

/**
 *
 * @author danilo.bernardi
 */
public class RealtimeFXMLController implements Initializable {

    @FXML
    private ImageView imageView;

    @FXML
    private Button playAll, pauseAll, stopAll, insertMark;
    
    @FXML
    private LineChart<String, Number> ecgChart, emgChart, forceChart, tempChart,
            edaChart, respChart, eegChart, accChart;
    
    private XYChart.Series<String, Number> seriesX, seriesY, seriesZ, seriesTemp,
            seriesEda, seriesEcg, seriesEmg, seriesForce, seriesResp, seriesEeg;

    private static final String STREAMING_SERVER_URL = "rtmp://127.0.0.1:1936/live";
    Logger log = Logger.getLogger(OfflineFXMLController.class.getName());
    private boolean play = false;
    private boolean stop = false;
    private int signalsCounter = 10;
    private Stage stage;
    private String deviceMAC;
    private Device dev;
    private Device.Frame[] frames;
    private OpenCVFrameGrabber grabber;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        /*      SETUP SENSORS     */
        
        seriesX = new XYChart.Series<>();
        seriesX.setName("X");
        seriesY = new XYChart.Series<>();
        seriesY.setName("Y");
        seriesZ = new XYChart.Series<>();
        seriesZ.setName("Z");
        seriesTemp = new XYChart.Series<>();
        seriesTemp.setName("Temp");
        seriesEcg = new XYChart.Series<>();
        seriesEcg.setName("ECG");
        seriesEda = new XYChart.Series<>();
        seriesEda.setName("EDA");
        seriesEeg = new XYChart.Series<>();
        seriesEeg.setName("EEG");
        seriesEmg = new XYChart.Series<>();
        seriesEmg.setName("EMG");
        seriesForce = new XYChart.Series<>();
        seriesForce.setName("Force");
        seriesResp = new XYChart.Series<>();
        seriesResp.setName("Resp");
        
        for (int i = 0; i < 10; i++){
            seriesX.getData().add(new XYChart.Data<>(i + "", 0));
            seriesY.getData().add(new XYChart.Data<>(i + "", 0));
            seriesZ.getData().add(new XYChart.Data<>(i + "", 0));
            seriesTemp.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesEcg.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesEda.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesEeg.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesEmg.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesForce.getData().add(new XYChart.Data<>(i + "", 0));
//            seriesResp.getData().add(new XYChart.Data<>(i + "", 0));
        }
        
        accChart.getData().add(seriesX);
        accChart.getData().add(seriesY);
        accChart.getData().add(seriesZ);
        tempChart.getData().add(seriesTemp);
//        ecgChart.getData().add(seriesEcg);
//        emgChart.getData().add(seriesEmg);
//        eegChart.getData().add(seriesEeg);
//        edaChart.getData().add(seriesEda);
//        forceChart.getData().add(seriesForce);
//        respChart.getData().add(seriesResp);
              
   
        try {
       
            Vector devs = new Vector();
            Device.FindDevices(devs);
            deviceMAC = (String) devs.get(0);

           
            dev = new Device(deviceMAC); // MAC address of physical device

            // initialize frames vector and each frame within the vector
            frames = new Device.Frame[1000];
            for (int i = 0; i < 1000; i++)
               frames[i] = new Device.Frame();

            dev.BeginAcq(1000, 0xFF, 16); // 1000 Hz, 8 channels, 16 bits

        }
        catch (BPException err){
            System.out.println("Exception: " + err + " - " + err.getMessage());
        }         
        
        Timeline updateGraphs = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    dev.GetFrames(1000, frames); 
                 
                    if (seriesX.getData().size() >= 10){
                        seriesX.getData().remove(0);
                        seriesY.getData().remove(0);
                        seriesZ.getData().remove(0);
                        seriesTemp.getData().remove(0);
//                        seriesEcg.getData().remove(0);
//                        seriesEda.getData().remove(0);
//                        seriesEeg.getData().remove(0);
//                        seriesEmg.getData().remove(0);
//                        seriesResp.getData().remove(0);
//                        seriesForce.getData().remove(0);
                    }

                    seriesX.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[0]));
                    seriesY.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[1]));
                    seriesZ.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[2]));
                    seriesTemp.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[3]));
//                    seriesEcg.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[4]));
//                    seriesEeg.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[5]));
//                    seriesResp.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[6]));
//                    seriesEda.getData().add(new XYChart.Data<>(signalsCounter + "", frames[0].an_in[7]));
                            
                    signalsCounter++;
                        
                } catch (BPException ex) {
                    Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        updateGraphs.setCycleCount(Timeline.INDEFINITE);
        updateGraphs.play(); 


        /*      SETUP CAMERA VIDEO STREAM     */
        GridPane.setHalignment(imageView, HPos.CENTER);
        
        grabber = new OpenCVFrameGrabber(0);
        Java2DFrameConverter converter = new Java2DFrameConverter();
        
        try {
            grabber.start();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Thread t = new Thread(() -> {
            Frame capturedFrame = null;
            try {
                while((capturedFrame = grabber.grab()) != null){
                    Image image = SwingFXUtils.toFXImage(converter.convert(capturedFrame), null);
                    imageView.setImage(image);
                }
            } catch (Exception ex) {
                Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        t.setDaemon(true);
        t.start();

    }
    
    protected void stopAcquisitions(){
        try {
            dev.EndAcq();
            dev.Close();
            
            grabber.stop();
            grabber.close();
            System.out.println("STOP");
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BPException ex) {
            Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void switchScene(ActionEvent event) throws IOException{
        stage = (Stage) imageView.getScene().getWindow();
        BorderPane root = FXMLLoader.load(getClass().getResource("/fxml/OfflineScene.fxml"));
        stage.getScene().setRoot(root);
    }
    
}
