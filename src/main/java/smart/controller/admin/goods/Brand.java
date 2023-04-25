package smart.controller.admin.goods;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.BrandCache;
import smart.config.AppConfig;
import smart.entity.BrandEntity;
import smart.lib.AdminHelper;
import smart.util.Helper;
import smart.lib.JsonResult;
import smart.lib.Pagination;
import smart.repository.BrandRepository;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller(value = "admin/goods/brand")
@RequestMapping(path = "/admin/goods/brand")
@Transactional
public class Brand {

    @Resource
    BrandRepository brandRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        BrandEntity brandEntity = brandRepository.findByIdForUpdate(id);
        if (brandEntity == null) {
            jsonResult.setMsg("删除失败,指定的商品品牌不存在");
            return jsonResult.toString();
        }
        long num = Helper.longValue(AppConfig.getJdbcTemplate().queryForObject("select count(*) from t_goods where cate_id =?",
                Long.class, id));
        if (num > 0) {
            jsonResult.setMsg("删除失败,该品牌的商品累计个数:" + num);
            return jsonResult.toString();
        }
        brandRepository.delete(brandEntity);
        BrandCache.getRows().remove(id);
        jsonResult.setMsg("已删除商品品牌: " + brandEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/brand/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        BrandEntity brandEntity;
        if (id != 0) {
            brandEntity = brandRepository.findById(id).orElse(null);
            if (brandEntity == null) {
                return AdminHelper.msgPage("商品品牌不存在", "list", request);
            }
            modelAndView.addObject("title", "修改商品品牌");
        } else {
            brandEntity = new BrandEntity();
            modelAndView.addObject("title", "新建商品品牌");
        }
        modelAndView.addObject("item", brandEntity);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        String name = request.getParameter("name");
        if (name == null || name.trim().length() == 0) {
            jsonResult.setMsg("品牌名称不得为空");
            return jsonResult.toString();
        }
        name = name.replace(" ", "");
        String note = request.getParameter("note");
        if (note == null) {
            note = "";
        }
        BrandEntity brandEntity;

        if (id == 0) {
            brandEntity = new BrandEntity();
            jsonResult.setMsg("已完成新建商品品牌:" + name);
        } else {
            brandEntity = brandRepository.findById(id).orElse(null);
            if (brandEntity == null) {
                jsonResult.setMsg("需要更新的商品品牌不存在，请返回列表后刷新后重试");
                return jsonResult.toString();
            }
            jsonResult.setMsg("已完成商品品牌更新:" + name);
        }

        brandEntity.setName(name);
        brandEntity.setNote(note);
        brandRepository.saveAndFlush(brandEntity);
        BrandCache.getRows().put(brandEntity.getId(), brandEntity);
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        Pagination pagination = Pagination.newBuilder("select * from t_brand order by name")
                .page(request)
                .build();
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/brand/list", request);
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", "商品品牌");
        return modelAndView;
    }

}
