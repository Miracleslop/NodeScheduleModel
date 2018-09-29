package com.demo;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import com.demo.constans.DicReturnType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;


import static com.demo.DBTableSqlSet.*;


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

    private final static int task_per = 2000;

    /**
     * 测试sku任务分配
     *
     * @throws IOException
     */
    @Test
    public void testSkuSchedule() throws IOException, InterruptedException, NoHaveEffectiveNodeException, SQLException {
        //  连接数据库
        MysqlUtil mysqlUtil = new MysqlUtil();
        Statement statement = mysqlUtil.creatStatement();

        //  连接node
        log.info("[ScheduleSocket] sku wait node connect...");
        ScheduleSocket serverSocket = ScheduleSocket.getInstance(socket_num, 8081);

        //  查询total
        ResultSet rs = statement.executeQuery("select count(1) from gc_goods_sku");
        rs.next();
        int total = rs.getInt(1);
        TaskControl taskControl = new TaskControl(task_per, total);
        log.info("[TaskControl]  gc_goods_sku's task allot success: " + taskControl.toString());

        while (taskControl.hasNext()) {
            Task task = taskControl.next();
            String s = serverSocket.allotTask(task);
            log.debug(s + " accept task: " + task.data());
        }
        serverSocket.waitNodeClose(600000);
        log.info("[ScheduleSocket] sku close success...");
    }

    /**
     * 测试其他任务分配
     */
    @Test
    public void testOtherSchedule() throws IOException, InterruptedException, NoHaveEffectiveNodeException, SQLException {
        //  连接数据库
        MysqlUtil mysqlUtil = new MysqlUtil();
        Statement statement = mysqlUtil.creatStatement();
        DBTableSqlSet table[] = {
                gc_goods_spec
                , gc_shopping_cart
                , tc_discount_comm, tc_order_detail, tc_record_goods_rejected, tc_supplier_return_account_detail, uc_message_sc_ag
        };

        //  连接node
        log.info("[ScheduleSocket] other wait node connect...");
        ScheduleSocket serverSocket = ScheduleSocket.getInstance(socket_num, 8081);

        for (DBTableSqlSet sqlSet : table) {
            //  查询并创建任务控制中心
            TaskControl taskControl;
            try {
                ResultSet rs = statement.executeQuery(sqlSet.getCount_sql());
                rs.next();
                int total = rs.getInt(1);
                taskControl = new TaskControl(task_per, total);
            } catch (Exception e) {
                log.error(sqlSet.getCount_sql() + " execute fail and skip ");
                continue;
            }
            //  获取表名
            String tableName = sqlSet.getSelect().substring(sqlSet.getSelect().indexOf("FROM") + 4, sqlSet.getSelect().indexOf("LIMIT") - 1).trim();
            log.info("[TaskControl]  " + tableName + "'s task allot success: " + taskControl.toString());

            //  开始分配任务到node
            while (taskControl.hasNext()) {
                Task task = taskControl.next(tableName);
                String s = serverSocket.allotTask(task);
                log.debug(s + " accept task: " + task.data());
            }
        }
        //  关闭mysql
        mysqlUtil.close();
        //  等待关闭服务
        serverSocket.waitNodeClose(60000);
        log.info("[ScheduleSocket] other close success...");
    }


    @Test
    public void testLogBack() {
        log.debug("test debug");
        log.info("test info");
        log.warn("test warn");
        log.error("test error");
    }
}
