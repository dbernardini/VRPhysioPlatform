
package com.bernardini.vrphysioplatform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import plux.newdriver.bioplux.BPException;
import plux.newdriver.bioplux.Device;

/**
 *
 * @author danilo.bernardi
 */
public class FXMLController implements Initializable {

    private static final String DEVICE = "00:07:80:FC:57:19";
    private static final double MIN_CHANGE = 0.1;
    private static final double SLIDER_MARGIN_RL = 5;
    private static final double SLIDER_MARGIN_BOTTOM = 2;
    
    Logger log = Logger.getLogger(FXMLController.class.getName());

    @FXML
    private AnchorPane anchorPane1, anchorPane2, anchorPane;

    @FXML
    private MediaView mediaView1, mediaView2;
    
    @FXML
    private Slider slider1, slider2, sliderSignals, slider;
    
    @FXML
    private Label timeLabel1, timeLabel2, timeLabelSignals, timeLabel;
    
    @FXML
    private Button open1, play1, pause1, stop1, open2, play2, pause2, stop2,
            openSignals, playSignals, pauseSignals, stopSignals, playAll, 
            pauseAll, stopAll, insertMark;
    
    @FXML
    private LineChart<String, Number> chart00, chart01;
    
    XYChart.Series<String, Number> series1, series2, series3, series4;

    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private String videoPath1;
    private String videoPath2;
    private String signalsPath;
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
    
    private ArrayList<Integer> timestamps = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
    private ArrayList<Integer> data1 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data2 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data3 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data4 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data5 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data6 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data7 = new ArrayList<>(Collections.nCopies(10, 0));
    private ArrayList<Integer> data8 = new ArrayList<>(Collections.nCopies(10, 0));

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
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

        series1 = new XYChart.Series<>();
        series1.setName("X");
        series2 = new XYChart.Series<>();
        series2.setName("Y");
        series3 = new XYChart.Series<>();
        series3.setName("Z");
        series4 = new XYChart.Series<>();
        series4.setName("Temp");
       
        chart00.getData().add(series1);
        chart00.getData().add(series2);
        chart00.getData().add(series3);
        chart01.getData().add(series4);

        
    }

    @FXML
    private void openVideo1(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a video file", "*.mp4");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        
        videoPath1 = file.toURI().toString();
        if (videoPath1 != null) {
            Media media = new Media(videoPath1);
            mediaPlayer1 = new MediaPlayer(media);
            mediaView1.setMediaPlayer(mediaPlayer1);
            
            mediaPlayer1.totalDurationProperty().addListener((obs, oldDuration, newDuration) -> slider1.setMax(newDuration.toSeconds()));
            
            slider1.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (! isChanging) {
                    mediaPlayer1.seek(Duration.seconds(slider1.getValue()));
                }
            });
            
            slider1.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (! slider1.isValueChanging()) {
                    double currentTime = mediaPlayer1.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newValue.doubleValue()) > MIN_CHANGE) {
                        mediaPlayer1.seek(Duration.seconds(newValue.doubleValue()));
                    }
                }
            });

            mediaPlayer1.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (! slider1.isValueChanging()) {
                    slider1.setValue(newTime.toSeconds());
                }
            });
            
            
            
            timeLabel1.textProperty().bind(
                Bindings.createStringBinding(() -> {
                        Duration time = mediaPlayer1.getCurrentTime();
                        return String.format("%4d:%02d:%04.1f",
                            (int) time.toHours(),
                            (int) time.toMinutes() % 60,
                            time.toSeconds() % 3600);
                    },
                    mediaPlayer1.currentTimeProperty()));
            
            play1.disableProperty().set(false);
            pause1.disableProperty().set(false);
            stop1.disableProperty().set(false);
            open2.disableProperty().set(false);
            
            markerRadius = (int)(slider.getHeight()/2);
        

            DoubleProperty width = mediaView1.fitWidthProperty();
            DoubleProperty height = mediaView1.fitHeightProperty();
            
            width.bind(Bindings.selectDouble(mediaView1.parentProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView1.parentProperty(), "height"));

            scene = anchorPane.getScene();
            stage = (Stage) scene.getWindow();
            stageWidth = stage.getWidth();
            stageHeigth = stage.getHeight();
            
        }
    }

    @FXML
    private void openVideo2(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a video file", "*.mp4");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        videoPath2 = file.toURI().toString();
        if (videoPath2 != null) {
            Media media = new Media(videoPath2);
            mediaPlayer2 = new MediaPlayer(media);
            mediaView2.setMediaPlayer(mediaPlayer2);
      
            mediaPlayer2.totalDurationProperty().addListener((obs, oldDuration, newDuration) -> slider2.setMax(newDuration.toSeconds()));
            
            slider2.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (! isChanging) {
                    mediaPlayer2.seek(Duration.seconds(slider2.getValue()));
                }
            });
            
            slider2.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (! slider2.isValueChanging()) {
                    double currentTime = mediaPlayer2.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newValue.doubleValue()) > MIN_CHANGE) {
                        mediaPlayer2.seek(Duration.seconds(newValue.doubleValue()));
                    }
                }
            });

            mediaPlayer2.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (! slider2.isValueChanging()) {
                    slider2.setValue(newTime.toSeconds());
                }
            });
            
            timeLabel2.textProperty().bind(
                Bindings.createStringBinding(() -> {
                        Duration time = mediaPlayer2.getCurrentTime();
                        return String.format("%4d:%02d:%04.1f",
                            (int) time.toHours(),
                            (int) time.toMinutes() % 60,
                            time.toSeconds() % 3600);
                    },
                    mediaPlayer2.currentTimeProperty()));
            
            play2.disableProperty().set(false);
            pause2.disableProperty().set(false);
            stop2.disableProperty().set(false);
            openSignals.disableProperty().set(false);

         
            DoubleProperty width = mediaView2.fitWidthProperty();
            DoubleProperty height = mediaView2.fitHeightProperty();
            width.bind(Bindings.selectDouble(mediaView1.parentProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView1.parentProperty(), "height"));
            

        }
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
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file", "*.txt");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null){
            signalsPath = file.toURI().toString().replace("file:/", "");

            String line;
            try {
                br = new BufferedReader(new FileReader(signalsPath));
                br.readLine();
                String properties = br.readLine().substring(2);
                line = br.readLine();
                while (line != null){
                    line = br.readLine();
                    signals.add(line);
                    if (signalsCounter < 10){
                        String[] parts = line.split("\t");

                        series1.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[2])));
                        series2.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[3])));
                        series3.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[4])));
                        series4.getData().add(new XYChart.Data<>(signalsCounter + "", Integer.parseInt(parts[5])));
                        signalsCounter++;
                    }
                }
                br.close();

                



                sliderSignals.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                    if (! isChanging) 
                        signalsCounter = (int)sliderSignals.getValue();
                });


                sliderSignals.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (! sliderSignals.isValueChanging()) 
                        signalsCounter = (int)sliderSignals.getValue()+10;
                });
                
                playSignals.disableProperty().set(false);
                pauseSignals.disableProperty().set(false);
                stopSignals.disableProperty().set(false);
                
                playAll.disableProperty().set(false);
                pauseAll.disableProperty().set(false);
                stopAll.disableProperty().set(false);
                insertMark.disableProperty().set(false);

                
                String last = signals.get(signals.size()-2);
                double lenght = Double.parseDouble(last.split("\t")[0]);
                
                sliderSignals.setValue(0);
                sliderSignals.setBlockIncrement(1);
                
                
                double duration1 = mediaPlayer1.totalDurationProperty().getValue().toSeconds();
                double duration2 = mediaPlayer2.totalDurationProperty().getValue().toSeconds();
                
                JsonParser parser = new JsonParser();
                JsonObject o = parser.parse(properties).getAsJsonObject();
                JsonObject dev = o.getAsJsonObject(DEVICE);
                samplingRate = dev.get("sampling rate").getAsInt();

                signalsDuration = lenght/samplingRate;
                
                sliderSignals.setMax(signalsDuration);
                log.log(Level.INFO, "signalsDuration: {0}", signalsDuration);

                double min = Math.min(signalsDuration, Math.min(duration1, duration2));
                
                slider.setMax(min);
                slider.setValue(0);
                slider.setBlockIncrement(1);
                
                if (min == duration1){
                    slider1.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (! slider1.isValueChanging()) {
                            slider.setValue(slider1.getValue());
                        }
                    });
                }
                else if (min == duration2){
                    slider2.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (! slider2.isValueChanging()) {
                            slider.setValue(slider2.getValue());
                        }
                    });
                }
                else {
                    sliderSignals.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (! sliderSignals.isValueChanging()) {
                            slider.setValue(sliderSignals.getValue());
                        }
                    });
                }
                
                slider.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (! slider.isValueChanging()) {
                            double value = slider.getValue();
//                            valoreSlider : valoreMaxSlider = valore1 : valoreMax1
//                            double value1 = slider.getValue() * (slider1.getMax()-min) / slider.getMax();
//                            double value2 = slider.getValue() * (slider2.getMax()-min) / slider.getMax();
//                            double valueSignals = slider.getValue() * (signalsDuration-min) / slider.getMax();
                            slider1.setValue(value);
                            slider2.setValue(value);
                            sliderSignals.setValue(value);
                        }
                    });
                

                Thread t = new Thread(new ReadData());
                t.setDaemon(true);
                t.start();

            
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
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        sliderSignals.setValue(0);
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
//        double currentTime = slider.getValue();
        
//        log.log(Level.INFO, "sliderWidth: {0}", slider1.getWidth());
//        log.log(Level.INFO, "sliderValue: {0}", slider1.getValue());
//        log.log(Level.INFO, "sliderMax: {0}", slider1.getMax());
        
        double offset = slider.getValue()*slider.getWidth()/slider.getMax();
//        double offset1 = slider1.getValue()*slider1.getWidth()/slider1.getMax();
//        double offset2 = slider2.getValue()*slider2.getWidth()/slider2.getMax();
//        double offsetSignals = sliderSignals.getValue()*sliderSignals.getWidth()/sliderSignals.getMax();
        
        Marker marker = new Marker(8, slider.getHeight(), offset);
        marker.setFill(Color.RED);
        
        marker.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                log.log(Level.INFO, "marker offset: {0}", marker.getOffset());
                double value = marker.getOffset()*slider.getMax()/slider.getWidth();
                slider.setValue(value);
            }
        });
        
        marker.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.HAND);
            }
        });
        
        marker.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.DEFAULT);
            }
        });
        
        AnchorPane.setBottomAnchor(marker, 5.0);
        AnchorPane.setLeftAnchor(marker, offset);
//        log.log(Level.INFO, "offset: {0}", offset);
        
        anchorPane.getChildren().add(marker);
                
//        currentTime:totalTime=offset:slider
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

                            sliderSignals.increment();
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
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    class ReadData implements Runnable {

        @Override
        public void run() {
            readData();
        }

    }
}
