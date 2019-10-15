package com.fenghuaxz.ipay;

import android.content.*;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Base64;
import com.fenghuaxz.ipay.AppBridge;
import com.fenghuaxz.ipay.AppManager;
import com.fenghuaxz.ipay.ConstantPool;
import com.fenghuaxz.ipay.HostAddress;
import com.fenghuaxz.ipay.api.DeviceManager;
import com.fenghuaxz.ipay.api.OrderManager;
import com.fenghuaxz.ipay.api.SecurityManager;
import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.ipay.service.AndroidImpl;
import com.fenghuaxz.ipay.utils.FileUtils;
import com.fenghuaxz.rpcframework.AsyncHandler;
import com.fenghuaxz.rpcframework.RPCClient;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.robv.android.xposed.XposedBridge;

import javax.crypto.Cipher;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppManagerImpl extends AppManager.Stub {

    private final BroadcastReceiver BATTERY_RECEIVER = new BroadcastReceiver() {
        volatile int lastLevel;

        @Override
        public void onReceive(Context context, Intent intent) {
            final int lowBattery = 15;//15%为低电量
            int level = intent.getIntExtra("level", 0);
            if (lastLevel != level) {
                lastLevel = level;
                onBatteryChanged(level <= lowBattery);
            }
        }
    };

    private final BroadcastReceiver UNINSTALL_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getDataString();
            if (ConstantPool.MY_PACKAGE_NAME.equals(packageName)) {
                clearData(ConstantPool.DATA_DIR);
            }
        }
    };

    private String publicKey;
    private PrivateKey privateKey;
    private final Context mContext;
    private final Map<String, HostAddress> addresses;
    private final Map<String, AppBridge> appBridges = new ConcurrentHashMap<>();
    private final AndroidImpl android = new AndroidImpl(appBridges);
    private final Map<String, RPCClient> channels = new ConcurrentHashMap<>();
    private final Handler mHandler = new Handler();

    public AppManagerImpl(Context context) {
        this.mContext = context;
        this.addresses = FileUtils.readFromFile(ConstantPool.ADDRESS_FILE, new ConcurrentHashMap<>());
        initKeys();
    }

    private void initKeys() {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {

            JsonObject rsaJson = new JsonObject();
            File rsa = ConstantPool.KEYS_FILE;
            if (rsa.exists()) {
                rsaJson = gson.fromJson(new FileReader(rsa), JsonObject.class);
            }

            if (!rsaJson.has("privateKey") || !rsaJson.has("publicKey")) {
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
                keyPairGen.initialize(1024, new SecureRandom());
                KeyPair keyPair = keyPairGen.generateKeyPair();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(privateKey);
                oos.flush();
                byte[] privateKeyBytes = bos.toByteArray();
                oos.close();
                rsaJson.addProperty("publicKey", Base64.encodeToString(publicKey.getEncoded(), 0));
                rsaJson.addProperty("privateKey", Base64.encodeToString(privateKeyBytes, 0));
                FileWriter writer = new FileWriter(rsa);
                writer.write(gson.toJson(rsaJson));
                writer.flush();
                writer.close();
            }

            this.publicKey = rsaJson.get("publicKey").getAsString();
            String privateKeyStr = rsaJson.get("privateKey").getAsString();

            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(Base64.decode(privateKeyStr, 0)));
            this.privateKey = (PrivateKey) ois.readObject();
            ois.close();
        } catch (Exception e) {
            throw new RuntimeException("initKeys", e);
        }
    }

    public void systemReady() {

        //注册电量监听
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(BATTERY_RECEIVER, filter);

        //注册卸载监听
        filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(UNINSTALL_RECEIVER, filter);

        //初始化连接
        for (HostAddress address : this.addresses.values()) {
            this.channels.put(address.toString(), wrapClient(address));
        }

        Runnable infiniteLoop = new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, RPCClient> entry : channels.entrySet()) {
                    final String name = entry.getKey();
                    final RPCClient client = entry.getValue();

                    HostAddress address;
                    if ((address = addresses.get(name)) != null) {
                        if (!client.isConnected()) {
                            address.isConnected = false;
                            address.isLocked = false;
                            new Thread(() -> {
                                try {
                                    client.connect();
                                    address.isConnected = true;
                                    client.async(SecurityManager.class, (AsyncHandler<Boolean>) future -> {
                                        if (future.isSuccess()) {
                                            boolean isLocked = future.getNow();
                                            if (isLocked) {
                                                address.isLocked = true;
                                                String[] appNames = appBridges.keySet().toArray(new String[0]);
                                                client.oneway(DeviceManager.class).register(appNames, securityCode());
                                            }
                                        }
                                    }).tryLock(publicKey, securityCode());
                                } catch (Exception ignored) {
                                }
                            }).start();
                        }
                    }
                }
                mHandler.postDelayed(this, 5000);
            }
        };
        mHandler.post(infiniteLoop);
    }

    private String securityCode() {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] data = "android".getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(cipher.doFinal(data), 0);
        } catch (Exception e) {
            return null;
        }
    }

    private RPCClient wrapClient(HostAddress address) {
        RPCClient client = new RPCClient();
        client.setAddress(new InetSocketAddress(address.ip, address.port));
        client.addService(android);
        return client;
    }

    private void saveAddress() {
        try {
            File file = ConstantPool.ADDRESS_FILE;
            FileUtils.saveToFile(file, this.addresses);
        } catch (Exception e) {
            throw new RuntimeException("saveAddress", e);
        }
    }

    private void clearData(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                clearData(dir);
            else {
                file.delete();
            }
        }
        dir.delete();
    }

    private void onBatteryChanged(boolean isLowBattery) {
        for (RPCClient client : this.channels.values()) {
            if (client.isConnected()) {
                client.oneway(DeviceManager.class).setLowBattery(isLowBattery);
            }
        }
    }

    private void appUpdate(String appName, boolean isActive) {
        for (RPCClient client : this.channels.values()) {
            if (client.isConnected()) {
                client.oneway(DeviceManager.class).appUpdate(appName, isActive);
            }
        }
    }

    @Override
    public void setBridge(String appName, AppBridge bridge) throws RemoteException {
        char[] tags = "abcdef".toCharArray();

        for (char tag : tags) {
            String tempName = appName + "(" + tag + ")";
            if (!appBridges.containsKey(tempName)) {
                appName = tempName;
                break;
            }
        }

        this.appBridges.put(appName, bridge);
        appUpdate(appName, true);

        bridge.asBinder().linkToDeath(() -> {
            Iterator<Map.Entry<String, AppBridge>> it = appBridges.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AppBridge> e = it.next();
                if (bridge.equals(e.getValue())) {
                    appUpdate(e.getKey(), false);
                    it.remove();
                    return;
                }
            }
        }, 0);
    }

    @Override
    public void orderNotify(String appName, String order, String desc, double amount) throws RemoteException {
        final String securityCode = securityCode();
        for (RPCClient client : this.channels.values()) {
            if (client.isConnected()) {
                client.async(OrderManager.class, (AsyncHandler<Receipt>) future -> {
                    if (future.isSuccess()) {
                        //成功
                    }
                    //失败
                }).newOrder(appName, order, desc, amount, securityCode);
            }
        }
    }

    @Override
    public void addHost(HostAddress address) throws RemoteException {
        if (!this.addresses.containsKey(address.toString())) {
            this.addresses.put(address.toString(), address);
            this.channels.put(address.toString(), wrapClient(address));
            saveAddress();
        }
    }

    @Override
    public void deleteHost(HostAddress address) throws RemoteException {
        boolean isSuccess = this.addresses.remove(address.toString()) != null;
        if (isSuccess) {
            RPCClient client;
            if ((client = this.channels.remove(address.toString())) != null) {
                client.dispose();
            }
            saveAddress();
        }
    }

    @Override
    public void updateHost(HostAddress address) throws RemoteException {
        HostAddress now;
        if ((now = this.addresses.get(address.toString())) != null) {
            address.isConnected = now.isConnected;
            address.isLocked = now.isLocked;
            this.addresses.put(address.toString(), address);
            saveAddress();
        }
    }

    @Override
    public HostAddress[] browseHosts() throws RemoteException {
        return this.addresses.values().toArray(new HostAddress[0]);
    }

    @Override
    public String[] alive() throws RemoteException {
        return appBridges.keySet().toArray(new String[0]);
    }
}