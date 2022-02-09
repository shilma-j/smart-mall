package smart.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.cache.*;
import smart.lib.Captcha;
import smart.lib.Helper;
import smart.lib.Pagination;
import smart.lib.session.Session;
import smart.service.GoodsService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;

@Controller
@Transactional
public class Site {

    @Resource
    GoodsService goodsService;

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
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response, Session session) {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");
        var captchaResult = Captcha.getImageCode();
        session.set(Helper.camelCase(Captcha.class), captchaResult.phrase());
        try {
            ImageIO.write(captchaResult.image(), "png", response.getOutputStream());
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

        ModelAndView modelAndView = Helper.newModelAndView("list", request);
        long cid = Helper.longValue(request.getParameter("cid"));
        String q = request.getParameter("q");
        if (q == null) {
            q = "";
        }
        String sort = request.getParameter("sort");
        if (sort == null) {
            sort = "";
        }
        Pagination pagination = goodsService.getGoodsList(cid, q, sort, Helper.intValue(request.getParameter("page")));
        modelAndView.addObject("cid", cid);
        modelAndView.addObject("q", q);
        modelAndView.addObject("sort", sort);
        modelAndView.addObject("categoryPath", CategoryCache.getCategoryPath(cid));
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 商品列表");
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
     * @param session  session
     * @return 消息页面
     */
    @GetMapping(value = "msg")
    public ModelAndView getMsg(HttpServletRequest request, HttpServletResponse response, Session session) {
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
