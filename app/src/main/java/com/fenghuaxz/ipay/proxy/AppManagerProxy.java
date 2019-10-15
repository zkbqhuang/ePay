package com.fenghuaxz.ipay.proxy;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import com.fenghuaxz.ipay.AppBridge;
import com.fenghuaxz.ipay.AppManager;
import com.fenghuaxz.ipay.HostAddress;

import java.lang.reflect.Method;

public class AppManagerProxy implements AppManager {

    private static AppManager subject;

    static {
        initSubject();
    }

    private static void initSubject() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            @SuppressLint("PrivateApi")
            Class<?> service_manager_class = loader.loadClass("android.os.ServiceManager");
            Method method = service_manager_class.getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            String serviceName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? "user.ipay.service" : "ipay.service";
            subject = AppManager.Stub.asInterface((IBinder) method.invoke(null, serviceName));
        } catch (Exception e) {
            throw new RuntimeException("initSubject", e);
        }
    }

    @Override
    public IBinder asBinder() {
        return subject.asBinder();
    }

    @Override
    public void setBridge(String appName, AppBridge bridge) throws RemoteException {
        subject.setBridge(appName, bridge);
    }

    @Override
    public void orderNotify(String appName, String order, String desc, double amount) throws RemoteException {
        subject.orderNotify(appName, order, desc, amount);
    }


    @Override
    public void addHost(HostAddress address) throws RemoteException {
        subject.addHost(address);
    }

    @Override
    public void deleteHost(HostAddress address) throws RemoteException {
        subject.deleteHost(address);
    }

    @Override
    public void updateHost(HostAddress address) throws RemoteException {
        subject.updateHost(address);
    }

    @Override
    public HostAddress[] browseHosts() throws RemoteException {
        return subject.browseHosts();
    }

    @Override
    public String[] alive() throws RemoteException {
        return subject.alive();
    }
}
