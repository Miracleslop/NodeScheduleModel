package com.demo;

public class TaskExecuteReturn {
    private boolean optSuc;

    private String response;

    private String msg;

    public TaskExecuteReturn(boolean optSuc) {
        this.optSuc = optSuc;
    }

    public TaskExecuteReturn(boolean optSuc, String response) {
        this.optSuc = optSuc;
        this.response = response;
    }

    public TaskExecuteReturn(boolean optSuc, String response, String msg) {
        this.optSuc = optSuc;
        this.response = response;
        this.msg = msg;
    }

    public boolean isOptSuc() {
        return optSuc;
    }

    public String getMsg() {
        return msg;
    }

    public String getResponse() {
        return response;
    }
}
