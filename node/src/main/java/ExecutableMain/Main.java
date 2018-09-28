package ExecutableMain;

import com.demo.NodeSocket;
import com.demo.TaskExecuteReturn;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * 先处理sku 然后 处理其他
 */
public class Main {
    public static void main(String[] args) throws SQLException, InterruptedException {
        //  准备工作
        final int poolMaxSize = 10;
        AtomicInteger count = new AtomicInteger();
        ExecutorService threadPool = Executors.newFixedThreadPool(poolMaxSize);

        //  sku 处理程序
        System.out.println("sku handle");
        Runnable skuRunnable = () -> {
            try {
                NodeSocket nodeSocket = new NodeSocket("192.168.2.63", 8081);
                ExecSku taskExecutor = new ExecSku();
                Mark_1:
                while (true) {
                    TaskExecuteReturn taskExecuteReturn = nodeSocket.autoExecuteTask(taskExecutor);
                    switch (taskExecuteReturn.getReturnType()) {
                        case OVER:
                            System.out.println(nodeSocket.toString() + " task over");
                            break Mark_1;
                        case WAIT:
                            System.out.println("wait 5s");
                            sleep(5000);
                            break;
                        case TEST:
                            System.out.println("test: " + taskExecuteReturn.toString());
                            break;
                        default:
                            System.out.println("complete task: " + taskExecuteReturn.getTaskData());
                    }
                }
                taskExecutor.close();
                nodeSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            count.incrementAndGet();
        };

        for (int i = 0; i < poolMaxSize; i++) {
            threadPool.execute(skuRunnable);
        }
        System.out.println("sku thread pool setup complete ");
        //等待所有任务都结束了再继续执行
        while (count.get() != poolMaxSize) {
            sleep(3000);
        }
        System.out.println("sku game over and wait 60s...");
        sleep(60000);


        //  other 处理程序
        final Map<Long, Long> o2n = new ExecSku().initMapOld2New();
        Runnable otherRunnable = () -> {
            try {
                NodeSocket nodeSocket = new NodeSocket("192.168.2.63", 8081);
                ExecOther taskExecutor = new ExecOther(o2n);
                Mark_1:
                while (true) {
                    TaskExecuteReturn taskExecuteReturn = nodeSocket.autoExecuteTask(taskExecutor);
                    switch (taskExecuteReturn.getReturnType()) {
                        case OVER:
                            System.out.println(nodeSocket.toString() + " task over");
                            break Mark_1;
                        case WAIT:
                            System.out.println("wait 5s");
                            sleep(5000);
                            break;
                        case TEST:
                            System.out.println("test: " + taskExecuteReturn.toString());
                            break;
                        default:
                            System.out.println("complete task: " + taskExecuteReturn.getTaskData());
                    }
                }
                taskExecutor.close();
                nodeSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };


        for (int i = 0; i < poolMaxSize; i++) {
            threadPool.execute(otherRunnable);
        }
        //  关闭线程池，等待任务完成
        threadPool.shutdown();
        System.out.println("other thread pool setup complete ");
        //等待所有任务都结束关闭
        while (true) {
            try {
                if (threadPool.isTerminated()) {
                    System.out.println("other game over ");
                    break;
                }
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
