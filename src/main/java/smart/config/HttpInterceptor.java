package smart.config;

import smart.authentication.UserToken;
import smart.cache.ArticleCache;
import smart.cache.CategoryCache;
import smart.cache.SystemCache;
import smart.lib.Helper;
import smart.lib.session.Session;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * http interceptor
 */
@SuppressWarnings("NullableProblems")
public class HttpInterceptor implements HandlerInterceptor {
    /**
     * initial global parameters
     * @param request req
     * @param response res
     * @param handler handler
     * @return true
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        Session session = new Session(request, response);
        request.setAttribute(Helper.camelCase(Session.class), session);
        request.setAttribute(Helper.camelCase(UserToken.class), UserToken.from(session));

        request.setAttribute("articleList", ArticleCache.getList());
        request.setAttribute("beian", SystemCache.getBeian());
        request.setAttribute("jsPath", SystemCache.getJsPath());
        request.setAttribute("siteName", SystemCache.getSiteName());
        request.setAttribute("siteUrl", SystemCache.getUrl());
        request.setAttribute("categoryNodes", CategoryCache.getNodes());
        request.setAttribute("keywords", SystemCache.getKeywords());
        request.setAttribute("now", new Date());
        return true;
    }
}
