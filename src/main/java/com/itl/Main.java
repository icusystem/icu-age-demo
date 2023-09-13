package com.itl;

import io.github.icusystem.icu_connect.Connect;
import io.github.icusystem.icu_connect.LocalAPIListener;
import io.github.icusystem.icu_connect.api_icu.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;


public class Main implements MjpegStream, LocalAPIListener {
    private Connect connect;
    private static final String streamURL = "http://192.168.137.8:8040/stream";
    private static final String icuIPAddress = "192.168.137.8";
    private static final boolean icuSSL = true;
    private static final String icuUserName = "apiuser";
    private static final String icuPassword = "apipassword";

    private int thresholdAge = 25;
    private JLabel labelInfo;
    private JLabel label;
    private JLabel labelResult;
    private JSpinner ageSpinner;
    private ImageIcon imageIcon;
    private MJPEGClient streamClient;







    /**
     *
     */
    private void createAndShowGUI() {


        //Create and set up the window.
        JFrame frame = new JFrame("ICU");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT,20,10));


        labelInfo = new JLabel("Connecting to ICU..",JLabel.LEFT);
        labelInfo.setPreferredSize(new Dimension(600,30));
        frame.getContentPane().add(labelInfo);

        label = new JLabel("Video",JLabel.CENTER);
        label.setPreferredSize(new Dimension(320,240));
        label.setBackground(Color.DARK_GRAY);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);

        frame.getContentPane().add(label);


        labelResult = new JLabel("",JLabel.CENTER);
        labelResult.setOpaque(true);
        labelResult.setForeground(Color.WHITE);
        labelResult.setBackground(Color.WHITE);
        labelResult.setFont(new Font("Arial", Font.PLAIN, 48));
        labelResult.setPreferredSize(new Dimension(320,240));
        frame.getContentPane().add(labelResult);


        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(320,240));
        panel.setBackground(Color.WHITE);
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(thresholdAge,1,100,1);
        ageSpinner = new JSpinner(spinnerNumberModel);
        JLabel splable = new JLabel("Threshold Age: ");
        panel.add(splable);
        panel.add(ageSpinner);


        frame.getContentPane().add(panel);

        frame.setSize(900,600);

        // the image for the stream
        imageIcon = new ImageIcon();
        //Display the window.
        frame.setVisible(true);



        /* Setup and start the ICU*/
        CameraSettings cameraSettings = new CameraSettings();
        cameraSettings.Rotation = 0;
        cameraSettings.Camera_distance = 2;
        cameraSettings.Spoof_level = 0;

        connect = new Connect(cameraSettings,icuIPAddress,icuSSL,icuUserName,icuPassword);
        /* Start the Connect, passing the event listener object */
        connect.Start("Main",this,false);
        /* Set the operation mode - AGE_ONLY in this example */
        connect.SetMode(ICURunMode.AGE_ONLY);

    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.createAndShowGUI();

        });


    }







    /**
     *
     * Stream Listener events
     */
    @Override
    public void NewFrame(BufferedImage image) {

        SwingUtilities.invokeLater(() ->{
            imageIcon.setImage(image);
            label.setIcon(imageIcon);
            label.repaint();
        });

    }

    /**
    * ICU listener events
    */
    @Override
    public void ICUConnected(ICUDevice icuDevice){
        System.out.println("connected " + icuDevice.deviceDetail.DeviceName);

        SwingUtilities.invokeLater(()->{
            labelInfo.setText(icuDevice.deviceDetail.DeviceName + " connected.");
        });

    }

    @Override
    public void ICUDisconnected(String message){
        System.out.println("dis-connected " + message);

        if(streamClient != null){
            streamClient.stopStream();
        }



}
    @Override
    public void ICUReady(){

        /* we do not want to display the result box on the stream */
        connect.SetMode(ICURunMode.STREAM_FACE_BOX_OFF);


        /* Create a new steam client for the ICU camera view */
        streamClient = new MJPEGClient(streamURL,this);

        SwingUtilities.invokeLater(()->{
            labelInfo.setText("");
        });


    }


    @Override
    public void ICUInitialising(){
        System.out.println("initialising...");
        SwingUtilities.invokeLater(()->{
            labelInfo.setText("ICU initialising...");
        });

    }


    @Override
    public void ICUAge(FaceSessionData data){

        SwingUtilities.invokeLater(()->{

            if(data.age < (Integer)ageSpinner.getValue()) {
                labelResult.setBackground(new Color(180,0,0));
                labelResult.setText("FAIL");
            }else{
                labelResult.setBackground(new Color(0,180,0));
                labelResult.setText("PASS");
            }
        });

    }

    @Override
    public void ICULiveFrame(List<FaceDetect> frameCounts){

        if(frameCounts.get(0).FaceCount == 0) {
            // clear result when no faces
            SwingUtilities.invokeLater(() -> {
                labelResult.setText("");
                labelResult.setBackground(Color.WHITE);
            });
        }

    }



}
