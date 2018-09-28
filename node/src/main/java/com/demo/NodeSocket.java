package com.demo;

import com.demo.constans.DicReturnType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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
    public String waitTask() throws SocketCreatFailException, InterruptedException, IOException {
        while (true) {
            try {
                return this.readString();
            } catch (SocketTimeoutException ste) {
                System.out.println(Thread.currentThread().getName() + ": " + ste.getMessage());
            } catch (SocketException e) {
                //  支持断后重连
                String ip = this.socket.getInetAddress().getHostAddress();
                int port = this.socket.getPort();
                this.close();
                this.init(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public TaskExecuteReturn executeTask(TaskExecutor taskExecutor, String taskData) {
        return taskExecutor.handle(taskData);
    }

    public void commitTask(DicReturnType returnType) throws IOException {
        this.writeString(returnType.str());
    }

    public void commitTask(String msg) throws IOException {
        this.writeString(msg);
    }

    public TaskExecuteReturn autoExecuteTask(TaskExecutor taskExecutor) throws IOException, SocketCreatFailException, InterruptedException {
        String taskData = waitTask();
        if (taskData == null || taskData.length() < 3 || "null".equals(taskData)) {
            //  任务获取异常
            this.commitTask(DicReturnType.NOTYPE);
            return new TaskExecuteReturn(DicReturnType.NOTYPE);
        }
        if (taskData.startsWith(DicReturnType.OVER.str())) {
            //  结束
            this.commitTask(DicReturnType.OVER);
            return new TaskExecuteReturn(DicReturnType.OVER);
        } else if (taskData.startsWith(DicReturnType.WAIT.str())) {
            //  等待
            this.commitTask(DicReturnType.WAIT);
            return new TaskExecuteReturn(DicReturnType.WAIT);
        } else if (taskData.startsWith(DicReturnType.TEST.str())) {
            //  测试
            this.commitTask(DicReturnType.TEST);
            return new TaskExecuteReturn(DicReturnType.TEST);
        } else if (taskData.startsWith(DicReturnType.TASK.str())) {
            // 接取任务并执行
            TaskExecuteReturn ret = this.executeTask(taskExecutor, taskData);
            this.commitTask(ret.toString());
            return ret;
        } else {
            //  异常任务
            this.commitTask(DicReturnType.NOTYPE);
            return new TaskExecuteReturn(DicReturnType.NOTYPE);
        }
    }

}
