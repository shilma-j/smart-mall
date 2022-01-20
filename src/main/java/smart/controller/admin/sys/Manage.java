package smart.controller.admin.sys;

import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.lib.AdminHelper;
import smart.lib.Crypto;
import smart.lib.Helper;
import smart.lib.JsonResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Controller(value = "admin/sys/manage")
@RequestMapping(path = "/admin/sys/manage")
@Transactional
public class Manage {

    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/manage/index", request);

        modelAndView.addObject("beian", SystemCache.getBeian());
        modelAndView.addObject("jsPath", SystemCache.getJsPath());
        modelAndView.addObject("keywords", SystemCache.getKeywordsStr());
        modelAndView.addObject("maxBuyNum", SystemCache.getMaxBuyNum());
        modelAndView.addObject("siteName", SystemCache.getSiteName());
        modelAndView.addObject("siteUrl", SystemCache.getUrl());
        modelAndView.addObject("storageType", SystemCache.getStorageType());
        modelAndView.addObject("ossAk", SystemCache.getOssAk());
        modelAndView.addObject("ossAks", SystemCache.getOssAks());
        modelAndView.addObject("ossBucket", SystemCache.getOssBucket());
        modelAndView.addObject("ossBucketUrl", SystemCache.getOssBucketUrl());
        modelAndView.addObject("ossEndpoint", SystemCache.getOssEndpoint());
        modelAndView.addObject("themeMobile", SystemCache.getThemeMobile());
        modelAndView.addObject("themePc", SystemCache.getThemePc());
        modelAndView.addObject("title", "系统管理");
        return modelAndView;
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        Map<String, String> map = new HashMap<>();
        map.put("beian", request.getParameter("beian"));
        map.put("jsPath", request.getParameter("jsPath"));
        map.put("keywords", request.getParameter("keywords"));
        map.put("maxBuyNum", request.getParameter("maxBuyNum"));
        map.put("siteName", request.getParameter("siteName"));
        map.put("siteUrl", request.getParameter("siteUrl"));
        map.put("storageType", request.getParameter("storageType"));
        map.put("ossAk", request.getParameter("ossAk"));
        map.put("ossAks", request.getParameter("ossAks"));
        map.put("ossBucket", request.getParameter("ossBucket"));
        map.put("ossBucketUrl", request.getParameter("ossBucketUrl"));
        map.put("ossEndpoint", request.getParameter("ossEndpoint"));
        map.put("themeMobile", request.getParameter("themeMobile"));
        map.put("themePc", request.getParameter("themePc"));
        map.forEach((k, v) -> {
            if (v == null) {
                map.put(k, "");
            } else {
                map.put(k, v.trim());
            }
        });
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='beian'", map.get("beian"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='jsPath'", map.get("jsPath"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='keywords'", map.get("keywords"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='maxBuyNum'", map.get("maxBuyNum"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='siteName'", map.get("siteName"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='sys' and attribute='url'",
                map.get("siteUrl").replaceAll(" ", "").replaceAll("[/\\\\]+$", ""));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='type'", map.get("storageType"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='ossAk'", Crypto.encrypt(map.get("ossAk")));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='ossAks'", Crypto.encrypt(map.get("ossAks")));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='ossBucket'", map.get("ossBucket"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='ossBucketUrl'",
                map.get("ossBucketUrl").replaceAll(" ", "").replaceAll("[/\\\\]+$", ""));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='storage' and attribute='ossEndpoint'",
                map.get("ossEndpoint").replaceAll(" ", "").replaceAll("[/\\\\]+$", ""));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='theme' and attribute='mobile'", map.get("themeMobile"));
        AppConfig.getJdbcTemplate().update("update t_system set value=? where entity='theme' and attribute='pc'", map.get("themePc"));
        SystemCache.init();
        jsonResult.setMsg("保存成功");
        return AdminHelper.msgPage(jsonResult, request);
    }

}
