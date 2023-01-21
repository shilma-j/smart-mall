package smart.cache;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smart.config.AppConfig;
import smart.entity.ExpressCompanyEntity;
import smart.lib.Json;
import smart.lib.express.FreeRule;
import smart.lib.express.PriceRule;
import smart.repository.ExpressCompanyRepository;

import java.util.List;

@Component
public class ExpressCache {
    private static List<ExpressCompanyEntity> companies;
    private static ExpressCompanyRepository expressCompanyRepository;
    private static FreeRule freeRule;
    private static PriceRule priceRule;

    @PostConstruct
    public static synchronized void init() {
        companies = expressCompanyRepository.findAll();
        var list = AppConfig.getJdbcTemplate().queryForList("select * from t_system where entity='shipping'");
        String json;
        for (var map : list) {
            json = (String) map.get("value");
            if (json.length() < 10) {
                continue;
            }
            switch ((String) map.get("attribute")) {
                case "freeRule" -> freeRule = Json.decode(json, FreeRule.class);
                case "priceRule" -> priceRule = Json.decode(json, PriceRule.class);
            }
        }
        if (freeRule == null) {
            freeRule = new FreeRule();
        }
        if (priceRule == null) {
            priceRule = new PriceRule();
        }
    }

    public static List<ExpressCompanyEntity> getCompanies() {
        return companies;
    }

    /**
     * 根据id获取快递公司名称
     *
     * @param id 快递id
     * @return 快递公司名称
     */
    public static String getNameById(long id) {
        for (var c : companies) {
            if (c.getId() == id) {
                return c.getName();
            }
        }
        return null;
    }

    public static FreeRule getFreeRule() {
        return freeRule;
    }

    public static PriceRule getPriceRule() {
        return priceRule;
    }

    @Autowired
    private void autowire(ExpressCompanyRepository expressCompanyRepository) {
        ExpressCache.expressCompanyRepository = expressCompanyRepository;
    }


}
