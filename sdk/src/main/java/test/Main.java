package test;

import com.fenghuaxz.ipay.api.DeviceManager;
import com.fenghuaxz.ipay.api.SecurityManager;
import com.fenghuaxz.ipay.api.pojo.Receipt;
import com.fenghuaxz.ipay.IPay;
import com.fenghuaxz.ipay.OrderListener;
import com.fenghuaxz.rpcframework.AsyncHandler;
import com.fenghuaxz.rpcframework.RPCClient;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;

import javax.crypto.Cipher;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class Main {

    public static void main(String[] args) throws Exception {

        IPay iPay = IPay.initIPay(8000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("isReady: " + iPay.isReady());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
