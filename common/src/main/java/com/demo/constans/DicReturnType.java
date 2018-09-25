package com.demo.constans;

public enum DicReturnType {
    SUCCESS("101"),
    FAIL("201"),
    OVER("301"),
    TASK("401");

    String val;

    DicReturnType(String val) {
        this.val = val;
    }

    public String str() {
        return this.val;
    }
}
