package com.fenghuaxz.ipay.dialog;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.fenghuaxz.ipay.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AboutDialog extends AlertDialog.Builder {

    public AboutDialog(@NonNull Context context) {
        super(context);
        setTitle("关于");
        setCancelable(false);
        setPositiveButton("返回", null);
        setMessage("易支付\n邮箱:1107912641@qq.com");

        setNegativeButton("获取SDK", (dialog, which) -> {
            try {
                File sdk = new File(context.getFilesDir(), "ipay-sdk.jar");
                FileOutputStream fos = new FileOutputStream(sdk);
                InputStream in = context.getAssets().open("sdk.jar");
                byte[] data = new byte[1024];
                int len;
                while ((len = in.read(data)) != -1) {
                    fos.write(data, 0, len);
                }
                in.close();
                fos.flush();
                fos.close();

                shareFile(context, Uri.fromFile(sdk));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "分享文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void shareFile(Context context, Uri uri) {
        // File file = new File("\sdcard\android123.cwj"); //附件文件地址

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("subject", ""); //
        intent.putExtra("body", ""); // 正文
        intent.putExtra(Intent.EXTRA_STREAM, uri); // 添加附件，附件为file对象
        if (uri.toString().endsWith(".gz")) {
            intent.setType("application/x-gzip"); // 如果是gz使用gzip的mime
        } else if (uri.toString().endsWith(".txt")) {
            intent.setType("text/plain"); // 纯文本则用text/plain的mime
        } else {
            intent.setType("application/octet-stream"); // 其他的均使用流当做二进制数据来发送
        }
        context.startActivity(intent); // 调用系统的mail客户端进行发送
    }
}
