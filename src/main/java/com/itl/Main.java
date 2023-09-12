package com.itl;

import io.github.icusystem.icu_connect.Connect;
import io.github.icusystem.icu_connect.api_icu.CameraSettings;
import io.github.icusystem.icu_connect.api_icu.ICURunMode;

import javax.swing.*;

public class Main {


    public static void main(String[] args) {



        SwingUtilities.invokeLater(() -> {
            String streamURL = "http://192.168.137.8:8040/stream";
             new MJPEGClient(streamURL);
        });

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


    }



}
