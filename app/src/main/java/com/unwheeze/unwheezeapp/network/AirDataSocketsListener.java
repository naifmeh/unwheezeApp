package com.unwheeze.unwheezeapp.network;

import com.unwheeze.unwheezeapp.beans.AirData;

/**
 * Created by User on 20/03/2018.
 */

public interface AirDataSocketsListener {
    void onDataReceived(AirData airData);
}
