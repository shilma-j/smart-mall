package smart.controller.admin.goods;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.SpecCache;
import smart.entity.SpecEntity;
import smart.lib.*;
import smart.repository.SpecRepository;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller(value = "admin/goods/spec")
@RequestMapping(path = "/admin/goods/spec")
@Transactional
public class Spec {

    @Resource
    SpecRepository specRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        SpecEntity specEntity = specRepository.findById(id).orElse(null);
        if (specEntity == null) {
            jsonResult.setMsg("删除失败,指定的商品规格不存在");
            return jsonResult.toString();
        }
        specRepository.delete(specEntity);
        SpecCache.init();
        jsonResult.setMsg("已删除商品规格: " + specEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/spec/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        SpecEntity specEntity;
        if (id != 0) {
            specEntity = specRepository.findById(id).orElse(null);
            if (specEntity == null) {
                return AdminHelper.msgPage("规格不存在", "list", request);
            }
            modelAndView.addObject("title", "修改规格");
        } else {
            specEntity = new SpecEntity();
            specEntity.setSort(50);
            specEntity.setList("[]");
            modelAndView.addObject("title", "新建规格");
        }
        modelAndView.addObject("item", specEntity);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        String[] dStr = new String[]{" "};
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        long sort = Helper.longValue(request.getParameter("sort"));
        String name = request.getParameter("name");
        String note = Helper.stringRemove(request.getParameter("note"), dStr);
        String[] values = request.getParameterValues("specVal");
        String[] hints = request.getParameterValues("specHint");
        String[] imgs = request.getParameterValues("specImg");
        if (name == null || name.trim().length() == 0) {
            jsonResult.setMsg("规格名称不得为空");
            return jsonResult.toString();
        }
        name = name.trim();
        if (values == null || hints == null || imgs == null || values.length == 0) {
            jsonResult.setMsg("没有接收到规格值");
            return jsonResult.toString();
        }
        int len = values.length;
        if (hints.length != len || imgs.length != len) {
            jsonResult.setMsg("规格值数据错误");
            return jsonResult.toString();
        }
        List<Map<String, String>> rows = new LinkedList<>();
        for (int i = 0; i < len; i++) {
            String val = values[i];
            if (val == null) {
                continue;
            }
            val = val.replace(" ", "");
            // skip repeat value
            String finalVal = val;
            if (val.length() == 0 ||
                    rows.stream().anyMatch(row -> row.get("val").equals(finalVal))) {
                continue;
            }

            String hint = hints[i] == null ? "" : hints[i].trim();
            String img = imgs[i] == null ? "" : imgs[i].trim();
            if (img.length() > 0 && !img.contains("?")) {
                img = HelperUtils.imgZoom(img, 40, 40);
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put("val", val);
            row.put("hint", hint);
            row.put("img", img);
            rows.add(row);
        }

        if (rows.size() == 0) {
            jsonResult.setMsg("规格值不得为空");
            return jsonResult.toString();
        }
        String json = Json.encode(rows);
        if (json == null) {
            jsonResult.setMsg("内部错误，请联系管理员");
            return jsonResult.toString();
        }

        SpecEntity specEntity;

        if (id == 0) {
            specEntity = new SpecEntity();
            jsonResult.setMsg("已完成新建商品规格:" + name);
        } else {
            specEntity = specRepository.findById(id).orElse(null);
            if (specEntity == null) {
                jsonResult.setMsg("需要更新的规格不存在，请返回列表后刷新后重试");
                return jsonResult.toString();
            }
            jsonResult.setMsg("已完成商品规格更新:" + name);
        }
        specEntity.setName(name);
        specEntity.setNote(note);
        specEntity.setSort(sort);
        specEntity.setList(json);
        specRepository.saveAndFlush(specEntity);
        SpecCache.init();
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        Pagination pagination = Pagination.newBuilder("select * from t_spec order by name,id")
                .page(request)
                .build();
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/spec/list", request);
        pagination.getRows().forEach(row -> {
            String str = (String) row.get("list");
            List<Map<String, String>> rows = Json.toList(str);
            StringBuilder sb = new StringBuilder();
            rows.forEach(spec -> sb.append(spec.get("val")).append(", "));
            String value = sb.toString();
            if (value.length() > 2) {
                value = value.substring(0, value.length() - 2);
            }
            row.put("value", value);
        });
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", "商品规格");
        return modelAndView;
    }

}
