package com.fenghuaxz.ipay.pojo;

import java.io.Serializable;

public class QRCache implements Serializable {

    public final String qr;
    private final long createTime;

    public QRCache(String qr) {
        this.qr = qr;
        this.createTime = System.currentTimeMillis();
    }

    //过期
    public boolean isExpired() {
        long maxMillis = 60 * 60 * 24 * 1000 * 7;
        return System.currentTimeMillis() - createTime > maxMillis;
    }
}