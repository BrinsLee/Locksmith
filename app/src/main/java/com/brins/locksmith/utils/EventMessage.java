package com.brins.locksmith.utils;

public class EventMessage<T> {

    private int code;
    private T data;

    public EventMessage(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static int CODE_UPDATE_PASSWORD = 1;
    public static int CODE_UPDATE_BANK = 2;

    public static int CODE_DELETE_PASSWORD = 3;
    public static int CODE_DELETE_BANK = 4;
}
