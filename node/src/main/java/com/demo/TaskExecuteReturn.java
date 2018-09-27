package com.demo;

import com.demo.constans.DicReturnType;

public class TaskExecuteReturn {

    private String taskData;

    private String exMsg;

    private DicReturnType returnType;

    public TaskExecuteReturn(DicReturnType returnType, String exMsg, String taskData) {
        this.taskData = taskData;
        this.exMsg = exMsg;
        this.returnType = returnType;
    }

    public TaskExecuteReturn(DicReturnType returnType, String exMsg) {
        this.returnType = returnType;
    }

    public TaskExecuteReturn(DicReturnType returnType) {
        this.returnType = returnType;
    }

    /**
     * 返回需要用于网络传输的数据
     */
    @Override
    public String toString() {
        return returnType.str() + (exMsg == null ? "" : exMsg);
    }

    public String getTaskData() {
        return taskData;
    }

    public String getExMsg() {
        return exMsg;
    }

    public DicReturnType getReturnType() {
        return returnType;
    }
}
