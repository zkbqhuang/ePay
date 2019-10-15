package com.fenghuaxz.ipay.dialog;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.fenghuaxz.ipay.proxy.AppManagerProxy;

public class AliveInfoDialog extends AlertDialog.Builder {

    public AliveInfoDialog(@NonNull Context context) {
        super(context);
        setTitle("存活服务");
        setCancelable(false);
        setNegativeButton("返回", null);

        try {
            String[] appNames = new AppManagerProxy().alive();
            if (appNames.length != 0) {
                StringBuilder builder = new StringBuilder();
                for (String name : appNames) {
                    builder.append(name).append('\n');
                }
                setMessage(builder.toString());
                show();
                return;
            }
            Toast.makeText(context, "没有服务存活。", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(context, "失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
