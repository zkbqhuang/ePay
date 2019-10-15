package com.fenghuaxz.ipay.api;

import com.fenghuaxz.rpcframework.annotations.Rpc;

@Rpc
public interface Android {


    String makeQRCode(String appName, String desc, double amount);

    void pay(String appName, String qr, String desc, double amount, String password);
}