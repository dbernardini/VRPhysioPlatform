
package com.bernardini.vrphysioplatform;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
    private ComboBox<String> comboBox1, comboBox2, comboBox3, comboBox4, 
            comboBox5, comboBox6, comboBox7, comboBox8; 
    
    @FXML
    private LineChart<String, Number> ecgChart, emgChart, forceChart, tempChart,
            edaChart, respChart, eegChart, accChart;
    
    private XYChart.Series<String, Number> seriesX, seriesY, seriesZ, seriesTemp,
            seriesEda, seriesEcg, seriesEmg, seriesForce, seriesResp, seriesEeg;

    private static final String STREAMING_SERVER_URL = "rtmp://127.0.0.1:1936/live";
    private static final int N_BITS = 16;
    private static final double A0 = 1.12764514e-3;
    private static final double A1 = 2.34282709e-4;
    private static final double A2 = 8.77303013e-8;
    private static final int VCC = 3;
    private static final double KELVIN_CELSIUS = 273.15;
    private static final double C_MIN = 27400;
    private static final double C_MAX = 39100;
    private static final double EMG_ECG_GAIN = 1000;
    private static final double EEG_GAIN = 40000;
    private static final double EDA_CONST = 0.12;
    private static final double POW_2_N = Math.pow(2, N_BITS);
    private static final ObservableList<String> SENSORS = FXCollections.observableArrayList(
            "ACC X",
            "ACC Y",
            "ACC Z",
            "ECG",
            "EDA",
            "EEG",
            "EMG",
            "Force",
            "Resp",
            "Temp"
        );
    
    
    Logger log = Logger.getLogger(OfflineFXMLController.class.getName());
    private boolean play = false;
    private boolean stop = false;
    private int signalsCounter = 10;
    private Stage stage;
    private String deviceMAC;
    private Device dev;
    private Device.Frame[] frames;
    private OpenCVFrameGrabber grabber;
    private Thread cameraThread;
    private Thread sensorsThread;
    private Timeline updateGraphs;
    
    private int accXPos;
    private int accYPos;
    private int accZPos;
    private int ecgPos;
    private int edaPos;
    private int eegPos;
    private int emgPos;
    private int forcePos;
    private int respPos;
    private int tempPos;
    
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
            seriesEcg.getData().add(new XYChart.Data<>(i + "", 0));
            seriesEda.getData().add(new XYChart.Data<>(i + "", 0));
            seriesEeg.getData().add(new XYChart.Data<>(i + "", 0));
            seriesEmg.getData().add(new XYChart.Data<>(i + "", 0));
            seriesForce.getData().add(new XYChart.Data<>(i + "", 0));
            seriesResp.getData().add(new XYChart.Data<>(i + "", 0));
        }
        
//        accChart.getData().add(seriesX);
//        accChart.getData().add(seriesY);
//        accChart.getData().add(seriesZ);
//        tempChart.getData().add(seriesTemp);
//        ecgChart.getData().add(seriesEcg);
//        emgChart.getData().add(seriesEmg);
//        eegChart.getData().add(seriesEeg);
//        edaChart.getData().add(seriesEda);
//        forceChart.getData().add(seriesForce);
//        respChart.getData().add(seriesResp);
           
        
        comboBox1.setItems(SENSORS);
        comboBox2.setItems(SENSORS);
        comboBox3.setItems(SENSORS);
        comboBox4.setItems(SENSORS);
        comboBox5.setItems(SENSORS);
        comboBox6.setItems(SENSORS);
        comboBox7.setItems(SENSORS);
        comboBox8.setItems(SENSORS);
        
        tempChart.setCreateSymbols(false);
        ecgChart.setCreateSymbols(false);
        emgChart.setCreateSymbols(false);
        forceChart.setCreateSymbols(false);            
        edaChart.setCreateSymbols(false);
        respChart.setCreateSymbols(false);
        eegChart.setCreateSymbols(false);
        accChart.setCreateSymbols(false);
   
        comboBox1.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 0;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 0;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 0;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 0;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 0;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 0;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 0;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 0;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 0;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 0;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox2.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 1;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 1;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 1;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 1;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 1;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 1;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 1;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 1;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 1;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 1;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox3.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 2;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 2;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 2;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 2;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 2;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 2;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 2;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 2;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 2;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 2;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox4.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 3;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 3;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 3;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 3;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 3;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 3;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 3;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 3;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 3;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 3;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox5.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 4;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 4;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 4;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 4;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 4;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 4;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 4;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 4;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 4;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 4;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox6.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 5;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 5;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 5;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 5;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 5;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 5;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 5;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 5;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 5;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 5;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox7.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 6;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 6;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 6;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 6;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 6;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 6;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 6;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 6;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 6;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 6;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        comboBox8.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "ACC X":
                        accChart.getData().add(seriesX);
                        accXPos = 7;
                        break;
                    case "ACC Y":
                        accChart.getData().add(seriesY);
                        accYPos = 7;
                        break;
                    case "ACC Z":
                        accChart.getData().add(seriesZ);
                        accZPos = 7;
                        break;
                    case "ECG":
                        ecgChart.getData().add(seriesEcg);
                        ecgPos = 7;
                        break;
                    case "EDA":
                        edaChart.getData().add(seriesEda);
                        edaPos = 7;
                        break;
                    case "EEG":
                        eegChart.getData().add(seriesEeg);
                        eegPos = 7;
                        break;
                    case "EMG":
                        emgChart.getData().add(seriesEmg);
                        emgPos = 7;
                        break;
                    case "Force":
                        forceChart.getData().add(seriesForce);
                        forcePos = 7;
                        break;
                    case "Resp":
                        respChart.getData().add(seriesResp);
                        respPos = 7;
                        break;
                    case "Temp":
                        tempChart.getData().add(seriesTemp);
                        tempPos = 7;
                        break; 
                    default:
                        System.err.println("Error with the ComboBox");
                }
            }
        });
        
        try {
            Vector devs = new Vector();
            Device.FindDevices(devs);
            
            deviceMAC = (String) devs.get(0);

           
            dev = new Device(deviceMAC); // MAC address of physical device

            // initialize frames vector and each frame within the vector
            frames = new Device.Frame[1000];
            for (int i = 0; i < 1000; i++)
               frames[i] = new Device.Frame();

            dev.BeginAcq(1000, 0xFF, N_BITS); // 100 Hz, 8 channels, 16 bits

        }
        catch (BPException err){
            System.out.println("Exception: " + err + " - " + err.getMessage());
        }    
        
        sensorsThread = new Thread(() -> {
            while(true){
                try {
                    dev.GetFrames(100, frames);
                    System.out.println("getFrames");
                    System.out.println("frames[0]: " + frames[0].an_in[1]);
                    Thread.sleep(100);
                } catch (BPException ex) {
                    Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RealtimeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        sensorsThread.setDaemon(true);
        sensorsThread.start();
        
        
        updateGraphs = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (seriesTemp.getData().size() >= 10){
                        seriesX.getData().remove(0);
                        seriesY.getData().remove(0);
                        seriesZ.getData().remove(0);
                        seriesTemp.getData().remove(0);
                        seriesEcg.getData().remove(0);
                        seriesEda.getData().remove(0);
                        seriesEeg.getData().remove(0);
                        seriesEmg.getData().remove(0);
                        seriesResp.getData().remove(0);
                        seriesForce.getData().remove(0);
                    }
                    
                    Device.Frame data = frames[0];
                    
                    double temp = round(transTemp(data.an_in[tempPos]),1);

                    seriesX.getData().add(new XYChart.Data<>(signalsCounter + "", transAcc(data.an_in[accXPos])));
                    seriesEeg.getData().add(new XYChart.Data<>(signalsCounter + "", transEeg(data.an_in[eegPos])));
                    seriesY.getData().add(new XYChart.Data<>(signalsCounter + "", transAcc(data.an_in[accYPos])));
                    seriesEcg.getData().add(new XYChart.Data<>(signalsCounter + "", transEcg(data.an_in[ecgPos])));
                    seriesZ.getData().add(new XYChart.Data<>(signalsCounter + "", transAcc(data.an_in[accZPos])));
                    seriesTemp.getData().add(new XYChart.Data<>(signalsCounter + "", temp));
                    seriesResp.getData().add(new XYChart.Data<>(signalsCounter + "", transResp(data.an_in[respPos])));
                    seriesForce.getData().add(new XYChart.Data<>(signalsCounter + "", transForce(data.an_in[forcePos])));
                    seriesEmg.getData().add(new XYChart.Data<>(signalsCounter + "", transEmg(data.an_in[emgPos])));
                    seriesEda.getData().add(new XYChart.Data<>(signalsCounter + "", transEda(data.an_in[edaPos])));
                            
                    signalsCounter++;
                        
                } catch (Exception ex) {
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
        
        cameraThread = new Thread(() -> {
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

        cameraThread.setDaemon(true);
        cameraThread.start();

    }
    
    @FXML
    private void startRealtime(ActionEvent event) {
        
    }
    
    private double transTemp(int rawData){
        double ntcV = rawData*VCC/POW_2_N;
        double ntcO = 10000*ntcV/(VCC-ntcV);
        double logO = Math.log(ntcO);
        double tempK = 1/(A0+(A1*logO)+(A2*Math.pow(logO,3)));
        double tempC = tempK-KELVIN_CELSIUS;      
        return tempC;
    }
    
    private double transAcc(int rawData){
        double acc = (2*(rawData-C_MIN)/(C_MAX-C_MIN))-1;
        return acc;
    }
    
    private double transResp(int rawData){
        double perc = ((rawData/POW_2_N)-0.5)*100;
        return perc;
    }
    
    private double transEmg(int rawData){
        double emgV = (((rawData/POW_2_N)-0.5)*VCC)/EMG_ECG_GAIN;
        double emgMV = emgV*1000;
        return emgMV;
    }
    
    private double transEda(int rawData){
        double edaMS = (rawData/POW_2_N*VCC)/EDA_CONST;
        return edaMS;
    }
    
    private double transEcg(int rawData){
        double ecgV = (((rawData/POW_2_N)-0.5)*VCC)/EMG_ECG_GAIN;
        double ecgMV = ecgV*1000;
        return ecgMV;
    }
    
    private double transEeg(int rawData){
        double eegV = (((rawData/POW_2_N)-0.5)*VCC)/EEG_GAIN;
        double eegMV = eegV*1000000;
        return eegMV;
    }
    
    private double transForce(int rawData){
        double force = rawData*100/POW_2_N;
        return force;
    }
    
    private static double round(double value, int places) {
        if (places < 0) 
            throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    protected void stopAcquisitions(){
        try {
            dev.EndAcq();
            dev.Close();
            grabber.stop();
            grabber.close();
        } catch (FrameGrabber.Exception | BPException ex) {
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
