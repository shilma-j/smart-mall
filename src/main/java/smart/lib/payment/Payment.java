package smart.lib.payment;

import java.util.Map;

public interface Payment {
    /**
     * 获取英文简称
     *
     * @return 英文简称
     */
    String getName();

    /**
     * 获取中文简称
     *
     * @return 中文简称
     */
    String getName1();

    /**
     * 初始化参数（全局只需设置一次）
     */
    void setConfig(Map<String, String> map);

    /**
     * 获取收款码,需要转换成QR二维码使用
     *
     * @param title   交易标题
     * @param orderNo 订单号
     * @param amount  订单金额
     * @return 收款码
     * @throws Exception error
     */
    String getQRCode(String title, String orderNo, long amount) throws Exception;

    /**
     * 成功信息短语
     *
     * @return 返会成功信息短语
     */
    String getSuccessResponse();

    /**
     * 处理异步通知
     *
     * @param map 通知参数
     * @return 是否成功
     */
    boolean notify(Map<String, String> map);

    /**
     * 退款
     *
     * @param orderNo 订单号
     * @param amount  退款金额
     * @return null成功, 或失败信息
     */
    String refund(long orderNo, long amount);
}
