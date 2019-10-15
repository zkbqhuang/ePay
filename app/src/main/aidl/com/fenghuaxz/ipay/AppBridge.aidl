package com.fenghuaxz.ipay;

interface AppBridge {

   String makeQRCode(String desc,double amount);

   void pay(String qr,String desc,double amount,String password);
}
