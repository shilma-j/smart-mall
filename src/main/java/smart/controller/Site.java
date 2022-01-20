package smart.controller;

import smart.cache.*;
import smart.entity.CategoryEntity;
import smart.lib.Captcha;
import smart.lib.Helper;
import smart.lib.Pagination;
import smart.lib.session.Session;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Map;

@Controller
@Transactional
public class Site {
    /**
     * 首页
     *
     * @param request request
     * @return view
     */
    @GetMapping(value = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("index", request);
        modelAndView.addObject("carouselList", SystemCache.getCarouselList());
        modelAndView.addObject("newsArticle", ArticleCache.getArticleCategoryById(1));
        modelAndView.addObject("recommend", GoodsCache.getRecommend());
        modelAndView.addObject("title", SystemCache.getSiteName());
        return modelAndView;
    }

    /**
     * 验证码
     *
     * @param request  request
     * @param response response
     * @param session  session
     */
    @RequestMapping(value = "captcha")
    @ResponseBody
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response,Session session) {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");
        var captchaResult = Captcha.getImageCode();
        session.set(Helper.camelCase(Captcha.class), captchaResult.getPhrase().toLowerCase());
        try {
            ImageIO.write(captchaResult.getImage(), "png", response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分类页面
     *
     * @param request http request
     */
    @GetMapping("category")
    public ModelAndView getCategory(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("category", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 分类");
        return modelAndView;

    }

    /**
     * 商品列表
     *
     * @param request request
     * @return view
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ModelAndView getList(HttpServletRequest request) {
        StringBuilder sql = new StringBuilder("select id,imgs,name,price from t_goods where status & 0b10 > 0");
        ModelAndView modelAndView = Helper.newModelAndView("list", request);
        long cid = Helper.longValue(request.getParameter("cid"));
        CategoryEntity categoryEntity = CategoryCache.getEntityById(cid);
        if (categoryEntity != null) {
            sql.append(" and cate_id in (").append(cid);
            for (var item : CategoryCache.getChildren(cid)) {
                sql.append(",").append(item.getId());
            }
            sql.append(")");
        } else {
            cid = 0;
        }
        long page = Helper.longValue(request.getParameter("page"));
        String q = request.getParameter("q");
        if (q == null) {
            q = "";
        } else {
            q = Helper.stringRemove(q, "'", "\"", "?", "%", "_", "[", "]").trim();
            StringBuilder qSql = new StringBuilder("'%");
            for (var key : q.split(" ")) {
                if (key.length() > 0) {
                    qSql.append(key).append("%");
                }
            }
            qSql.append("'");
            sql.append(" and name like ").append(qSql);
        }
        sql.append(" order by ");
        String sort = request.getParameter("sort");
        if (sort == null) {
            sort = "";
        }

        switch (sort) {
            // new 发布时间排序
            case "n" -> sql.append("released desc,recommend desc,id desc");

            // price 价格排序
            case "p1" -> sql.append("price,recommend desc,update_time desc,id desc");

            // 价格倒序
            case "p2" -> sql.append("price desc,recommend desc,update_time desc,id desc");

            // 默认按推荐排序
            default -> {
                sort = "";
                sql.append("recommend desc,update_time desc,id desc");
            }
        }
        Pagination pagination = new Pagination(sql.toString(), page,
                Map.of("cid", Long.toString(cid), "q", q, "sort", sort));

        String imgs;
        for (var row : pagination.getRows()) {
            imgs = (String) row.get("imgs");
            row.put("img", imgs.split(",")[0]);
        }
        modelAndView.addObject("cid", cid);
        modelAndView.addObject("q", q);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("categoryPath", CategoryCache.getCategoryPath(cid));
        modelAndView.addObject("pagination", pagination);
        if (categoryEntity == null) {
            modelAndView.addObject("title", SystemCache.getSiteName() + " - 商品列表");
        } else {
            modelAndView.addObject("title", SystemCache.getSiteName() + " - " + categoryEntity.getName());
        }

        return modelAndView;
    }

    @GetMapping(path = "region", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getRegion() {
        return RegionCache.getRegionJson();
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public String getTest(Session session) {

        return null;
    }


    /**
     * 显示消息页面
     *
     * @param request  request
     * @param response response
     * @param session session
     * @return 消息页面
     */
    @GetMapping(value = "msg")
    public ModelAndView getMsg(HttpServletRequest request, HttpServletResponse response,Session session) {
        ModelAndView modelAndView = Helper.newModelAndView("msg", request);
        var msg = session.get("msg");
        if (msg == null) {
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        modelAndView.addObject("msg", msg);
        modelAndView.addObject("backUrl", session.get("backUrl"));
        session.delete("msg", "backUrl");
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 信息");
        return modelAndView;
    }
}
