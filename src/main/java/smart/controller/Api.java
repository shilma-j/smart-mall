package smart.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import smart.entity.UserLogEntity;
import smart.lib.Db;
import smart.lib.Json;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import smart.repository.UserLogRepository;

import java.util.*;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class Api {

    @Resource
    UserLogRepository userLogRepository;
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

    @GetMapping("sleep")
    public String getSleep(@RequestParam(defaultValue = "1") int t) throws InterruptedException {
        Thread.sleep(t);
        return Integer.toString(t);
    }

    @GetMapping("test")
    public String getTest(HttpServletRequest request) {
        List<UserLogEntity> list = new LinkedList<>();
        List<Map<String, Object>> rows = new LinkedList<>();
        for(int i = 0;i< 10; i++) {
            UserLogEntity logEntity = new UserLogEntity();
            logEntity.setIp("localhost");
            logEntity.setMsg(Integer.toString(i));
            list.add(logEntity);
            Map<String, Object> row = new HashMap<>();
            row.put("ip", "localhost");
            row.put("msg", row.hashCode());
            row.put("uid", i);
            rows.add(row);

        }


        Db.inserts("t_user_log", rows);
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
