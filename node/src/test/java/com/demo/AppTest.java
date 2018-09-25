package com.demo;

import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testNodeServer() {
        ServerSocket serverSocket = null;
        Socket accept = null;
        MSocket mSocket = null;
        try {
            System.out.println("start create ServerSocket");
            serverSocket = new ServerSocket(8081);
            System.out.println("accept Socket");
            accept = serverSocket.accept();
            mSocket = new MSocket(accept);
            System.out.println("start execute test");
            int i = 0;
            while (true) {
                if (i > 5) {
                    if (i > 7) {
                        break;
                    }
                    mSocket.writeString(DicReturnType.OVER.str());
                    try {
                        String s = mSocket.readString();
                        if (s.startsWith(DicReturnType.OVER.str())) {
                            System.out.println("task over success");
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("task over fail: " + e.getMessage());
                        ++i;
                        continue;
                    }
                }
                mSocket.writeString(DicReturnType.TASK.str() + "test " + i);
                ++i;
                try {
                    String s = mSocket.readString();
                    System.out.println("client response: " + s);
                } catch (Exception e) {
                    System.out.println("sorry i have some Exception: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (accept != null) {
                try {
                    accept.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mSocket != null) {
                mSocket.close();
            }
        }
    }

    @Test
    public void testNode() throws IOException {
//        Runnable runnable = () -> {
//
//        };
//
//        Thread thread = new Thread(runnable);
//        thread.start();

        NodeSocket nodeSocket = new NodeSocket("localhost", 8081);
        while (true) {
            String task = nodeSocket.waitTask();
            if (task.startsWith(DicReturnType.OVER.str())) {
                nodeSocket.commitTask(DicReturnType.OVER);
                break;
            }
            nodeSocket.executeTask((String taskData) -> {
                System.out.println("execute task: " + taskData);
                return true;
            }, task);
            nodeSocket.commitTask(DicReturnType.SUCCESS);
        }

//        thread.interrupt();

    }
}
