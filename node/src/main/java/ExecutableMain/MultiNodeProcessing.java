package ExecutableMain;

import java.sql.*;

public class MultiNodeProcessing {
    private static Connection conn;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.2.98:3306/w5maltest?useUnicode=true&autoReconnect=true&connectTimeout=5000&useSSL=false", "root", "root");
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    void db(int begin, int end) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT gsku_id, gspu_id, tgsku_id" +
                "FROM gc_goods_sku" +
                "limit " + begin + "," + end);

        PreparedStatement pstmt = conn.prepareStatement("UPDATE gc_goods_sku SET tgsku_id = ? WHERE gsku_id = ?");
        
        int index = begin + 1;
        try {
            while (rs.next()) {
                try {
                    pstmt.setLong(1, index);
                    pstmt.setLong(1, rs.getLong(1));
                    pstmt.addBatch();
                    //
                } catch (Exception e) {
                    //  .....
                }
            }
            int[] ints = pstmt.executeBatch();
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

    }

    public static void main(String[] args) {

    }
}
