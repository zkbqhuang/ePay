package com.fenghuaxz.ipay.service;

import com.fenghuaxz.ipay.annotations.SecurityCode;
import com.fenghuaxz.ipay.api.DeviceManager;
import com.fenghuaxz.rpcframework.Service;
import com.fenghuaxz.rpcframework.Template;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceManagerImpl extends Service implements DeviceManager {

    private final Set<String> appNames;
    private final AtomicReference<Channel> channel;
    private final AtomicReference<Boolean> lowBattery;

    @Override
    protected Template[] newTemplates() {
        return new Template[]{new CloseHandler(appNames, channel, lowBattery)};
    }

    public DeviceManagerImpl(Set<String> appNames, AtomicReference<Channel> channel, AtomicReference<Boolean> lowBattery) {
        this.appNames = appNames;
        this.channel = channel;
        this.lowBattery = lowBattery;
    }

    @Override
    public void register(String[] appNames, @SecurityCode String securityCode) {
        if (this.channel.compareAndSet(null, channel())) {
            this.appNames.addAll(Arrays.asList(appNames));
        }
    }

    @Override
    public void appUpdate(String appName, boolean isActive) {
        Channel channel;
        if ((channel = this.channel.get()) != null && channel().equals(channel)) {
            if (isActive) {
                this.appNames.add(appName);
            } else {
                this.appNames.remove(appName);
            }
        }
    }

    @Override
    public void setLowBattery(boolean lowBattery) {
        Channel channel;
        if ((channel = this.channel.get()) != null && channel().equals(channel)) {
            this.lowBattery.set(lowBattery);
        }
    }

    static class CloseHandler extends Template {

        private final Set<String> appNames;
        private final AtomicReference<Channel> channel;
        private final AtomicReference<Boolean> lowBattery;

        CloseHandler(Set<String> appNames, AtomicReference<Channel> channel, AtomicReference<Boolean> lowBattery) {
            this.appNames = appNames;
            this.channel = channel;
            this.lowBattery = lowBattery;
        }

        @Override
        protected void doInactive(Channel channel) {
            if (this.channel.compareAndSet(channel, null)) {
                appNames.clear();
                this.lowBattery.set(false);
            }
        }
    }
}
