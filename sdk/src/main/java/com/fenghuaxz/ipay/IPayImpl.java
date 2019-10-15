package com.fenghuaxz.ipay;

import com.fenghuaxz.ipay.api.Android;
import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.ipay.service.DeviceManagerImpl;
import com.fenghuaxz.ipay.service.OrderManagerImpl;
import com.fenghuaxz.ipay.service.SecurityManagerImpl;
import com.fenghuaxz.ipay.validator.SecurityValidator;
import com.fenghuaxz.rpcframework.AsyncHandler;
import com.fenghuaxz.rpcframework.RPCServer;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

class IPayImpl implements IPay {

    private final Set<String> appNames = ConcurrentHashMap.newKeySet();
    private final AtomicReference<Channel> channel = new AtomicReference<>();
    private final AtomicReference<Boolean> lowBattery = new AtomicReference<>(false);
    private final AtomicReference<OrderListener> listener = new AtomicReference<>();

    IPayImpl(int port) throws Exception {
        RPCServer server = new RPCServer().bind(new InetSocketAddress(port));
        server.addService(new SecurityManagerImpl());
        server.addService(new DeviceManagerImpl(appNames, channel, lowBattery));
        server.addService(new OrderManagerImpl(listener));
        server.addHook(new SecurityValidator());
    }

    @Override
    public boolean isReady() {
        return this.channel.get() != null && !appNames.isEmpty();
    }

    @Override
    public boolean isLowBattery() {
        return this.lowBattery.get();
    }

    @Override
    public Set<String> appNames() {
        return this.appNames;
    }

    @Override
    public void makeQRCode(String appName, String desc, double amount, AsyncHandler<String> handler) {
        Channel channel;
        if ((channel = this.channel.get()) == null) {
            throw new IllegalStateException("Offline");
        }
        channel.async(Android.class, handler).makeQRCode(appName, desc, amount);
    }

    @Override
    public void pay(String appName, String qr, String desc, double amount, String password, AsyncHandler<Receipt> handler) {
        Channel channel;
        if ((channel = this.channel.get()) == null) {
            throw new IllegalStateException("Offline");
        }
        channel.async(Android.class, handler).pay(appName, qr, desc, amount, password);
    }

    @Override
    public void setListener(OrderListener listener) {
        this.listener.set(listener);
    }

    @Override
    public String toString() {
        Channel channel = this.channel.get();
        return (channel == null ? "offline: " : channel.toString().replaceAll("[\\[\\]]", "") + ": ") + appNames;
    }
}