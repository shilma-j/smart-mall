package smart.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import smart.lib.Json;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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

    public static class Pojo {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
