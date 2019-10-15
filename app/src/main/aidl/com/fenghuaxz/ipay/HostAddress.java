package com.fenghuaxz.ipay;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class HostAddress implements Serializable, Parcelable {

    public final String ip;
    public final int port;
    public final String desc;
    public transient volatile boolean isConnected;
    public transient volatile boolean isLocked;

    public HostAddress(String ip, int port, String desc) {
        this.ip = ip;
        this.port = port;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.ip + ":" + this.port;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ip);
        dest.writeInt(this.port);
        dest.writeString(this.desc);
        dest.writeByte((byte) (isConnected ? 1 : 0));
        dest.writeByte((byte) (isLocked ? 1 : 0));
    }

    public static final Creator<HostAddress> CREATOR = new Creator<HostAddress>() {
        @Override
        public HostAddress createFromParcel(Parcel in) {
            HostAddress address = new HostAddress(in.readString(), in.readInt(), in.readString());
            address.isConnected = in.readByte() == 1;
            address.isLocked = in.readByte() == 1;
            return address;
        }

        @Override
        public HostAddress[] newArray(int size) {
            return new HostAddress[size];
        }
    };
}
