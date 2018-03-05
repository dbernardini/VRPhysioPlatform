
package com.bernardini.vrphysioplatform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_highgui;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import org.bytedeco.javacpp.opencv_videoio;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_ANY;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import org.bytedeco.javacpp.opencv_videoio.CvCapture;
import static org.bytedeco.javacpp.opencv_videoio.cvCreateCameraCapture;
import static org.bytedeco.javacpp.opencv_videoio.cvQueryFrame;
import static org.bytedeco.javacpp.opencv_videoio.cvReleaseCapture;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import plux.newdriver.bioplux.BPException;
import plux.newdriver.bioplux.Device;

/**
 *
 * @author danilo.bernardi
 */
public class RealtimeFXMLController implements Initializable {

    private static final String DEVICE = "00:07:80:FC:57:19";
    private static final String STREAMING_SERVER_URL = "rtmp://127.0.0.1:1936/live";
    
    Logger log = Logger.getLogger(OfflineFXMLController.class.getName());

    @FXML
    private AnchorPane anchorPane1, anchorPane2, anchorPane;

    @FXML
    private MediaView mediaView1, mediaView2;
    
    @FXML
    private ImageView imageView;

    @FXML
    private Label timeLabel1, timeLabel2, timeLabelSignals, timeLabel;
    
    @FXML
    private Button playAll, pauseAll, stopAll, insertMark;
    
    @FXML
    private LineChart<String, Number> chart00, chart01;
    
    @FXML
    private GridPane topGridPane;
    
    XYChart.Series<String, Number> series1, series2, series3, series4;

    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;

    private boolean play = false;
    private boolean stop = false;
    private int signalsCounter = 0;
    private BufferedReader br;
    private int samplingRate;
    private double signalsDuration;
    private double stageWidth;
    private double stageHeigth;
    private int markerRadius;
    private Scene scene;
    private Stage stage;
    
    private ArrayList<String> signals = new ArrayList<>();
    private static volatile Thread playThread;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        log.log(Level.INFO, "TS 1: {0}", timestamp.getTime());
//        timestamp = new Timestamp(System.currentTimeMillis());
//        log.log(Level.INFO, "TS 2: {0}", timestamp.getTime());
//        timestamp = new Timestamp(System.currentTimeMillis());
//        log.log(Level.INFO, "TS 3: {0}", timestamp.getTime());
//        timestamp = new Timestamp(System.currentTimeMillis());
//        log.log(Level.INFO, "TS 4: {0}", timestamp.getTime());
//                try
//     {
//        // decomment next block to search for bioPlux devices
//        
//        Vector devs = new Vector();
//        Device.FindDevices(devs);
//        System.out.println("Found devices: " + devs);
//        
//
////        Device dev = new Device("Test"); // virtual test device (no connection to a physical device)
//        Device dev = new Device("DEVICE"); // MAC address of physical device
//
//        System.out.println("Description: " + dev.GetDescription());
//
//        // initialize frames vector and each frame within the vector
//        Device.Frame[] frames = new Device.Frame[1000];
//        for (int i = 0; i < 1000; i++)
//           frames[i] = new Device.Frame();
//
//        dev.BeginAcq(1000, 0xFF, 12); // 1000 Hz, 8 channels, 12 bits
//        dev.GetFrames(1000, frames); // get first 1000 frames
//
//        for (int i = 0; i < 20; i++) // print first 20 frames
//        {
//           System.out.print("seq: " + frames[i].seq + " dig_in: " + frames[i].dig_in + " an_in: ");
//           for (int j = 0; j < 8; j++)
//              System.out.print(frames[i].an_in[j] + " , ");
//           System.out.println();
//        }
//
//        dev.EndAcq();
//        dev.Close();
//     }
//     catch (BPException err)
//     {
//        System.out.println("Exception: " + err.code + " - " + err.getMessage());
//     }                



        
        /*      INITIALIZE VR VIDEO STREAM     */
//        Media media = new Media(STREAMING_SERVER_URL);
//        log.log(Level.INFO, "Media: {0}", media.getSource());
//        mediaPlayer1 = new MediaPlayer(media);
//        mediaPlayer1.play();
//        mediaView1.setMediaPlayer(mediaPlayer1);
//
//        timeLabel1.textProperty().bind(
//            Bindings.createStringBinding(() -> {
//                    Duration time = mediaPlayer1.getCurrentTime();
//                    return String.format("%4d:%02d:%04.1f",
//                        (int) time.toHours(),
//                        (int) time.toMinutes() % 60,
//                        time.toSeconds() % 3600);
//                },
//                mediaPlayer1.currentTimeProperty()));
//
//
//        DoubleProperty width = mediaView1.fitWidthProperty();
//        DoubleProperty height = mediaView1.fitHeightProperty();
//
//        width.bind(Bindings.selectDouble(mediaView1.parentProperty(), "width"));
//        height.bind(Bindings.selectDouble(mediaView1.parentProperty(), "height"));
//
//        stageWidth = stage.getWidth();
//        stageHeigth = stage.getHeight();
        

        /*      INITIALIZE CAMERA VIDEO STREAM     */
        GridPane.setHalignment(imageView, HPos.CENTER);
        
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        Java2DFrameConverter converter = new Java2DFrameConverter();
        
        try {
            grabber.start();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        playThread = new Thread(() -> {
            Frame capturedFrame = null;
            try {
                while(true){
                    capturedFrame = grabber.grab();
                    Image image = SwingFXUtils.toFXImage(converter.convert(capturedFrame), null);
                    imageView.setImage(image);
                }
            } catch (FrameGrabber.Exception ex) {
                Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        playThread.setDaemon(true);
        playThread.start();

       
        
    }


    @FXML
    private void playVideo1(ActionEvent event) {
        if (mediaPlayer1 != null)
            mediaPlayer1.play();
    }

    @FXML
    private void pauseVideo1(ActionEvent event) {
        if (mediaPlayer1 != null)
            mediaPlayer1.pause();
    }

    @FXML
    private void stopVideo1(ActionEvent event) {
        if (mediaPlayer1 != null)
            mediaPlayer1.stop();
    }

    @FXML
    private void playVideo2(ActionEvent event) {
        if (mediaPlayer2 != null)
            mediaPlayer2.play();
    }

    @FXML
    private void pauseVideo2(ActionEvent event) {
        if (mediaPlayer2 != null)
            mediaPlayer2.pause();
    }

    @FXML
    private void stopVideo2(ActionEvent event) {
        if (mediaPlayer2 != null)
            mediaPlayer2.stop();
    }
    
    @FXML
    private void openSignals(ActionEvent event) {
        try {
//            Thread t = new Thread(new ReadData());
//            t.setDaemon(true);
//            t.start();


//                Platform.runLater(() -> {
//                    readData();
//                });


//            Task task = new Task<Void>() {
//                @Override public Void call() {
//                    readData();
//                    return null;
//                }
//            };
//            new Thread(task).start();


        }
        catch (Exception ex) {
            Logger.getLogger(OfflineFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @FXML
    private void playSignals(ActionEvent event) {
        play = true;
    }

    @FXML
    private void pauseSignals(ActionEvent event) {
        play = false;
    }

    @FXML
    private void stopSignals(ActionEvent event) {
        play = false;
        stop = true;
        signalsCounter = 0;
    }
    
    @FXML
    private void playAll(ActionEvent event) {
        playVideo1(event);
        playVideo2(event);
        playSignals(event);
    }

    @FXML
    private void pauseAll(ActionEvent event) {
        pauseVideo1(event);
        pauseVideo2(event);
        pauseSignals(event);
    }

    @FXML
    private void stopAll(ActionEvent event) {
        stopVideo1(event);
        stopVideo2(event);
        stopSignals(event);
    }
    
    @FXML
    private void insertMarker(ActionEvent event) {

//        double offset = slider.getValue()*slider.getWidth()/slider.getMax();
//
//        Marker marker = new Marker(8, slider.getHeight(), offset);
//        marker.setFill(Color.RED);
//        
//        marker.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                log.log(Level.INFO, "marker offset: {0}", marker.getOffset());
//                double value = marker.getOffset()*slider.getMax()/slider.getWidth();
//                slider.setValue(value);
//            }
//        });
//        
//        marker.setOnMouseEntered(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                scene.setCursor(Cursor.HAND);
//            }
//        });
//        
//        marker.setOnMouseExited(new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                scene.setCursor(Cursor.DEFAULT);
//            }
//        });
//        
//        AnchorPane.setBottomAnchor(marker, 5.0);
//        AnchorPane.setLeftAnchor(marker, offset);
////        log.log(Level.INFO, "offset: {0}", offset);
//        
//        anchorPane.getChildren().add(marker);
//                
////        currentTime:totalTime=offset:slider
    }
    
    @FXML
    private void switchScene(ActionEvent event) throws IOException{
        stage = (Stage) chart00.getScene().getWindow();
        BorderPane root = FXMLLoader.load(getClass().getResource("/fxml/OfflineScene.fxml"));
        stage.getScene().setRoot(root);
    }
    
    private void readData(){
        String line = null;
            try {
//                for (int i = 0; i < 100; i++) {

                while (true){
                    Thread.sleep(1);
                    if (stop){
                        play = false;
                        stop = false;
                    }
                    if (play){
                        
                        Thread.sleep(1000);
                        
                        log.log(Level.INFO, "Signals counter: {0}", signalsCounter);
                        int index = signalsCounter * samplingRate;
                        
                        if (index < signals.size())
                            line = signals.get(signalsCounter * samplingRate);
                        else break;
                        
                        if (line != null){

                            String[] parts = line.split("\t");
                            
                            log.log(Level.INFO, "NOT NULL");
                            if (series1.getData().size() >= 10){
                                series1.getData().remove(0);
                                series2.getData().remove(0);
                                series3.getData().remove(0);
                                series4.getData().remove(0);
                            }
                            
                            
                            series1.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[2])));
                            series2.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[3])));
                            series3.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[4])));
                            series4.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[5])));

                        }
                        else{
                            break;
                        }
                    }
                }
                log.log(Level.INFO, "EXIT LOOP");
                br.close();
            }
            catch (Exception ex) {
                Logger.getLogger(OfflineFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    class ReadData implements Runnable {

        @Override
        public void run() {
            readData();
        }

    }
}
