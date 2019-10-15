package com.fenghuaxz.ipay;

import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.rpcframework.AsyncHandler;

import java.util.Set;

public interface IPay {

    static IPay initIPay(int port) throws Exception {
        return new IPayImpl(port);
    }

    boolean isReady();

    boolean isLowBattery();

    Set<String> appNames();

    void makeQRCode(String appName, String desc, double amount, AsyncHandler<String> handler);

    void pay(String appName, String qr, String desc, double amount, String password, AsyncHandler<Receipt> handler);

    void setListener(OrderListener listener);
}
