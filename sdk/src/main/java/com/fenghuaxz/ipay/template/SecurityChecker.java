package com.fenghuaxz.ipay.template;

import com.fenghuaxz.rpcframework.Template;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class SecurityChecker extends Template {

    private volatile RSAPublicKey key;

    public void setKey(RSAPublicKey key) {
        this.key = key;
    }

    public boolean check(String code) {
        if (key != null) {
            try {
                code = code.replaceAll("\\s+", "");
                byte[] data = Base64.getDecoder().decode(code.getBytes(StandardCharsets.UTF_8));
                Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new String(cipher.doFinal(data), StandardCharsets.UTF_8).contains("android");

            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}