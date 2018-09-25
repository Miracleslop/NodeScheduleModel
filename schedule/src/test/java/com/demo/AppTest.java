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
    public void testScheduleClient() {
        System.out.println("hello  i am test node");
        try {
            MSocket socket = new MSocket("localhost", 8081);
            while (true) {
                try {
                    String s = socket.readString();
                    if (s.startsWith(DicReturnType.TASK.str())) {
                        System.out.println("execute tast: " + s);
                        socket.writeString(DicReturnType.SUCCESS.str());
                    } else if (s.startsWith(DicReturnType.OVER.str())) {
                        System.out.println("task over ");
                        socket.writeString(DicReturnType.OVER.str());
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("wait time out...");
                }
            }
        } catch (Exception e) {
            System.out.println("sorry i have some exception !");
        }
    }

    @Test
    public void testSchedule() throws IOException, InterruptedException {
        ScheduleSocket serverSocket = new ScheduleSocket(1, 8081);
        TaskControl taskControl = new TaskControl(2000, 20000);
        while (true) {
            if (taskControl.hasNext()) {
                Task task = taskControl.next();
                serverSocket.allotTask(task);
            } else {
            }
            //  每2秒轮询一次
            sleep(2000);
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
