package smart.controller.admin.article;

import jakarta.annotation.Resource;
import smart.cache.ArticleCache;
import smart.config.AppConfig;
import smart.entity.ArticleCategoryEntity;
import smart.lib.AdminHelper;
import smart.lib.Db;
import smart.lib.Helper;
import smart.lib.JsonResult;
import smart.repository.ArticleCategoryRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller(value = "admin/article/category")
@RequestMapping(path = "/admin/article/category")
@Transactional
public class Category {

    @Resource
    ArticleCategoryRepository articleCategoryRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        ArticleCategoryEntity articleCategoryEntity = articleCategoryRepository.findById(id).orElse(null);
        if (articleCategoryEntity == null) {
            jsonResult.setMsg("删除失败,指定的文章分类不存在");
            return jsonResult.toString();
        }
        if (id == 1) {
            jsonResult.setMsg("删除失败,该分类为内置分类，禁止删除");
            return jsonResult.toString();
        }
        long num = Helper.longValue(AppConfig.getJdbcTemplate().queryForObject("select count(*) from t_article where cate_id =?",
                Long.class, id));
        if (num > 0) {
            jsonResult.setMsg("删除失败,该分类下的文章累计个数:" + num);
            return jsonResult.toString();
        }
        articleCategoryRepository.delete(articleCategoryEntity);
        articleCategoryRepository.flush();
        Db.commit();
        ArticleCache.init();
        jsonResult.setMsg("已删除文章分类: " + articleCategoryEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/article/category/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        ArticleCategoryEntity articleCategoryEntity;
        if (id != 0) {
            articleCategoryEntity = articleCategoryRepository.findById(id).orElse(null);
            if (articleCategoryEntity == null) {
                return AdminHelper.msgPage("文章分类不存在", "list", request);
            }
            modelAndView.addObject("title", "修改文章分类");
        } else {
            articleCategoryEntity = new ArticleCategoryEntity();
            articleCategoryEntity.setRecommend(100);
            modelAndView.addObject("title", "新建文章分类");
        }
        modelAndView.addObject("item", articleCategoryEntity);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        String name = request.getParameter("name");
        boolean footerShow = Helper.intValue(request.getParameter("footerShow")) > 0;
        if (name == null || name.trim().length() == 0) {
            jsonResult.setMsg("分类名称不得为空");
            return jsonResult.toString();
        }
        name = name.replace(" ", "");
        ArticleCategoryEntity articleCategoryEntity;

        if (id == 0) {
            articleCategoryEntity = new ArticleCategoryEntity();
            jsonResult.setMsg("已完成新建文章分类:" + name);
        } else {
            articleCategoryEntity = articleCategoryRepository.findById(id).orElse(null);
            if (articleCategoryEntity == null) {
                jsonResult.setMsg("需要更新的文章分类不存在，请返回列表后刷新后重试");
                return jsonResult.toString();
            }
            jsonResult.setMsg("已完成文章分类更新:" + name);
        }
        articleCategoryEntity.setName(name);
        articleCategoryEntity.setFooterShow(footerShow);
        articleCategoryEntity.setRecommend(Helper.longValue(request.getParameter("recommend")));
        articleCategoryRepository.save(articleCategoryEntity);
        articleCategoryRepository.flush();
        Db.commit();
        ArticleCache.init();
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    /**
     * 文章分类
     *
     * @param request request
     * @return page
     */
    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/article/category/list", request);
        modelAndView.addObject("title", "文章分类");
        return modelAndView;
    }
}
