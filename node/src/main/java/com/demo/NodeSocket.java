package com.demo;

import com.demo.constans.DicReturnType;

import java.io.*;
import java.net.Socket;

/**
 * 单个处理任务的节点
 */
public class NodeSocket extends MSocket {


    public NodeSocket(Socket socket) throws IOException {
        super(socket);
    }

    public NodeSocket(final String ip, final int port) throws IOException {
        super(ip, port);
    }

    /**
     * 该程序会一直阻塞直到接收到分派任务，或者任务已经全部完成
     *
     * @return 返回任务数据
     */
    public String waitTask() {
        while (true) {
            try {
                return this.readString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean executeTask(TaskExecutor taskExecutor, String taskData) {
        boolean ret = taskExecutor.handle(taskData);
        return ret;
    }

    public void commitTask(DicReturnType returnType) throws IOException {
        this.writeString(returnType.str());
    }

    public boolean autoExecuteTask(TaskExecutor taskExecutor) throws IOException {
        String taskData = waitTask();
        if (taskData == null) {
            return true;
        }
        boolean ret = this.executeTask(taskExecutor, taskData);
        if (ret) {
            this.commitTask(DicReturnType.SUCCESS);
            return true;
        } else {
            this.commitTask(DicReturnType.FAIL);
            return false;
        }
    }
}
