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

    public NodeSocket(final String ip, final int port) throws IOException, SocketCreatFailException, InterruptedException {
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
            } catch (Exception ignored) {
            }
        }
    }

    public TaskExecuteReturn executeTask(TaskExecutor taskExecutor, String taskData) {
        return taskExecutor.handle(taskData);
    }

    public void commitTask(DicReturnType returnType) throws IOException {
        this.writeString(returnType.str());
    }

    public TaskExecuteReturn autoExecuteTask(TaskExecutor taskExecutor) throws IOException {
        String taskData = waitTask();
        if (taskData == null) {
            return new TaskExecuteReturn(false, null);
        }
        System.out.println(this.toString() + " execute task: " + taskData);
        if (taskData.startsWith(DicReturnType.OVER.str())) {
            return new TaskExecuteReturn(true, DicReturnType.OVER.str());
        }
        TaskExecuteReturn ret = this.executeTask(taskExecutor, taskData);
        if (ret.isOptSuc()) {
            this.commitTask(DicReturnType.SUCCESS);
        } else {
            this.commitTask(DicReturnType.FAIL);
        }
        return ret;
    }

}
