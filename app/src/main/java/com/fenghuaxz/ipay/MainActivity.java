package com.fenghuaxz.ipay;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.fenghuaxz.ipay.dialog.AboutDialog;
import com.fenghuaxz.ipay.dialog.NewHostDialog;
import com.fenghuaxz.ipay.dialog.AliveInfoDialog;
import com.fenghuaxz.ipay.fragment.HostFragment;
import com.fenghuaxz.ipay.fragment.PaymentFragment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Map<Integer, Fragment> fragmentMap;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        setFragment(item.getItemId());
        return true;
    };

    private void setFragment(int id) {
        Fragment fragment;
        if ((fragment = fragmentMap.get(id)) != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_view, fragment);
            transaction.commit();
        }
    }

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.host_view, HostFragment.instance);
        fragmentMap.put(R.id.payment_view, PaymentFragment.instance);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setFragment(R.id.host_view);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isActive()) {
            new AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setCancelable(false)
                    .setIcon(R.drawable.err_icon)
                    .setMessage("模块尚未激活。")
                    .setNegativeButton("退出", (dialog, which) -> Process.killProcess(Process.myPid()))
                    .show();
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    @SuppressLint("PrivateApi") Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception ignored) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    private volatile long lastBackDown;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long current = System.currentTimeMillis();
            if (lastBackDown != 0 && current - lastBackDown < 2000) {
                Process.killProcess(Process.myPid());
            } else {
                lastBackDown = current;
                Toast.makeText(this, "再按一次退出应用程序。", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.addHost) {
            new NewHostDialog(this).show();
        } else if (id == R.id.alive_info) {
            new AliveInfoDialog(this);
        } else if (id == R.id.about) {
            new AboutDialog(this).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isActive() {
        return false;
    }
}
