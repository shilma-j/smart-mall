package smart.controller;

import smart.lib.Json;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Api {

    @GetMapping(path = "now")
    public String getNow() {
        Map<String, Object> map = new HashMap<>();
        map.put("now", new Date().toString());
        return Json.encode(map);
    }

    @PostMapping(path = "post")
    public String postPost(@RequestBody String str) {
        return str;
    }

    @GetMapping("test")
    public String getTest(HttpServletRequest request) {
        return null;
    }

}
