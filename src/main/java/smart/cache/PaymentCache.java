package smart.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.lib.Json;
import smart.lib.Security;
import smart.lib.payment.Alipay;
import smart.lib.payment.Payment;
import smart.repository.PaymentRepository;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class PaymentCache {

    private static final Log log = LogFactory.getLog(PaymentCache.class);

    private static PaymentRepository paymentRepository;
    private static List<String> paymentNames;
    private static List<Payment> payments;

    /**
     * 初始化
     */
    @PostConstruct
    public static void init() {
        List<String> names = new LinkedList<>();
        List<Payment> list = new LinkedList<>();

        paymentRepository.findAvailable().forEach(row -> {
            String config = Security.decrypt(row.getConfig());
            if (config == null) {
                log.error("decrypt payment config failure, payment name: %s" + row.getName());
                return;
            }
            if (config.length() < 10) {
                log.error("payment config content error, payment name: %s" + row.getName());
                return;
            }

            Map<String, String> map = Json.decode(config);
            if (map == null || map.keySet().size() < 3) {
                log.error("payment config error, name: %s" + row.getName());
            }

            Payment payment = null;
            switch (row.getName()) {
                case Alipay.NAME:
                    payment = new Alipay();
                    payment.setConfig(map);
                    break;
                default:
                    log.error("unknown payment, name: %s" + row.getName());
            }
            if (payment != null) {
                names.add(payment.getName());
                list.add(payment);
            }

        });
        paymentNames = names;
        payments = list;
    }

    /**
     * 根据支付名称获取支付方式
     *
     * @param name 支付名称(英文)
     * @return 支付方式
     */
    public static Payment getPaymentByName(String name) {
        for (Payment payment : payments) {
            if (payment.getName().equals(name)) {
                return payment;
            }
        }
        return null;
    }

    public static List<String> getPaymentNames() {
        return paymentNames;
    }

    public static List<Payment> getPayments() {
        return payments;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        PaymentCache.paymentRepository = paymentRepository;
    }
}
