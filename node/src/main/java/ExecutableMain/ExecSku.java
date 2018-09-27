package ExecutableMain;

import com.demo.NodeSocket;
import com.demo.TaskExecuteReturn;
import com.demo.TaskExecutor;
import com.demo.constans.DicReturnType;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class ExecSku implements TaskExecutor {
    private Connection conn;

    private final String database = "192.168.1.22:3306/w5mall_check";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + database + "?useUnicode=true&autoReconnect=true&connectTimeout=5000&useSSL=false", "root", "W5zg@20180716pre");
            conn.setAutoCommit(false);
            System.out.println("[mysql] " + database + " connect success !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    private String sku(int begin, int num) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT gsku_id, gspu_id, tgsku_id FROM gc_goods_sku limit " + begin + ", " + num);

        PreparedStatement pstmt = conn.prepareStatement("UPDATE gc_goods_sku SET tgsku_id = ? WHERE gsku_id = ?");

        int index = begin + 1;
        StringBuilder temp = new StringBuilder();
        boolean optSuc = false;
        try {
            while (rs.next()) {
                if ((index - begin) % 250 == 0) {
                    float progress_bar = ((float) index - (float) begin) / (float) num * 100;
                    System.out.println(String.format("%.2f", progress_bar) + "%");
                }
                try {
                    pstmt.setLong(1, index);
                    pstmt.setLong(2, rs.getLong(1));
                    ++index;
                    int i = pstmt.executeUpdate();
                    if (i != 1 && rs.getObject(3) == null) {
                        temp.append("(").append(rs.getObject(1)).append(",").append(rs.getObject(2)).append(",").append(rs.getObject(3)).append("):").append(i);
                    }
                } catch (Exception e) {
                    temp.append("(").append(rs.getObject(1)).append(",").append(rs.getObject(2)).append(",").append(rs.getObject(3)).append("):").append(e.getMessage());
                }
            }
            conn.commit();
            optSuc = true;
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
        }
        return optSuc ? (DicReturnType.SUCCESS.str() + temp.toString()) : DicReturnType.FAIL.str();

    }

    public void close() {
        try {
            this.conn.close();
            System.out.println("[mysql] " + database + " connect close !");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[mysql] " + database + " connect unusual close !");
        }
    }

    @Override
    public TaskExecuteReturn handle(String taskData) {
        try {
            int i = taskData.indexOf(",");
            int begin = Integer.parseInt(taskData.substring(3, i));
            int num = Integer.parseInt(taskData.substring(i + 1));
            String sku = sku(begin, num);
            if (sku.startsWith(DicReturnType.SUCCESS.str())) {
                return new TaskExecuteReturn(DicReturnType.SUCCESS, sku, taskData);
            } else {
                return new TaskExecuteReturn(DicReturnType.FAIL, sku, taskData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new TaskExecuteReturn(DicReturnType.FAIL, e.getMessage(), taskData);
        }
    }


    public static void main(String[] args) {
        Runnable runnable = () -> {
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
                            System.out.println("execute task: " + taskExecuteReturn.getTaskData());
                    }
                }
                taskExecutor.close();
                nodeSocket.close();
                System.out.println(nodeSocket.toString() + " connect over");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };


        //  7个线程同时处理
        final int node_num = 7;
        ExecutorService threadPool = Executors.newFixedThreadPool(node_num);
        for (int i = 0; i < node_num; i++) {
//            threadPool.execute(runnable);
        }
        threadPool.shutdown();
        System.out.println("thread pool setup complete ");
        //  关闭线程池，等待任务完成
        while (true) {//等待所有任务都结束了继续执行
            try {
                if (threadPool.isTerminated()) {
                    System.out.println("game over ");
                    break;
                }
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
