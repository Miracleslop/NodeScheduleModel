package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


import static com.demo.DBTableSqlSet.*;


/**
 * Unit test for simple App.
 */
public class AppTest {

    private static final String database = "192.168.1.22:3306/w5mall";
    private static final String user = "root";
    private static final String password = "W5zg@20180716pre";

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
            MSocket socket = new MSocket("192.168.2.63", 8081);
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

    private final static int socket_num = 300;
    /**
     * 测试sku任务分配
     *
     * @throws IOException
     */
    @Test
    public void testSkuSchedule() throws IOException, InterruptedException, NoHaveEffectiveNodeException {
        log.info("[ScheduleSocket] sku wait node connect...");
        ScheduleSocket serverSocket = ScheduleSocket.getInstance(socket_num, 8081);
        TaskControl taskControl = new TaskControl(2000, 265004);
        while (taskControl.hasNext()) {
            Task task = taskControl.next();
            String s = serverSocket.allotTask(task);
//                    String s = serverSocket.allotTask(new Task(DicReturnType.WAIT.str()));
            log.debug(s + " accept task: " + task.data());
        }
        serverSocket.waitNodeClose(60000);
//        serverSocket.close();
        log.info("[ScheduleSocket] sku close success...");
    }

    /**
     * 测试其他任务分配
     */
    @Test
    public void testOtherSchedule() throws IOException, InterruptedException, NoHaveEffectiveNodeException {
        //  连接数据库
        Statement statement;
        Connection conn;
        while (true) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + database + "?useUnicode=true&autoReconnect=true&connectTimeout=5000&useSSL=false", user, password);
                statement = conn.createStatement();
                break;
            } catch (Exception e) {
                log.error("[mysql]  connection fail: " + e.getMessage());
                sleep(3000);
            }
        }
        log.info("[ScheduleSocket] other wait node connect...");
        ScheduleSocket serverSocket = ScheduleSocket.getInstance(socket_num, 8081);
        DBTableSqlSet table[] = {
                gc_goods_spec
                , gc_shopping_cart
                , tc_discount_comm, tc_order_detail, tc_record_goods_rejected, tc_supplier_return_account_detail, uc_message_sc_ag
        };
        //  执行任务
        for (DBTableSqlSet sqlSet : table) {
            TaskControl taskControl;
            try {
                ResultSet rs = statement.executeQuery(sqlSet.getCount_sql());
                rs.next();
                int total = rs.getInt(1);
                taskControl = new TaskControl(2000, total);
            } catch (Exception e) {
                log.error(sqlSet.getCount_sql() + " execute fail and skip ");
                continue;
            }
            String tableName = sqlSet.getSelect().substring(sqlSet.getSelect().indexOf("FROM") + 4, sqlSet.getSelect().indexOf("LIMIT") - 1).trim();
            while (taskControl.hasNext()) {
                Task task = taskControl.next(tableName);
                String s = serverSocket.allotTask(task);
//                    String s = serverSocket.allotTask(new Task(DicReturnType.WAIT.str()));
                log.debug(s + " accept task: " + task.data());
            }
        }
        //  等待关闭服务
        serverSocket.waitNodeClose(60000);
//        serverSocket.close();
        log.info("[ScheduleSocket] sku close success...");
    }


    @Test
    public void testLogBack() {
        log.debug("test debug");
        log.info("test info");
        log.warn("test warn");
        log.error("test error");
    }
}
