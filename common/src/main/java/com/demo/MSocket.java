package com.demo;

import java.io.*;
import java.net.Socket;

public class MSocket {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public MSocket(Socket socket) throws IOException {
        this.init(socket);
    }

    public MSocket(final String ip, final int port) throws IOException {
        Socket ts = new Socket(ip, port);
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
        return this.socket.isClosed();
    }


    public void close() {
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
