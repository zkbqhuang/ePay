package com.fenghuaxz.ipay;

import com.fenghuaxz.ipay.impl.ImplBase;
import com.fenghuaxz.ipay.impl.WechatImpl;
import com.fenghuaxz.ipay.proxy.AppManagerProxy;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HackInit implements IXposedHookLoadPackage {

    private ImplBase[] allImpls() {
        return new ImplBase[]{
                new WechatImpl()
        };
    }

    private String processName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        setActive(param);

        for (ImplBase impl : allImpls()) {
            ImplBase.Process process;
            if ((process = impl.getClass().getAnnotation(ImplBase.Process.class)) != null) {
                if (param.processName.contains(process.value())) {
                    if (process.value().equals(processName())) {
                        impl.initHack(param);
                        new AppManagerProxy().setBridge(impl.name(), (AppBridge) impl);
                    }
                }
            }
        }
    }

    private void setActive(XC_LoadPackage.LoadPackageParam param) {
        String packageName = ConstantPool.MY_PACKAGE_NAME;
        if (packageName.equals(param.packageName)) {
            Class<?> main_activity_class;
            try {
                String main_activity_name = packageName.concat(".MainActivity");
                main_activity_class = param.classLoader.loadClass(main_activity_name);
            } catch (Exception e) {
                return;
            }
            findAndHookMethod(main_activity_class, "isActive", XC_MethodReplacement.returnConstant(true));
        }
    }
}
