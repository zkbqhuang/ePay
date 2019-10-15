package com.fenghuaxz.ipay.service;

import com.fenghuaxz.ipay.api.SecurityManager;
import com.fenghuaxz.ipay.template.SecurityChecker;
import com.fenghuaxz.rpcframework.Service;
import com.fenghuaxz.rpcframework.Template;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class SecurityManagerImpl extends Service implements SecurityManager {

    private volatile RSAPublicKey key;

    @Override
    protected Template[] newTemplates() {
        return new Template[]{new SecurityChecker()};
    }

    @Override
    public boolean isLocked() {
        return key != null;
    }


    @Override//这个安全码不需要安全注解 用以流程验证
    public boolean tryLock(String key, String code) {
        key = key.replaceAll("\\s+", "");
        code = code.replaceAll("\\s+", "");

        if (this.key == null) {
            try {
                byte[] data = Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8));
                this.key = new RSAPublicKeyImpl(data);
                channel().template(SecurityChecker.class).setKey(this.key);
                return true;
            } catch (Exception e) {
                throw new IllegalStateException("tryLock", e);
            }
        } else {
            try {
                System.out.println(channel() + ": " + key);
                byte[] data = Base64.getDecoder().decode(code.getBytes(StandardCharsets.UTF_8));
                Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, this.key);
                return new String(cipher.doFinal(data), StandardCharsets.UTF_8).contains("android");
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("tryLock", e);
            }
        }
    }
}
