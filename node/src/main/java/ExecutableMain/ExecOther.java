package ExecutableMain;

import com.demo.DBTableSqlSet;
import com.demo.TaskExecuteReturn;
import com.demo.TaskExecutor;
import com.demo.constans.DicReturnType;

import java.util.HashMap;
import java.util.Map;

import static com.demo.DBTableSqlSet.*;

public class ExecOther implements TaskExecutor {

    public Map<Long, Long> o2n;

    public String other(final int begin, final int end, DBTableSqlSet dbTableSqlSet) {
        String select = String.format(dbTableSqlSet.getSelect(), begin, end);
        String update = String.format(dbTableSqlSet.getUpdate(), 1, 2);
        return "";
    }

    @Override
    public TaskExecuteReturn handle(String taskData) {
        try {
            int i = taskData.indexOf(",");
            int begin = Integer.parseInt(taskData.substring(3, i));
            int i2 = taskData.indexOf(",", i + 1);
            int num = Integer.parseInt(taskData.substring(i + 1, i2));
            String ss_str = taskData.substring(i2);
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
            if (other.startsWith(DicReturnType.SUCCESS.str())) {
                return new TaskExecuteReturn(DicReturnType.SUCCESS, other, taskData);
            } else {
                return new TaskExecuteReturn(DicReturnType.FAIL, other, taskData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new TaskExecuteReturn(DicReturnType.FAIL, e.getMessage(), taskData);
        }
    }

    public static void main(String[] args) {

    }
}
