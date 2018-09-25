package com.demo;

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
    public void testNode() throws IOException {
        NodeSocket socket = new NodeSocket("localhost", 8081);
        TaskExecutor taskExecutor = (String data) -> {
            return true;
        };
        socket.acceptTask(taskExecutor);
    }
}
