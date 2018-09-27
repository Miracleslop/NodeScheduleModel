package com.demo;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MSocket {
    protected Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    /**
     * 毫秒
     */
    private final int defaultWaitTime = 60000;


    public MSocket(Socket socket) throws IOException {
        this.init(socket);
    }

    /**
     * 自动重连服务端
     */
    public MSocket(final String ip, final int port) throws IOException, SocketCreatFailException, InterruptedException {
        this.init(ip, port);
    }

    protected void init(final String ip, final int port) throws IOException, InterruptedException, SocketCreatFailException {
        Socket ts = null;
        int i = 0;
        while (++i > 0) {
            try {
                ts = new Socket(ip, port);
                break;
            } catch (ConnectException ce) {
                System.out.println("connect fail " + i + " time");
            }
            sleep(5000);
        }
        init(ts);
    }

    protected void init(Socket socket) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(defaultWaitTime);
        dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        System.out.println("[socket] " + this.toString() + " connect success");
    }

    public void writeString(String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
    }

    public String readString() throws IOException {
        return dis.readUTF();
    }

    public boolean isAlive() {
        return this.socket != null && !this.socket.isClosed();
    }


    public void close() {
        try {
            dis.close();
            dos.close();
            socket.close();
            System.out.println("[socket] " + this.toString() + " connect close");
        } catch (IOException ignored) {
            System.out.println("[socket] " + this.toString() + " connect unusual close");
        }
    }

    @Override
    public String toString() {
        return this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
    }


}
