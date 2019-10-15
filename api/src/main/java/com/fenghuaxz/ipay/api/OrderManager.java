package com.fenghuaxz.ipay.api;

import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.rpcframework.annotations.Rpc;

@Rpc
public interface OrderManager {

    Receipt newOrder(String appName, String order, String desc, double amount, String securityCode);
}
