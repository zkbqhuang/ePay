package com.fenghuaxz.ipay;

import java.io.File;

public class ConstantPool {

    public static final String MY_PACKAGE_NAME = "com.fenghuaxz.ipay";
    public static final File DATA_DIR = new File("/data/data/android/ipay");
    public static final File KEYS_FILE = new File(DATA_DIR, "rsa.json");
    public static final File ADDRESS_FILE = new File(DATA_DIR, "address.obj");
    public static final File QR_CACHE_FILE = new File(DATA_DIR, "qr.obj");

    static {
        DATA_DIR.mkdirs();
    }
}
