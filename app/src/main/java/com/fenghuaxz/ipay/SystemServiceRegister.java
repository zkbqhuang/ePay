package com.fenghuaxz.ipay;

import android.content.Context;
import android.os.Build;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class SystemServiceRegister implements IXposedHookZygoteInit {

    @Override
    public void initZygote(StartupParam startupParam) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Class<?> activityThread = findClass("android.app.ActivityThread", loader);
        hookAllMethods(activityThread, "systemMain", new SystemServiceHook());

    }

    static class SystemServiceHook extends XC_MethodHook {

        private static boolean isReady;
        private volatile Class<?> activity_manager_service_class;

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            if (isReady) return;
            isReady = true;

            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                activity_manager_service_class = findClass("com.android.server.am.ActivityManagerService", loader);
            } catch (Throwable e) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hookAllConstructors(activity_manager_service_class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        final Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        addService(new AppManagerImpl(context));
                    }
                });
                return;
            }

            hookAllMethods(activity_manager_service_class, "main", new XC_MethodHook() {
                @Override
                protected final void afterHookedMethod(final MethodHookParam param) {
                    final Context context = (Context) param.getResult();
                    addService(new AppManagerImpl(context));
                }
            });
        }

        private void addService(AppManagerImpl appManager) {
            Class ServiceManager = XposedHelpers.findClass("android.os.ServiceManager"
                    , Thread.currentThread().getContextClassLoader());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                callStaticMethod(ServiceManager, "addService", "user.ipay.service", appManager, true);
            else {
                callStaticMethod(ServiceManager, "addService", "ipay.service", appManager);
            }

            hookAllMethods(activity_manager_service_class, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    appManager.systemReady();
                }
            });
        }
    }
}

