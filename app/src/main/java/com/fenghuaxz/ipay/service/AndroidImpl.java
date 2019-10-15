package com.fenghuaxz.ipay.service;

import android.os.RemoteException;
import com.fenghuaxz.ipay.AppBridge;
import com.fenghuaxz.ipay.ConstantPool;
import com.fenghuaxz.ipay.api.Android;
import com.fenghuaxz.ipay.pojo.QRCache;
import com.fenghuaxz.ipay.utils.FileUtils;
import com.fenghuaxz.rpcframework.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidImpl extends Service implements Android {

    private final Map<String, AppBridge> appBridges;
    private final Map<String, QRCache> qrCaches;

    public AndroidImpl(Map<String, AppBridge> appBridges) {
        this.appBridges = appBridges;
        this.qrCaches = FileUtils.readFromFile(ConstantPool.QR_CACHE_FILE, new ConcurrentHashMap<>());
    }

    private void clearQRCache() {
        final Collection<QRCache> caches = this.qrCaches.values();
        for (QRCache cache : caches) {
            if (cache.isExpired()) {
                caches.remove(cache);
            }
        }
    }

    @Override
    public String makeQRCode(String appName, String desc, double amount) {
        clearQRCache();

        String formatName = appName.replaceAll("[()|a-z]", "");//去除标识
        final String key = formatName + "#" + desc + "#" + amount;

        QRCache cache;
        if ((cache = qrCaches.get(key)) != null) {
            return cache.qr;
        }

        AppBridge appBridge;
        if ((appBridge = appBridges.get(appName)) == null) {
            throw new IllegalStateException("No appName added: " + appName);
        }

        try {
            String qr_str = appBridge.makeQRCode(desc, amount);
            qrCaches.put(key, new QRCache(qr_str));
            return qr_str;
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void pay(String appName, String qr, String desc, double amount, String password) {
        AppBridge appBridge;
        if ((appBridge = appBridges.get(appName)) == null) {
            throw new IllegalStateException("No appName added: " + appName);
        }
        try {
            appBridge.pay(qr, desc, amount, password);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
