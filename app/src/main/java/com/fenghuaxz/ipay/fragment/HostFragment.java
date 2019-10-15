package com.fenghuaxz.ipay.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.text.HtmlCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.fenghuaxz.ipay.HostAddress;
import com.fenghuaxz.ipay.R;
import com.fenghuaxz.ipay.dialog.EditHostDialog;
import com.fenghuaxz.ipay.proxy.AppManagerProxy;
import org.xml.sax.XMLReader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HostFragment extends Fragment {

    public static final HostFragment instance = new HostFragment();

    private volatile BaseAdapter adapter;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler();

    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ListView view = new ListView(getContext());
        adapter = new MyAdapter(getContext());
        view.setAdapter(adapter);

        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                mHandler.postDelayed(this, 2000);
            }
        };
        mHandler.post(refresh);
        return view;
    }

    static class MyAdapter extends BaseAdapter {

        private final Context mContext;
        List<HostAddress> addressList = new CopyOnWriteArrayList<>();

        MyAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public void notifyDataSetChanged() {
            try {
                HostAddress[] addresses = new AppManagerProxy().browseHosts();
                addressList.clear();
                addressList.addAll(Arrays.asList(addresses));
                super.notifyDataSetChanged();
            } catch (RemoteException ignored) {
            }
        }

        @Override
        public int getCount() {
            return addressList.size();
        }

        @Override
        public Object getItem(int position) {
            return addressList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            @SuppressLint({"ViewHolder", "InflateParams"}) View v = inflater.inflate(R.layout.host_item, null);
            ImageView image = v.findViewById(R.id.state);
            TextView desc = v.findViewById(R.id.desc_text);
            ImageView setting = v.findViewById(R.id.setting);

            HostAddress address = addressList.get(position);
            if (address.isConnected && address.isLocked) {
                image.setImageResource(R.drawable.conn_ok);
            } else if (!address.isConnected) {
                image.setImageResource(R.drawable.conn_failure);
            } else if (!address.isLocked) {
                image.setImageResource(R.drawable.conn_unlock);
            }

            desc.setText(HtmlCompat.fromHtml(address.desc + "<br><size>" + address.ip + ":" + address.port + "</size>"
                    , 0, null, new SizeHandler(14)));
            setting.setOnClickListener(v1 -> new EditHostDialog(mContext, address, this).show());
            return v;
        }

        class SizeHandler implements Html.TagHandler {
            private int size;
            private int startIndex = 0;
            private int stopIndex = 0;

            SizeHandler(int size) {
                this.size = size;
            }

            @Override
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                if (tag.toLowerCase().equals("size")) {
                    if (opening) {
                        startIndex = output.length();
                    } else {
                        stopIndex = output.length();
                        output.setSpan(new AbsoluteSizeSpan(dip2px(size)), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            int dip2px(float dpValue) {
                final float scale = mContext.getResources().getDisplayMetrics().density;
                return (int) (dpValue * scale + 0.5f);
            }
        }
    }
}
