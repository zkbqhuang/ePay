package com.fenghuaxz.ipay.service;

import com.fenghuaxz.ipay.api.OrderManager;
import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.ipay.OrderListener;
import com.fenghuaxz.ipay.annotations.SecurityCode;
import com.fenghuaxz.rpcframework.Service;

import java.util.concurrent.atomic.AtomicReference;

public class OrderManagerImpl extends Service implements OrderManager {

    private final AtomicReference<OrderListener> listener;

    public OrderManagerImpl(AtomicReference<OrderListener> listener) {
        this.listener = listener;
    }

    @Override
    public Receipt newOrder(String appName, String order, String desc, double amount, @SecurityCode String securityCode) {
        OrderListener listener;
        if ((listener = this.listener.get()) != null) {
            return listener.newOrder(appName, order, desc, amount);
        }
        return Receipt.UNPROCESSED;
    }
}
