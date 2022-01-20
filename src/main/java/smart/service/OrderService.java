package smart.service;

import smart.cache.ExpressCache;
import smart.cache.PaymentCache;
import smart.config.AppConfig;
import smart.entity.*;
import smart.lib.Cart;
import smart.lib.Db;
import smart.lib.Helper;
import smart.lib.Validate;
import smart.lib.payment.Payment;
import smart.lib.status.GoodsStatus;
import smart.lib.status.OrderGoodsStatus;
import smart.lib.status.OrderStatus;
import smart.repository.*;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
public class OrderService {

    @Resource
    GoodsRepository goodsRepository;

    @Resource
    GoodsSpecRepository goodsSpecRepository;

    @Resource
    OrderRepository orderRepository;

    @Resource
    OrderGoodsService orderGoodsService;

    @Resource
    UserAddressRepository userAddressRepository;

    @Resource
    UserRepository userRepository;

    /**
     * 根据订单ID生成订单号
     *
     * @param orderId 订单ID
     * @return 订单号
     */
    public static long generalOrderNo(long orderId) {
        String str = Helper.dateFormat(new Date(), "yyMMdd");
        String str1 = String.format("%06d", orderId * 997 % 1000000);
        return Helper.longValue(str + str1);
    }

    /**
     * 取消未付款的订单
     *
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    @Transactional
    public String cancelOrder(long orderNo) {
        if (AppConfig.getJdbcTemplate().
                update("update t_order set status=4 where no = ? and status = 0 and payTime is null", orderNo) == 0) {
            return "订单信息错误";
        }
        // 增加库存
        orderGoodsService.backOrderGoods(orderNo);
        return null;
    }


    /**
     * 取消未付款的订单,取消时验证用户是否拥有该订单
     *
     * @param userId  user id
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    @Transactional
    public String cancelOrder(long userId, long orderNo) {
        if (AppConfig.getJdbcTemplate().
                update("update t_order set status=4 where no = ? and userId = ? and status = 0 and payTime is null", orderNo, userId) == 0) {
            return "订单信息错误";
        }
        // 增加库存
        orderGoodsService.backOrderGoods(orderNo);
        return null;
    }

    /**
     * 确认收货
     *
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    @Transactional
    public String confirmOrder(long orderNo) {
        if (AppConfig.getJdbcTemplate().
                update("update t_order set status=3, confirmTime=NOW() where no = ? and status = 2", orderNo) == 0) {
            return "订单信息错误";
        }
        AppConfig.getJdbcTemplate().update("update t_order_goods set status = ? where order_no = ?", OrderGoodsStatus.RECEIVED.getCode(), orderNo);
        return null;
    }

    /**
     * 确认收货,确认时验证用户是否拥有该订单
     *
     * @param userId  user id
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    @Transactional
    public String confirmOrder(long userId, long orderNo) {
        if (AppConfig.getJdbcTemplate().
                update("update t_order set status=3, confirmTime=NOW() where no = ? and userId = ? and status = 2", orderNo, userId) == 0) {
            return "订单信息错误";
        }
        AppConfig.getJdbcTemplate().update("update t_order_goods set status = ? where order_no = ?", OrderGoodsStatus.RECEIVED.getCode(), orderNo);
        return null;
    }

    /**
     * 设置订单删除状态, 复原/回收站/删除
     *
     * @param userId  user id
     * @param orderNo order no
     * @param deleted deleted status
     * @return null(成功) or 错误信息
     */
    public String deleteOrder(long userId, long orderNo, int deleted) {
        if (deleted < 0 || deleted > 2) {
            return "删除参数不正确";
        }
        String sql = "update t_order set deleted=? where no = ? and userId = ? and deleted=? and status > 2";
        int tmp = 0;
        int oldDeleted = deleted == 1 ? 0 : 1;
        if (AppConfig.getJdbcTemplate().update(sql, deleted, orderNo, userId, oldDeleted) == 0) {
            return "订单信息错误";
        }
        return null;
    }


    /**
     * 下单
     *
     * @param addrId      地址ID
     * @param cart        用户购物车
     * @param sumPrice    商品总金额,用于效验
     * @param shippingFee 物流费,用于效验
     * @param source      订单来源 1电脑网页,2移动端网页,3微信公众号,4微信小程序
     * @return 订单信息
     */
    @Transactional
    public OrderInfo addOrder(long addrId,
                              String payName,
                              Cart cart,
                              long sumPrice,
                              long shippingFee,
                              long source) {
        OrderInfo orderInfo = new OrderInfo();
        long userId = cart.getUserToken().getId();
        UserEntity userEntity = userRepository.findByIdForUpdate(userId);
        if (userEntity == null || userEntity.getStatus() != 0) {
            return orderInfo.setErr("用户不存在或状态异常");
        }
        UserAddressEntity addr = userAddressRepository.findByIdAndUserId(addrId, userId);
        if (addr == null) {
            return orderInfo.setErr("收货地址不存在");
        }
        if (payName == null || !PaymentCache.getPaymentNames().contains(payName)) {
            return orderInfo.setErr("支付方式不正确");
        }
        if (cart.sumPrice() != sumPrice || cart.shippingFee(addr.getRegion()) != shippingFee || cart.sumNum() == 0) {
            return orderInfo.setErr("购物车被修改，请重新提交");
        }

        //要购买的商品,完成后从购物车清除
        Set<Cart.Item> cartItems = new LinkedHashSet<>();
        for (var item : cart.getItems()) {
            if (!item.isSelected()) {
                continue;
            }
            cartItems.add(item);
        }
        // 优化锁行顺序, 按商品ID排序后顺序锁行
        List<Long> goodsIds = new ArrayList<>();
        for (var item : cartItems) {
            goodsIds.add(item.getGoodsId());
        }
        Collections.sort(goodsIds);
        Map<Long, GoodsEntity> goodsEntityMap = new HashMap<>();
        for (var goodsId : goodsIds) {
            try {
                goodsEntityMap.put(goodsId, goodsRepository.findByIdForUpdate(goodsId));
            } catch (CannotAcquireLockException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return orderInfo.setErr("系统繁忙");
            }
        }

        for (var item : cartItems) {
            GoodsEntity goodsEntity = goodsEntityMap.get(item.getGoodsId());
            if (goodsEntity == null
                    || (goodsEntity.getStatus() & GoodsStatus.RECYCLE_BIN) > 0
                    || (goodsEntity.getStatus() & GoodsStatus.ON_SELL) == 0
            ) {
                return orderInfo.setErr("商品已下架");
            }
            // 库存
            if (item.getSpecId() == 0) {
                if (goodsEntity.getStock() < item.getNum()) {
                    return orderInfo.setErr("库存不足:" + goodsEntity.getName());
                }
            } else {
                GoodsSpecEntity goodsSpecEntity = null;
                try {
                    goodsSpecEntity = goodsSpecRepository.findByIdForUpdate(item.getSpecId());
                } catch (CannotAcquireLockException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return orderInfo.setErr("系统繁忙");
                }
                if (goodsSpecEntity == null || goodsSpecEntity.getGoodsId() != item.getGoodsId()) {
                    return orderInfo.setErr("规格数据错误:" + goodsEntity.getName());
                }
                if (goodsSpecEntity.getStock() < item.getNum()) {
                    return orderInfo.setErr("库存不足:" + goodsEntity.getName());
                }
            }
        }
        // 订单ID
        long orderId = AppConfig.getOrderId().incrementAndGet();
        // 订单号
        long orderNo = generalOrderNo(orderId);
        // 订单金额
        long orderAmount = sumPrice + shippingFee;
        // 创建订单信息记录
        Map<String, Object> orderRow = new HashMap<>();
        orderRow.put("address", addr.getAddress());
        orderRow.put("amount", orderAmount);
        orderRow.put("consignee", addr.getConsignee());
        orderRow.put("createTime", Helper.now());
        orderRow.put("id", orderId);
        orderRow.put("no", orderNo);
        orderRow.put("payName", payName);
        orderRow.put("phone", addr.getPhone());
        orderRow.put("region", addr.getRegion());
        orderRow.put("shippingFee", shippingFee);
        orderRow.put("source", source);
        orderRow.put("userId", userId);
        // 0额订单无需支付
        if (orderAmount == 0) {
            orderRow.put("status", OrderStatus.WAIT_FOR_SHIPPING.getCode());
            orderRow.put("payTime", Helper.now());
        }

        // 数据库保存订单
        Db.insert("orders", orderRow);

        // 扣库存，创建订单商品
        cartItems.forEach(item -> {
            if (item.getSpecId() == 0) {
                AppConfig.getJdbcTemplate().update(
                        "update t_goods set stock=stock - ? where id = ?",
                        item.getNum(), item.getGoodsId());
            } else {
                AppConfig.getJdbcTemplate().update(
                        "update t_goods_spec set stock=stock - ? where id = ?",
                        item.getNum(), item.getSpecId());
            }
            OrderGoodsEntity orderGoodsEntity = new OrderGoodsEntity();
            orderGoodsEntity.setOrderNo(orderNo);
            orderGoodsEntity.setGoodsId(item.getGoodsId());
            orderGoodsEntity.setGoodsName(item.getGoodsName());
            orderGoodsEntity.setNum(item.getNum());
            orderGoodsEntity.setSpecId(item.getSpecId());
            orderGoodsEntity.setSpecDes(item.getSpecDes());
            orderGoodsEntity.setPrice(item.getGoodsPrice());
            orderGoodsEntity.setWeight(item.getGoodsWeight());
            orderGoodsEntity.setImg(item.getGoodsImg());
            orderGoodsService.insertOrderGoods(orderGoodsEntity);
        });
        cart.del(cartItems);
        orderInfo.setOrderNo(orderNo).setAmount(orderAmount);
        return orderInfo;

    }

    /**
     * 支付订单
     *
     * @param orderNo   订单号
     * @param payName   支付方式
     * @param payAmount 支付金额
     * @param payNo     支付流水号
     * @return null 成功，或返回错误信息
     */
    @Transactional
    public String pay(long orderNo, String payName, long payAmount, String payNo) {
        if (payName == null) {
            return "支付方式不得为空";
        }
        OrderEntity orderEntity = orderRepository.findByNoForUpdate(orderNo);
        if (orderEntity == null) {
            return "订单不存在";
        }
        if (orderEntity.getStatus() != 0) {
            return "该订单不是待支付订单";
        }
        AppConfig.getJdbcTemplate().update("update t_order set payName = ?,payTime = ?,payAmount = ?,payNo =?,status = 1 where no = ?",
                payName, new Timestamp(System.currentTimeMillis()), payAmount, payNo, orderNo);
        return null;
    }

    /**
     * 订单发货/修改物流信息
     *
     * @param orderNo 订单号
     * @return null 成功，或返回错误信息
     */
    @Transactional
    public String ship(long orderNo, long expressId, String expressNo) {
        OrderEntity orderEntity = orderRepository.findByNoForUpdate(orderNo);
        if (orderEntity == null) {
            return "订单不存在";
        }
        if (orderEntity.getStatus() != 1 && orderEntity.getStatus() != 2) {
            return "订单状态错误";
        }
        if (ExpressCache.getNameById(expressId) == null) {
            return "快递公司id错误,express id:" + expressId;
        }
        if (Validate.notEmpty(expressNo, "") != null) {
            return "订单号不得为空";
        }
        expressNo = expressNo.trim();
        AppConfig.getJdbcTemplate().update("update t_order set expressId = ?, express_no = ?, shippingTime = ?,status = 2 where no = ?",
                expressId, expressNo, new Timestamp(System.currentTimeMillis()), orderNo);
        AppConfig.getJdbcTemplate().update("update t_order_goods set status = ? where order_no = ?", OrderGoodsStatus.SHIPPED.getCode(), orderNo);
        return null;
    }

    /**
     * 订单退款
     *
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    @Transactional
    public String refundOrder(long orderNo) {
        OrderEntity orderEntity = orderRepository.findByNoForUpdate(orderNo);
        if (orderEntity == null) {
            return "订单不存在";
        }
        // 订单金额大于0的原路退回
        if (orderEntity.getAmount() > 0) {
            Payment payment = PaymentCache.getPaymentByName(orderEntity.getPayName());
            if (payment != null) {
                var msg = payment.refund(orderNo, orderEntity.getPayAmount());
                if (msg != null) {
                    return msg;
                }
            }
        }

        if (AppConfig.getJdbcTemplate().
                update("update t_order set status = 5 where no = ? and status in (1,2,3) and payTime is not null", orderNo) == 0) {
            return "订单信息错误";
        }
        AppConfig.getJdbcTemplate().update("update t_order_goods set status = ? where order_no = ?", OrderGoodsStatus.RETURNED.getCode(), orderNo);

        return null;
    }

    public static class OrderInfo {
        private String err;
        private long orderNo;
        // 订单金额
        private long amount;

        public String getErr() {
            return err;
        }

        public OrderInfo setErr(String err) {
            this.err = err;
            return this;
        }

        public long getOrderNo() {
            return orderNo;
        }

        public OrderInfo setOrderNo(long orderNo) {
            this.orderNo = orderNo;
            return this;
        }

        public long getAmount() {
            return amount;
        }

        public OrderInfo setAmount(long amount) {
            this.amount = amount;
            return this;
        }
    }
}
