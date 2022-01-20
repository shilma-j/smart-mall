package smart.cache;

import smart.lib.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class SpecCache {
    private static JdbcTemplate jdbcTemplate;
    private static String json;

    @PostConstruct
    public synchronized static void init() {
        var rows = jdbcTemplate.queryForList("select * from t_spec order by sort, name, note, id");

        rows.forEach(row -> {
            String json = (String) row.get("list");
            List<Map<String, String>> list = Json.toList(json);
            row.put("list", list);
        });
        json = Json.encode(rows);
    }

    public static String getJson() {
        return json;
    }

    @Autowired
    private void autowire(JdbcTemplate jdbcTemplate) {
        SpecCache.jdbcTemplate = jdbcTemplate;
    }

}
