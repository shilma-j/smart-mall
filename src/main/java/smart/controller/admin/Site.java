package smart.controller.admin;

import smart.config.AppConfig;
import smart.lib.Security;
import smart.lib.Helper;
import smart.lib.JsonResult;
import smart.lib.session.Session;
import smart.storage.Storage;
import smart.storage.UploadResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;


@Controller(value = "admin/site")
@RequestMapping(path = "admin")
@Transactional
public class Site {

    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/index", request);
        modelAndView.addObject("title", "管理后台");
        return modelAndView;
    }

    @GetMapping(value = "msg")
    public ModelAndView getMsg(HttpServletRequest request, HttpServletResponse response) {
        Session session = Session.from(request);
        ModelAndView modelAndView = Helper.newModelAndView("admin/msg", request);
        var msg = session.get("msg");
        if (msg == null) {
            try {
                response.sendRedirect("/admin");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", session.get("backUrl"));
        session.delete("msg", "backUrl");
        modelAndView.addObject("title", msg);
        return modelAndView;
    }

    @GetMapping(value = "upload")
    public ModelAndView getUpload(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/upload", request);
        modelAndView.addObject("title", "上传文件");
        return modelAndView;
    }

    @PostMapping(value = "upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postUpload(HttpServletRequest request,
                             @RequestParam(name = "file", required = false) MultipartFile file) throws Exception {
        JsonResult jsonResult = new JsonResult();
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            jsonResult.error.put("file", "未发现上传文件");
            return jsonResult.toString();
        }
        String fileName = file.getOriginalFilename();
        String ext = "";
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > 0) {
            ext = fileName.substring(lastIndex).toLowerCase();
            if (ext.equals(".jpe") || ext.equals(".jpeg")) {
                ext = ".jpg";
            }
        }

        String uri = Security.sha3_256(file.getInputStream()) + Long.toHexString(file.getSize());
        uri = new BigInteger(uri, 16).toString(36);
        uri = uri.substring(0, 2) + "/" + uri.substring(2, 4) + "/" + uri.substring(4);

        /* whether keep original filename */
        if (request.getParameter("keep") == null) {
            uri += ext;
        } else {
            uri += "/" + file.getOriginalFilename();
        }

        try (Storage storage = AppConfig.getContext().getBean(Storage.class)) {
            UploadResult uploadResult = storage.upload(file.getInputStream(), uri);
            if (uploadResult.getErr() == null) {
                jsonResult.setUrl(uploadResult.getUrl());
            } else {
                jsonResult.setMsg(uploadResult.getErr());
            }
        }
        file.getInputStream().close();
        return jsonResult.toString();
    }

}
