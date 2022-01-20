package smart.service;

import smart.config.AppConfig;
import smart.entity.OrderGoodsEntity;
import smart.lib.Db;
import smart.repository.OrderGoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderGoodsService {
    @Resource
    OrderGoodsRepository orderGoodsRepository;

    /**
     * 订单中的商品返回库存
     *
     * @param orderNo 订单号
     */
    @Transactional
    public void backOrderGoods(long orderNo) {
        List<OrderGoodsEntity> goodsEntities = orderGoodsRepository.findAllByOrderNoForUpdate(orderNo);
        for (var item : goodsEntities) {
            if (item.getSpecId() > 0) {
                AppConfig.getJdbcTemplate().update(
                        "update t_goods_spec set stock = stock + ? where id = ?", item.getNum(), item.getSpecId());
            } else {
                AppConfig.getJdbcTemplate().update("update t_goods set stock = stock + ? where id = ?", item.getNum(), item.getGoodsId());
            }
        }
    }

    /**
     * 数据库插入订单商品记录
     *
     * @param ety orderGoodsEntity
     */
    public void insertOrderGoods(OrderGoodsEntity ety) {
        Map<String, Object> row = new HashMap<>();
        row.put("orderNo", ety.getOrderNo());
        row.put("goodsId", ety.getGoodsId());
        row.put("specId", ety.getSpecId());
        row.put("specDes", ety.getSpecDes());
        row.put("goodsName", ety.getGoodsName());
        row.put("price", ety.getPrice());
        row.put("weight", ety.getWeight());
        row.put("num", ety.getNum());
        row.put("img", ety.getImg());
        Db.insert("orderGoods", row);
    }
}
