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

    public boolean acceptTask(TaskExecutor taskExecutor) {
        try {
            String taskData = this.readString();
            boolean ret = taskExecutor.handle(taskData);
            if (ret) {
                this.commitTask(DicReturnType.SUCCESS);
            } else {
                this.commitTask(DicReturnType.FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void commitTask(DicReturnType returnType) throws IOException {
        this.writeString(returnType.str());
    }
}
