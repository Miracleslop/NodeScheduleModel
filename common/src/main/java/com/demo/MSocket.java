package com.demo;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MSocket {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public MSocket(Socket socket) throws IOException {
        this.init(socket);
    }

    /**
     * 自动重连服务端
     *
     * @param ip
     * @param port
     * @throws IOException
     */
    public MSocket(final String ip, final int port) throws IOException, SocketCreatFailException, InterruptedException {
        int i = 0;
        Socket ts = null;
        while (true) {
            ++i;
            try {
                ts = new Socket(ip, port);
                break;
            } catch (ConnectException ce) {
                System.out.println("connect fail " + i + " time");
            }
            if (i > 999) {
                throw new SocketCreatFailException();
            }
            sleep(2000);
        }
        this.init(ts);
    }

    private void init(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public String toString() {
        return this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
    }
}
