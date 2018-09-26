package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public void testSimulateServer() {
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
                    try {
                        mSocket.writeString(DicReturnType.OVER.str());
                        String s = mSocket.readString();
                        if (s.startsWith(DicReturnType.OVER.str())) {
                            System.out.println("task over success");
                            break;
                        }
                    } catch (EOFException eof) {
                        System.out.println("socket is closed");
                        break;
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
    public void testNode() throws IOException, SocketCreatFailException, InterruptedException {
        Runnable runnable = () -> {
            try {
                NodeSocket nodeSocket = new NodeSocket("localhost", 8081);
                TaskExecutor taskExecutor = (String taskData) -> {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new TaskExecuteReturn(true, taskData);
                };
                System.out.println(nodeSocket.toString() + " connect success");
                while (true) {
                    TaskExecuteReturn taskExecuteReturn = nodeSocket.autoExecuteTask(taskExecutor);
                    if (taskExecuteReturn.isOptSuc() && taskExecuteReturn.getResponse().startsWith(DicReturnType.OVER.str())) {
                        System.out.println(nodeSocket.toString() + " task over");
                        break;
                    }
                }
                nodeSocket.close();
                System.out.println(nodeSocket.toString() + " connect over");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        //  6个线程同时处理
        final int node_num = 6;
        ExecutorService threadPool = Executors.newFixedThreadPool(node_num);
        for (int i = 0; i < node_num; i++) {
            threadPool.execute(runnable);
        }
        threadPool.shutdown();
        System.out.println("close thread pool");
        //  关闭线程池，等待任务完成
        while (true) {//等待所有任务都结束了继续执行
            try {
                if (threadPool.isTerminated()) {
                    System.out.println("所有的子线程都结束了！");
                    break;
                }
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
