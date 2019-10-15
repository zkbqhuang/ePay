package com.fenghuaxz.ipay.api;

import com.fenghuaxz.rpcframework.annotations.Rpc;

@Rpc
public interface DeviceManager {

    void register(String[] appNames, String securityCode);

    void setLowBattery(boolean lowBattery);

    void appUpdate(String appName, boolean isActive);
}
