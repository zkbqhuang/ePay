package com.fenghuaxz.ipay.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.fenghuaxz.ipay.HostAddress;
import com.fenghuaxz.ipay.R;
import com.fenghuaxz.ipay.fragment.HostFragment;
import com.fenghuaxz.ipay.proxy.AppManagerProxy;

public class NewHostDialog extends AlertDialog.Builder {

    public NewHostDialog(@NonNull Context context) {
        super(context);
        setTitle("添加主机");
        setCancelable(false);
        setIcon(R.drawable.ip_icon);
        setNegativeButton("取消", null);

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        setView(view);

        EditText hostEdit = new EditText(context);
        hostEdit.setHint("主机地址(IP:端口)");
        view.addView(hostEdit);

        EditText descEdit = new EditText(context);
        descEdit.setHint("主机备注");
        view.addView(descEdit);

        setPositiveButton("添加", (dialog, which) -> {
            String host = hostEdit.getText().toString();
            String desc = descEdit.getText().toString();
            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(desc)) {
                Toast.makeText(context, "输入项不可为空。", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!host.matches("(?:(?:[0,1]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}(?:[0,1]?\\d?\\d|2[0-4]\\d|25[0-5]):\\d{0,5}")) {
                Toast.makeText(context, "主机地址格式错误。", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] temp = host.split(":");

            try {
                new AppManagerProxy().addHost(new HostAddress(temp[0], Integer.parseInt(temp[1]), desc));
                HostFragment.instance.notifyDataSetChanged();
            } catch (RemoteException e) {
                Toast.makeText(context, "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
