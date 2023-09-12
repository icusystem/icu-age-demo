package com.itl;

import io.github.icusystem.icu_connect.LocalAPIListener;
import io.github.icusystem.icu_connect.api_icu.ICUDevice;

public class ApiEvents implements LocalAPIListener {

    @Override
    public void ICUConnected(ICUDevice device) {
        LocalAPIListener.super.ICUConnected(device);

        System.out.println("ICUConnected " + device.deviceDetail.DeviceId);

    }
}
