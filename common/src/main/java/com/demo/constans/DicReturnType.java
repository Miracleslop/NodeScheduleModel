package com.demo.constans;

public enum DicReturnType {

    //-----------------------------------------server -> client-----------------------------------------
    /**
     * 代表该请求为分配的任务数据
     */
    TASK("401"),

    //-----------------------------------------client -> server-----------------------------------------

    SUCCESS("402"),
    FAIL("403"),
    /**
     * 异常类型
     */
    NOTYPE("999"),

    //-----------------------------------------client <-> server-----------------------------------------

    /**
     * 代表任务完成，需要关闭连接
     */
    OVER("301"),

    /**
     * 命令节点等待若干时间
     */
    WAIT("501"),
    /**
     * 测试用
     */
    TEST("901");

    String val;

    DicReturnType(String val) {
        this.val = val;
    }

    public String str() {
        return this.val;
    }
}
