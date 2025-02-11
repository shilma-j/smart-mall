package smart.service;

import jakarta.annotation.Resource;
import smart.config.AppConfig;
import smart.entity.OrderGoodsEntity;
import smart.repository.OrderGoodsRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;

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
}
