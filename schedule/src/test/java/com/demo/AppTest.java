package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

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
    public void testSchedule() throws IOException, InterruptedException {
        ScheduleSocket serverSocket = new ScheduleSocket(5, 8081);
        TaskControl taskControl = new TaskControl(2000, 20000);
        while (true) {
            if (taskControl.hasNext()) {
                Task task = taskControl.next();
                serverSocket.allotTask(task);
            } else {
                serverSocket.respondNode();
                if (serverSocket.isAllAlive()) {
                    break;
                }
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
