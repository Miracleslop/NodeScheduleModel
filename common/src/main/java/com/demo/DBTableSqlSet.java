package com.demo;

public class DBTableSqlSet {
    private final String select;
    private final String update;
    private final String count_sql;

    public DBTableSqlSet(String select, String update, String count_sql) {
        this.select = select;
        this.update = update;
        this.count_sql = count_sql;
    }

    public String getSelect() {
        return select;
    }

    public String getUpdate() {
        return update;
    }

    public String getCount_sql() {
        return count_sql;
    }


    public static final DBTableSqlSet gc_goods_spec = new DBTableSqlSet("SELECT gspec_id, gsku_id, gspu_id FROM gc_goods_spec LIMIT %d, %d",
            "UPDATE gc_goods_spec SET gsku_id = %d WHERE gspec_id = %d",
            "SELECT count(1) FROM gc_goods_spec ");
    public static final DBTableSqlSet gc_shopping_cart = new DBTableSqlSet("SELECT sc_id, gsku_id, gspu_id FROM gc_shopping_cart LIMIT %d, %d",
            "UPDATE gc_shopping_cart SET gsku_id = %d WHERE sc_id = %d",
            "SELECT count(1) FROM gc_shopping_cart ");

    public static final DBTableSqlSet tc_discount_comm = new DBTableSqlSet("SELECT id, gsku_id FROM tc_discount_comm LIMIT %d, %d",
            "UPDATE tc_discount_comm SET gsku_id = %d WHERE id = %d",
            "SELECT count(1) FROM tc_discount_comm ");
    public static final DBTableSqlSet tc_order_detail = new DBTableSqlSet("SELECT od_id, gsku_id, gspu_id FROM tc_order_detail LIMIT %d, %d",
            "UPDATE tc_order_detail SET gsku_id = %d WHERE od_id = %d",
            "SELECT count(1) FROM tc_order_detail ");
    public static final DBTableSqlSet tc_record_goods_rejected = new DBTableSqlSet("SELECT id, gsku_id, gspu_id FROM tc_record_goods_rejected LIMIT %d, %d",
            "UPDATE tc_record_goods_rejected SET gsku_id = %d WHERE id = %d",
            "SELECT count(1) FROM tc_record_goods_rejected ");

    public static final DBTableSqlSet tc_supplier_return_account_detail = new DBTableSqlSet("SELECT srd_id, sku_id FROM tc_supplier_return_account_detail LIMIT %d, %d",
            "UPDATE tc_supplier_return_account_detail SET sku_id = %d WHERE srd_id = %d",
            "SELECT count(1) FROM tc_supplier_return_account_detail ");
    public static final DBTableSqlSet uc_message_sc_ag = new DBTableSqlSet("SELECT scag_id, gsku_id, gspu_id FROM uc_message_sc_ag LIMIT %d, %d",
            "UPDATE uc_message_sc_ag SET gsku_id = %d WHERE scag_id = %d",
            "SELECT count(1) FROM uc_message_sc_ag ");

}
