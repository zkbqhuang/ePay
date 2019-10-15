package com.fenghuaxz.ipay.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.fenghuaxz.ipay.HostAddress;
import com.fenghuaxz.ipay.R;
import com.fenghuaxz.ipay.fragment.HostFragment;
import com.fenghuaxz.ipay.proxy.AppManagerProxy;

public class EditHostDialog extends AlertDialog.Builder {

    @SuppressLint("SetTextI18n")
    public EditHostDialog(@NonNull Context context, HostAddress address, BaseAdapter adapter) {
        super(context);
        setTitle("主机配置");
        setIcon(R.drawable.edit_icon);
        setCancelable(false);
        setNegativeButton("取消", null);

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        setView(view);

        EditText descEdit = new EditText(context);
        descEdit.setHint("主机备注");
        descEdit.setText(address.desc);
        view.addView(descEdit);

        setNeutralButton("删除", (dialog, which) -> {
            try {
                new AppManagerProxy().deleteHost(address);
                adapter.notifyDataSetChanged();
            } catch (RemoteException e) {
                Toast.makeText(context, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        setPositiveButton("修改", (dialog, which) -> {
            String desc = descEdit.getText().toString();
            if (TextUtils.isEmpty(desc)) {
                Toast.makeText(context, "输入项不可为空。", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                HostAddress update = new HostAddress(address.ip, address.port, desc);
                new AppManagerProxy().updateHost(update);
                adapter.notifyDataSetChanged();
            } catch (RemoteException e) {
                Toast.makeText(context, "编辑失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
