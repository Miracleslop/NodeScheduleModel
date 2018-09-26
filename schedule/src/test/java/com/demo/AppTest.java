package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static Logger log = LoggerFactory.getLogger(AppTest.class);

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testSimulateClient() {
        System.out.println("hello  i am test node");
        try {
            MSocket socket = new MSocket("localhost", 8081);
            int i = 0;
            while (true) {
                ++i;
                try {
                    String s = socket.readString();
                    if (s.startsWith(DicReturnType.TASK.str())) {
                        System.out.println("execute tast: " + s);
                        socket.writeString(DicReturnType.SUCCESS.str());
                        if (i > 3) {
                            socket.close();
                            break;
                        }
                    } else if (s.startsWith(DicReturnType.OVER.str())) {
                        System.out.println("task over ");
                        socket.writeString(DicReturnType.OVER.str());
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("wait time out...");
                }
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("sorry i have some exception !");
        }
    }

    /**
     * 测试一个客户端连接
     *
     * @throws IOException
     */
    @Test
    public void testSchedule() throws IOException {
        ScheduleSocket serverSocket = new ScheduleSocket(6, 8081);
        TaskControl taskControl = new TaskControl(2000, 150000);
        try {
            while (true) {
                if (taskControl.hasNext()) {
                    Task task = taskControl.next();
                    String s = serverSocket.allotTask(task);
                    log.debug(s + " accept task: " + task.data());
                } else {
                    serverSocket.close();
                    if (serverSocket.isAllClose()) {
                        break;
                    }
                    String s = serverSocket.allotTask(new Task(DicReturnType.OVER.str()));
                    log.debug(s + " accept task: OVER");
                }
            }
        } catch (Exception e) {
            serverSocket.safeInterrupt();
        }
    }


    @Test
    public void testLogBack() {
        log.debug("test debug");
        log.info("test info");
        log.warn("test warn");
        log.error("test error");
    }
}
