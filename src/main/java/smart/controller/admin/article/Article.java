package smart.controller.admin.article;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.ArticleCache;
import smart.entity.ArticleEntity;
import smart.lib.*;
import smart.repository.ArticleCategoryRepository;
import smart.repository.ArticleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@Controller(value = "admin/article/article")
@RequestMapping(path = "/admin/article/article")
@Transactional
public class Article {

    @Resource
    ArticleRepository articleRepository;

    @Resource
    ArticleCategoryRepository articleCategoryRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        ArticleEntity articleEntity = articleRepository.findById(id).orElse(null);
        if (articleEntity == null) {
            jsonResult.setMsg("删除失败,指定的文章不存在");
            return jsonResult.toString();
        }
        articleRepository.delete(articleEntity);
        Db.commit();
        ArticleCache.init();
        jsonResult.setMsg("已删除文章: " + articleEntity.getTitle());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView view = Helper.newModelAndView("admin/article/article/edit", request);
        long id = Helper.longValue(request.getParameter("id"));
        ArticleEntity articleEntity;
        if (id == 0) {
            articleEntity = new ArticleEntity();
            articleEntity.setReleaseTime(new Timestamp(System.currentTimeMillis()));
            articleEntity.setRecommend(100);
            view.addObject("title", "新建文章");
        } else {
            articleEntity = articleRepository.findById(id).orElse(null);
            if (articleEntity == null) {
                return AdminHelper.msgPage("文章不存在", "list", request);
            }
            view.addObject("title", "修改文章");
        }
        view.addObject("item", articleEntity);
        return view;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        long cateId = Helper.longValue(request.getParameter("cateId"));
        if (cateId != 0 && articleCategoryRepository.findById(cateId).orElse(null) == null) {
            jsonResult.setMsg("文章类别不存在");
            return jsonResult.toString();
        }
        boolean visible = Helper.longValue(request.getParameter("visible")) > 0;
        String title = request.getParameter("title");
        if (title == null || title.trim().length() == 0) {
            jsonResult.setMsg("标题不得为空");
            return jsonResult.toString();
        }
        String content = request.getParameter("content");
        if (content == null) {
            content = "";
        }

        title = title.replace(" ", "");
        java.util.Date releaseTime;
        try {
            releaseTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(request.getParameter("releaseTime"));
        } catch (ParseException | NullPointerException ignored) {
            jsonResult.setMsg("请选择发布日期");
            return jsonResult.toString();
        }

        ArticleEntity articleEntity;
        if (id == 0) {
            articleEntity = new ArticleEntity();
            jsonResult.setMsg("已完成新建文章:" + title);
        } else {
            articleEntity = articleRepository.findById(id).orElse(null);
            if (articleEntity == null) {
                jsonResult.setMsg("需要修改的文章不存在，请返回列表后刷新后重试");
                return jsonResult.toString();
            }
            jsonResult.setMsg("已完成文章修改:" + title);
        }

        articleEntity.setTitle(title);
        articleEntity.setContent(content);
        articleEntity.setCateId(cateId);
        articleEntity.setReleaseTime(new Timestamp(releaseTime.getTime()));
        articleEntity.setVisible(visible);
        articleRepository.save(articleEntity);
        Db.commit();
        ArticleCache.init();
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "list")
    public ModelAndView getList(HttpServletRequest request) {
        long cateId = Helper.longValue(request.getParameter("cateId"));
        String sqlWhere = cateId > 0 ? "where a.cateId=" + cateId : "";
        String sql = """
                select a.id,
                       a.cate_id,
                       ac.name as cate_name,
                       a.content,
                       a.title,
                       a.release_time as release_time,
                       a.visible,
                       a.recommend
                from t_article a
                         left join t_article_category ac on a.cate_id = ac.id
                %s
                order by a.release_time desc, a.recommend desc
                """;
        sql = String.format(sql, sqlWhere);
        Pagination pagination = Pagination.newBuilder(sql)
                .page(request)
                .query(Map.of("cateId", Long.toString(cateId)))
                .build();
        ModelAndView view = Helper.newModelAndView("admin/article/article/list", request);
        view.addObject("cateId", cateId);
        view.addObject("pagination", pagination);
        view.addObject("title", "文章列表");
        return view;
    }

}
