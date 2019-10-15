package com.fenghuaxz.ipay.api.pojo;

public class Receipt {

    public static final Receipt SUCCESS = new Receipt(Signal.SUCCESS, "成功。");
    public static final Receipt FAILURE = new Receipt(Signal.FAILED, "失败。");
    public static final Receipt UNPROCESSED = new Receipt(Signal.UNPROCESSED, "未处理。");

    public final Signal signal;
    public final String text;

    public Receipt(Signal signal, String text) {
        this.signal = signal;
        this.text = text;
    }

    @Override
    public String toString() {
        return signal + ": " + text;
    }

    public enum Signal {

        SUCCESS("成功"), FAILED("失败"), UNPROCESSED("未处理");

        public final String name;

        Signal(String name) {
            this.name = name;
        }
    }
}
