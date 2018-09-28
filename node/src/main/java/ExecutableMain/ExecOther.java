package ExecutableMain;

import com.demo.DBTableSqlSet;
import com.demo.TaskExecuteReturn;
import com.demo.TaskExecutor;
import com.demo.constans.DicReturnType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.demo.DBTableSqlSet.*;

public class ExecOther extends ExecBase implements TaskExecutor {

    private Map<Long, Long> o2n;

    public ExecOther(Map<Long, Long> o2n) {
        this.o2n = o2n;
    }

    /**
     * @return 返回出现异常的记录，null = success  size > 1 = fail，
     */
    private String other(final int begin, final int num, DBTableSqlSet dbTableSqlSet) throws SQLException {
        StringBuilder msg = new StringBuilder();

        Statement stmt = conn.createStatement();
        //  默认 1：主键 2：gsku_id
        ResultSet rs = stmt.executeQuery(String.format(dbTableSqlSet.getSelect(), begin, num));
        //  1：gsku_id   2：主键
        PreparedStatement pstmt = conn.prepareStatement(dbTableSqlSet.getUpdate());
        int index = begin + 1;
        try {
            while (rs.next()) {
                if ((index - begin) % 250 == 0) {
                    float progress_bar = ((float) index - (float) begin) / (float) num * 100;
                    System.out.println(String.format("%.2f", progress_bar) + "%");
                }
                try {
                    ++index;
                    pstmt.setLong(1, o2n.get(rs.getLong(2)));
                    pstmt.setLong(2, rs.getLong(1));
                    int i = pstmt.executeUpdate();
                    if (i != 1) {
                        msg.append("(").append(rs.getLong(1)).append(",").append(rs.getLong(2)).append("):").append(i);
                    }
                } catch (Exception e) {
                    msg.append("(").append(rs.getLong(1)).append(",").append(rs.getLong(2)).append("):").append(e.getMessage());
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
        }
        return msg.toString();
    }

    @Override
    public TaskExecuteReturn handle(String taskData) {
        try {
            int i = taskData.indexOf(",");
            int begin = Integer.parseInt(taskData.substring(3, i));
            int i2 = taskData.indexOf(",", i + 1);
            int num = Integer.parseInt(taskData.substring(i + 1, i2));
            String ss_str = taskData.substring(i2 + 1);
            DBTableSqlSet dbTableSqlSet;
            switch (ss_str) {
                case "gc_goods_spec":
                    dbTableSqlSet = gc_goods_spec;
                    break;
                case "gc_shopping_cart":
                    dbTableSqlSet = gc_shopping_cart;
                    break;
                case "tc_discount_comm":
                    dbTableSqlSet = tc_discount_comm;
                    break;
                case "tc_order_detail":
                    dbTableSqlSet = tc_order_detail;
                    break;
                case "tc_record_goods_rejected":
                    dbTableSqlSet = tc_record_goods_rejected;
                    break;
                case "tc_supplier_return_account_detail":
                    dbTableSqlSet = tc_supplier_return_account_detail;
                    break;
                case "uc_message_sc_ag":
                    dbTableSqlSet = uc_message_sc_ag;
                    break;
                default:
                    return new TaskExecuteReturn(DicReturnType.NOTYPE);

            }
            String other = this.other(begin, num, dbTableSqlSet);
            if (other.isEmpty()) {
                return new TaskExecuteReturn(DicReturnType.SUCCESS, other, taskData);
            } else {
                return new TaskExecuteReturn(DicReturnType.FAIL, ss_str + other, taskData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new TaskExecuteReturn(DicReturnType.FAIL, e.getMessage(), taskData);
        }
    }

}
