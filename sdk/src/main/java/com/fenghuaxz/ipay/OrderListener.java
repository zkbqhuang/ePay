package com.fenghuaxz.ipay;

import com.fenghuaxz.ipay.api.pojo.Receipt;

public interface OrderListener {

    Receipt newOrder(String appName, String order, String desc, double amount);
}
