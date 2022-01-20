package smart.controller.admin.other;

import smart.cache.SystemCache;
import smart.lib.AdminHelper;
import smart.lib.Db;
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
import java.util.Map;

@Controller(value = "admin/other/static")
@RequestMapping(path = "/admin/other/static")
@Transactional
public class Static {

    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/other/static/index", request);
        modelAndView.addObject("jsVersion", SystemCache.getJsVersion());
        modelAndView.addObject("title", "静态文件");
        return modelAndView;
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        String jsVersion = request.getParameter("jsVersion");
        if (jsVersion == null) {
            jsVersion = "";
        }
        Db.update("system",
                Map.of("entity", "sys", "attribute", "jsVersion"),
                Map.of("value", jsVersion));
        SystemCache.setJsVersion(jsVersion);
        jsonResult.setMsg("保存成功");
        return AdminHelper.msgPage(jsonResult, request);
    }
}
