package com.fenghuaxz.ipay;

import com.fenghuaxz.ipay.AppBridge;
import com.fenghuaxz.ipay.HostAddress;

interface AppManager {

    void setBridge(String appName,AppBridge bridge);

    void orderNotify(String appName,String order,String desc,double amount);

    void addHost(in HostAddress address);

    void deleteHost(in HostAddress address);

    void updateHost(in HostAddress address);

    HostAddress[] browseHosts();

    String[] alive();
}
