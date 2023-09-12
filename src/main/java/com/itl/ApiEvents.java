package com.itl;

import io.github.icusystem.icu_connect.LocalAPIListener;
import io.github.icusystem.icu_connect.api_icu.FaceSessionData;
import io.github.icusystem.icu_connect.api_icu.ICUDevice;

public class ApiEvents implements LocalAPIListener {


    @Override
    public void ICUConnected(ICUDevice icuDevice){

        System.out.println("connected " + icuDevice.deviceDetail.DeviceName);


    }

    @Override
    public void ICUDisconnected(String message){

        System.out.println("dis-connected " + message);


    }


    @Override
    public void ICUInitialising(){

        System.out.println("initialising...");


    }


    @Override
    public void ICUReady(){

        System.out.println("Ready");

    }


    @Override
    public void ICUAge(FaceSessionData data){

        System.out.println("Age  " + data.age);

    }


}