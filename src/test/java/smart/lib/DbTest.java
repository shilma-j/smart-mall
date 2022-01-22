package smart.lib;

import smart.entity.OrderGoodsEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DbTest {

    @Test
    void camelCaseToUnderscoresNaming() {
        Assertions.assertEquals(Db.camelCaseToUnderscoresNaming("UserID"), "userid");
        Assertions.assertEquals(Db.camelCaseToUnderscoresNaming("userId"), "user_id");
    }

    @Test
    void getTableNameByEntity() {
        Assertions.assertEquals(Db.getTableNameByEntity(OrderGoodsEntity.class), "t_order_goods");
    }

    @Test
    void underscoresToCamelCaseNaming() {
        Assertions.assertEquals(Db.underscoresToCamelCaseNaming("ORDER_ID"), "orderId");
        Assertions.assertEquals(Db.underscoresToCamelCaseNaming("user_id"), "userId");
    }
}