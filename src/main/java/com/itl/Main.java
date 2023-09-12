package com.itl;

import io.github.icusystem.icu_connect.Connect;
import io.github.icusystem.icu_connect.api_icu.CameraSettings;
import io.github.icusystem.icu_connect.api_icu.ICURunMode;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class Main {

    private static JLabel label;

    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("ICU");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        label = new JLabel("");
        frame.getContentPane().add(label);
        frame.setSize(1200,960);

        //Display the window.
        frame.setVisible(true);


    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();

            StreamEvents streamEvents = new StreamEvents(label);
            String streamURL = "http://192.168.137.8:8040/stream";
            MJPEGClient client =   new MJPEGClient(streamURL,streamEvents);


            CameraSettings cameraSettings = new CameraSettings();
            cameraSettings.Rotation = 0;
            cameraSettings.Camera_distance = 2;
            cameraSettings.Spoof_level = 0;

            Connect connect = new Connect(cameraSettings,"192.168.137.8",true,"apiuser","apiuser");
            ApiEvents apiEvents = new ApiEvents();
            /* Start the Connect, passing the event listener object */
            connect.Start("Main",apiEvents,false);
            /* Set the operation mode - AGE_ONLY in this example */
            connect.SetMode(ICURunMode.AGE_ONLY);


        });




    }


}
