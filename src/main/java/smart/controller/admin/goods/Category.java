package smart.controller.admin.goods;

import smart.cache.CategoryCache;
import smart.cache.GoodsCache;
import smart.entity.CategoryEntity;
import smart.lib.AdminHelper;
import smart.lib.Helper;
import smart.lib.JsonResult;
import smart.repository.CategoryRepository;
import smart.repository.GoodsRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller(value = "admin/goods/category")
@RequestMapping(path = "/admin/goods/category")
@Transactional
public class Category {

    @Resource
    CategoryRepository categoryRepository;

    @Resource
    GoodsRepository goodsRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElse(null);
        if (categoryEntity == null) {
            jsonResult.setMsg("删除失败,指定的分类不存在");
            return jsonResult.toString();
        }
        long num = categoryRepository.countByParentId(id);
        if (num > 0) {
            jsonResult.setMsg("删除失败,请先删除该分类下的子分类");
            return jsonResult.toString();
        }
        num = goodsRepository.countByCateId(id);
        if (num > 0) {
            jsonResult.setMsg("删除失败,请先删除该分类下的商品");
            return jsonResult.toString();
        }
        categoryRepository.delete(categoryEntity);
        CategoryCache.init();
        GoodsCache.init();
        jsonResult.setMsg("已删除分类: " + categoryEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/category/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        CategoryEntity categoryEntity;
        if (id != 0) {
            categoryEntity = categoryRepository.findById(id).orElse(null);
            if (categoryEntity == null) {
                return AdminHelper.msgPage("商品分类不存在", "list", request);
            }
            modelAndView.addObject("title", "修改商品分类");
        } else {
            categoryEntity = new CategoryEntity();
            categoryEntity.setParentId(Helper.longValue(request.getParameter("parentId")));
            categoryEntity.setRecommend(1000);
            modelAndView.addObject("title", "新建商品分类");
        }
        modelAndView.addObject("list", CategoryCache.getList());
        modelAndView.addObject("item", categoryEntity);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long i = Helper.longValue(request.getParameter("i")) == 0 ? 0 : 1;
        long id = Helper.longValue(request.getParameter("id"));
        long parentId = Helper.longValue(request.getParameter("parentId"));
        if (parentId != 0 && categoryRepository.findById(parentId).orElse(null) == null) {
            jsonResult.setMsg("上级分类不存在,请刷新后重试");
            return jsonResult.toString();
        }
        String name = request.getParameter("name");
        if (name == null || name.length() == 0) {
            jsonResult.setMsg("商品分类名称不得为空");
            return jsonResult.toString();
        }
        CategoryEntity categoryEntity;

        if (id == 0) {
            categoryEntity = new CategoryEntity();
            jsonResult.setMsg("已完成新建商品分类:" + name);
        } else {
            categoryEntity = categoryRepository.findById(id).orElse(null);
            if (categoryEntity == null) {
                jsonResult.setMsg("需要更新的分类不存在，请返回列表后刷新后重试");
                return jsonResult.toString();
            }
            jsonResult.setMsg("已完成商品分类更新:" + name);
        }
        categoryEntity.setI(i);
        categoryEntity.setName(name);
        categoryEntity.setParentId(parentId);
        categoryEntity.setRecommend(Helper.longValue(request.getParameter("recommend")));
        categoryRepository.saveAndFlush(categoryEntity);
        CategoryCache.init();
        GoodsCache.init();
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/goods/category/list", request);
        modelAndView.addObject("list", CategoryCache.getList());
        modelAndView.addObject("title", "商品分类");
        return modelAndView;
    }

}
