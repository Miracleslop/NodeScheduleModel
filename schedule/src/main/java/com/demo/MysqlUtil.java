package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlUtil {

    private static Logger log = LoggerFactory.getLogger(MysqlUtil.class);

    private final String database = "192.168.1.22:3306/w5mall";
    private final String user = "root";
    private final String password = "W5zg@20180716pre";

    private Connection conn;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    {
        while (true) {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://" + database + "?useUnicode=true&autoReconnect=true&connectTimeout=5000&useSSL=false", user, password);
                conn.setAutoCommit(false);
                log.info("[mysql]  connection success: " + database);
                break;
            } catch (SQLException e) {
                log.error("[mysql]  connection fail: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Statement creatStatement() throws SQLException {
        return conn.createStatement();
    }

    public void close() throws SQLException {
        this.conn.close();
    }

    public void commit() throws SQLException {
        this.conn.commit();
    }

    public void rollback() throws SQLException {
        this.conn.rollback();
    }


}
