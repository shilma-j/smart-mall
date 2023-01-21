package smart.config;

import smart.authentication.AdminAuthException;
import smart.authentication.UserAuthException;
import smart.lib.AdminHelper;
import smart.lib.Helper;
import smart.lib.Json;
import smart.lib.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class.getName());

    /**
     * generate error response entity
     *
     * @return response entity
     */
    private ResponseEntity<String> getResponseEntity(HttpServletRequest request, HttpStatus httpStatus) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String content;
        if ("application/json".equals(request.getHeader("content-type"))) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("timestamp", System.currentTimeMillis());
            map.put("status", httpStatus.value());
            map.put("error", httpStatus.getReasonPhrase());
            map.put("path", request.getRequestURI());
            content = Json.encode(map);
        } else {
            httpHeaders.setContentType(MediaType.TEXT_HTML);
            content = Helper.getErrorHtml(httpStatus);
        }
        return new ResponseEntity<>(
                content,
                httpHeaders,
                httpStatus);
    }

    /**
     * 管理员鉴权异常
     *
     * @param request  request
     * @param response response
     * @param e        exception
     * @return 异常处理结果
     */
    @ExceptionHandler(AdminAuthException.class)
    public ModelAndView adminAuthExceptionHandler(HttpServletRequest request, HttpServletResponse response, AdminAuthException e) {
        if (e.isDenied()) {
            if (request.getMethod().equals("GET")) {
                return AdminHelper.msgPage("无此权限", "", request);
            } else {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                JsonResult jsonResult = new JsonResult();
                jsonResult.setMsg("无此权限");
                try {
                    response.getWriter().write(jsonResult.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            String loginUrl = "/admin/login";
            if (request.getMethod().equals("GET")) {
                loginUrl += "?back=" + Helper.urlEncode(request.getRequestURI());
                try {
                    response.sendRedirect(loginUrl);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                JsonResult jsonResult = new JsonResult();
                jsonResult.setUrl(loginUrl);
                try {
                    response.getWriter().write(jsonResult.toString());
                    response.getWriter().write(jsonResult.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }
        return null;
    }

    /**
     * 引用error/405.html会出现WARN日志
     * 捕获异常自定义处理不会出现WARN日志
     *
     * @return METHOD_NOT_ALLOWED info
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<String> httpRequestMethodNotSupportedExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 上传文件名包含\u0000等字符串时会引发该错误
     *
     * @return 400 error info
     */
    @ExceptionHandler(InvalidFileNameException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> invalidFileNameExceptionExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.BAD_REQUEST);
    }

    /**
     * 上传文件过大时会引发该错误
     *
     * @return 413 error info
     */
    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<String> sizeLimitExceededExceptionExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(UserAuthException.class)
    public void userAuthExceptionHandler(HttpServletRequest request, HttpServletResponse response, UserAuthException e) {
        String loginUri = "/user/login";
        if (request.getMethod().equals("GET")) {
            loginUri += "?back=" + Helper.urlEncode(request.getRequestURI());
            String queryString = request.getQueryString();
            if (queryString != null) {
                loginUri += Helper.urlEncode('?' + queryString);
            }
            try {
                response.sendRedirect(loginUri);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            JsonResult jsonResult = new JsonResult();
            jsonResult.setUrl(loginUri);
            try {
                response.getWriter().write(jsonResult.toString());
                response.getWriter().write(jsonResult.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
