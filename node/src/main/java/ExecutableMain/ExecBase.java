package ExecutableMain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * mysql连接基类，提供connection和close方法
 */
class ExecBase {

    Connection conn;

    private static final String database = "192.168.1.22:3306/w5mall";
    private static final String user = "root";
    private static final String password = "W5zg@20180716pre";

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
            conn = DriverManager.getConnection("jdbc:mysql://" + database + "?useUnicode=true&autoReconnect=true&connectTimeout=5000&useSSL=false", user, password);
            conn.setAutoCommit(false);
            System.out.println("[mysql] " + database + " connect success !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    void close() {
        try {
            this.conn.close();
            System.out.println("[mysql] " + database + " connect close !");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[mysql] " + database + " connect unusual close !");
        }
    }


}
