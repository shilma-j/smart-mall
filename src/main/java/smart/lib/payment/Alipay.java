package smart.lib.payment;

import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.lib.Helper;
import smart.service.OrderService;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class Alipay implements Payment {

    public static final String NAME = "alipay";
    public static final String NAME1 = "支付宝";

    //中文名称
    private String name1;

    @Override
    public String getName() {
        // 英文名称
        return NAME;
    }

    @Override
    public String getName1() {
        return NAME1;
    }

    /**
     * 获取收款码
     *
     * @param title   交易标题
     * @param orderNo 订单号
     * @param amount  订单金额
     * @return 收款码
     * @throws Exception error
     */
    @Override
    public String getQRCode(String title, String orderNo, long amount) throws Exception {

        AlipayTradePrecreateResponse alipayResponse = Factory.Payment.FaceToFace()
                .preCreate(title, orderNo, Helper.priceFormat(amount));
        if (ResponseChecker.success(alipayResponse)) {
            return alipayResponse.qrCode;
        }
        throw new Exception(alipayResponse.msg + "," + alipayResponse.subMsg);
    }

    @Override
    public String getSuccessResponse() {
        return "SUCCESS";
    }

    @Override
    @Transactional
    public boolean notify(Map<String, String> map) {
        try {
            if (Factory.Payment.Common().verifyNotify(map)
                    // 需要交易成功状态
                    && "TRADE_SUCCESS".equals(map.get("trade_status"))) {
                long orderNo = Helper.longValue(map.get("out_trade_no"));
                BigDecimal decimal = new BigDecimal(map.get("buyer_pay_amount"));
                decimal = decimal.multiply(new BigDecimal("100"));
                long payAmount = Helper.longValue(decimal);
                OrderService orderService = AppConfig.getContext().getBean(OrderService.class);
                if (orderService.pay(orderNo, NAME, payAmount, map.get("trade_no")) == null) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public String refund(long orderNo, long amount) {
        try {
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(Long.toString(orderNo), Helper.priceFormat(amount));
            if (!response.msg.equals("Success")) {
                return response.subMsg;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }


    @Override
    public void setConfig(Map<String, String> map) {
        Config config = new Config();
        config.protocol = "https";

        // 正式 openapi.alipay.com
        // 沙箱 openapi.alipaydev.com
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = "RSA2";
        config.appId = map.get("appId");
        config.merchantPrivateKey = map.get("merchantPrivateKey");
        config.alipayPublicKey = map.get("alipayPublicKey");
        //异步通知接收服务地址（可选）
        config.notifyUrl = SystemCache.getUrl() + "/payNotify/" + getName();
        // 1. 设置参数（全局只需设置一次）
        Factory.setOptions(config);
    }
}
