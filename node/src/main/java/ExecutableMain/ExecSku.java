package ExecutableMain;

import com.demo.TaskExecuteReturn;
import com.demo.TaskExecutor;
import com.demo.constans.DicReturnType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ExecSku extends ExecBase implements TaskExecutor {


    /**
     * @return 返回出现异常的记录，null = success  size > 1 = fail，
     */
    private String sku(int begin, int num) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT gsku_id, gspu_id, tgsku_id FROM gc_goods_sku limit " + begin + ", " + num);

        PreparedStatement pstmt = conn.prepareStatement("UPDATE gc_goods_sku SET tgsku_id = ? WHERE gsku_id = ?");

        int index = begin + 1;
        StringBuilder temp = new StringBuilder();
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
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
        }
        return temp.toString();

    }


    @Override
    public TaskExecuteReturn handle(String taskData) {
        try {
            int i = taskData.indexOf(",");
            int begin = Integer.parseInt(taskData.substring(3, i));
            int num = Integer.parseInt(taskData.substring(i + 1));
            String sku = sku(begin, num);
            if (sku.isEmpty()) {
                return new TaskExecuteReturn(DicReturnType.SUCCESS, sku, taskData);
            } else {
                return new TaskExecuteReturn(DicReturnType.FAIL, sku, taskData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new TaskExecuteReturn(DicReturnType.FAIL, e.getMessage(), taskData);
        }
    }

    public Map<Long, Long> initMapOld2New() throws SQLException {
        Map<Long, Long> map = new HashMap<>(524288);
        Statement statement = conn.createStatement();
        //  1：old_gsku_id   2：new_gsku_id
        ResultSet rs = statement.executeQuery("SELECT gsku_id, tgsku_id FROM gc_goods_sku");
        while (rs.next()) {
            map.put(rs.getLong(1), rs.getLong(2));
        }
        return map;
    }

}
