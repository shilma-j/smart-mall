package smart.controller.sapi;

import smart.lib.ApiJsonResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.util.Map;



/**
 * Server Application Programming Interface
 * 面向管理后台的接口
 */
@RestController
@RequestMapping(path = "/sapi", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Home {
    /**
     * server hi
     * @param payload json
     * @return {}
     */
    @GetMapping(path = "hi")
    public String getHello(@RequestBody Map<String, Object> payload) {
        return ApiJsonResult.success("hi").toString();
    }
    /**
     * administrator login
     * @param payload json
     * @return {}
     */
    @PostMapping(path = "login")
    public String postPost(@RequestBody Map<String, Object> payload) {
        return ApiJsonResult.success("登录成功").toString();
    }
}
