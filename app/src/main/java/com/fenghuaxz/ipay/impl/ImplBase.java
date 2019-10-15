package com.fenghuaxz.ipay.impl;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.annotation.*;

public interface ImplBase {

    String name();

    void initHack(XC_LoadPackage.LoadPackageParam param);

    @Inherited
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Process {

        String value();
    }
}
