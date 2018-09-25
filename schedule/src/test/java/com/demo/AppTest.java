package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;

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
}
