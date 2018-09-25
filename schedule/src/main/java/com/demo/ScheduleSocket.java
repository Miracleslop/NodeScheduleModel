package com.demo;

import com.demo.constans.DicReturnType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 调度中心
 */
public final class ScheduleSocket {

    private int defaultReadTime = 3000;

    class MSSocket extends MSocket {

        private boolean isFree;

        public MSSocket(Socket socket) throws IOException {
            super(socket);
        }

        public boolean isFree() {
            return isFree;
        }

        public void allotTask(Task task) throws IOException {
            this.writeString(task.data());
            isFree = false;
        }

        public void completeTask() {
            isFree = true;
        }
    }

    private ServerSocket serverSocket;
    private Map<String, MSSocket> socketMap;


    public ScheduleSocket(final int socket_num, final int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socketMap = new HashMap<>();
        while (this.socketMap.size() < socket_num) {
            Socket accept = this.serverSocket.accept();
            String key = this.getKeyFroMSSocket(accept);
            accept.setSoTimeout(defaultReadTime);
            this.socketMap.put(key, new MSSocket(accept));
        }
    }

    /**
     * @param socket 对应的连接
     * @return 返回socket对应的唯一识别key
     */
    private String getKeyFroMSSocket(Socket socket) {
        return socket.getInetAddress().getHostAddress();
    }

    public void over(Socket socket) {
        String key = this.getKeyFroMSSocket(socket);
        MSSocket clientSocket = this.socketMap.get(key);
        try {
            clientSocket.writeString(DicReturnType.OVER.str());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
        this.socketMap.put(key, null);
    }

    public boolean isAlive(Socket socket) {
        String key = this.getKeyFroMSSocket(socket);
        MSSocket clientSocket = this.socketMap.get(key);
        return clientSocket != null && clientSocket.isAlive();
    }

    public boolean isAllAlive() {
        boolean sign = true;
        for (MSSocket clientSocket : socketMap.values()) {
            if (clientSocket == null || !clientSocket.isAlive()) {
                sign = false;
                break;
            }
        }
        return sign;
    }

    /**
     * 分配单个任务到空闲的节点中，并执行
     *
     * @param task 需要执行的任务
     */
    public void allotTask(Task task) throws IOException {
        boolean noExec = true;
        while (noExec) {
            for (MSSocket msk : socketMap.values()) {
                if (msk.isFree()) {
                    try {
                        msk.allotTask(task);
                        noExec = false;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //  如何为false，则代表没有空闲的节点可以调度
            if (noExec) {
                respondNode();
            }
        }
    }

    public void respondNode() {
        for (MSSocket msk : socketMap.values()) {
            try {
                String rep = msk.readString();
                if (rep.endsWith(DicReturnType.SUCCESS.str())) {
                    msk.completeTask();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
