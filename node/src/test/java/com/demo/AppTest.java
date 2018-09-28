package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void testMemory() {
        System.gc();
        long start = Runtime.getRuntime().freeMemory();
        Map<Long, Long> o2n = new HashMap<>(262144);
        for (long i = 0; i < 262144; i++) {
            o2n.put(i, 262144L);
        }
        long end = Runtime.getRuntime().freeMemory();
        System.out.println("一个HashMap对象占内存:" + (start - end) / 1000.0);
    }


    @Test
    public void testExecutorService() {
        AtomicInteger count = new AtomicInteger();
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            System.out.println("create thread " + i);
            threadPool.execute(() -> {
                try {
                    sleep(10000);
                    System.out.println("thread " + Thread.currentThread().getName() + "complete");
                    count.incrementAndGet();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        while (true) {
            if (count.get() == 10) {
                System.out.println("threadpool is terminated");
                break;
            }
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("create thread " + i);
            threadPool.execute(() -> {
                try {
                    sleep(10000);
                    System.out.println("thread " + Thread.currentThread().getName() + "complete");
                    count.incrementAndGet();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) ;
        System.out.println(threadPool.isShutdown());
        System.out.println(threadPool.isShutdown());
    }

}
