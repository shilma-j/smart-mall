package smart.controller.user;

import jakarta.annotation.Resource;
import smart.authentication.UserToken;
import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.entity.UserEntity;
import smart.lib.*;
import smart.lib.session.Session;
import smart.repository.UserRepository;
import smart.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller(value = "user/site")
@RequestMapping(path = "user")
@Transactional
public class Site {

    @Resource
    UserRepository userRepository;

    @Resource
    UserService userService;


    @GetMapping(path = "central")
    public ModelAndView getCentral(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/central", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 用户中心");
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }

    @GetMapping(path = "info")
    public ModelAndView getInfo(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/info", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 个人信息");
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }


    @PostMapping(path = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postInfo(
            HttpServletRequest request,
            Session session,
            UserToken userToken,
            @RequestParam(required = false) String phone
    ) {
        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = Validate.mobile(phone, "手机号");
        if (msg != null) {
            jsonResult.error.put("phone", msg);
        }

        if (jsonResult.error.size() == 0) {
            if (userRepository.updateInfo(userToken.getId(), phone) == 0) {
                jsonResult.setMsg("修改失败,请刷新重试");
            } else {
                jsonResult.setMsg("修改成功");
                jsonResult.setUrl("/user/central");
                userToken.setPhone(phone);
                userToken.save(session);

            }
        }
        return jsonResult.toString();
    }

    @GetMapping(path = "login")
    public ModelAndView getLogin(HttpServletRequest request) {
        if (request.getParameter("logout") != null) {
            userService.logout(request);
        }
        ModelAndView modelAndView = Helper.newModelAndView("user/login", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 用户登录");
        modelAndView.addObject("back", request.getParameter("back"));
        return modelAndView;
    }

    @PostMapping(path = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(HttpServletRequest request, Session session) {
        String back = request.getParameter("back");
        if (back == null || back.length() == 0) {
            back = "/";
        }
        Cart oldCart = null;
        if (UserToken.from(session) == null) {
            oldCart = new Cart(request);
        }
        JsonResult jsonResult = new JsonResult();
        var name = request.getParameter("name");
        var password = request.getParameter("password");
        UserService.Result result = userService.login(name, password, Helper.getClientIp(request));
        if (result.userEntity != null) {
            //login success
            UserToken userToken = new UserToken(result.userEntity);
            userToken.save(session);
            request.setAttribute(Helper.camelCase(UserToken.class), userToken);
            Cart cart = new Cart(request);
            if (oldCart != null) {
                oldCart.getItems().forEach(item -> cart.add(item.getGoodsId(), item.getSpecId(), item.getNum()));
                session.delete(Cart.NAME);
            }
            jsonResult.setUrl(back);

        } else {
            jsonResult.setMsg(result.error);
            jsonResult.error.putAll(result.errors);
        }
        return jsonResult.toString();

    }

    @GetMapping(path = "password")
    public ModelAndView getPassword(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("user/password", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 修改密码");
        return modelAndView;
    }

    @PostMapping(path = "password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postPassword(
            HttpServletRequest request,
            Session session,
            UserToken userToken,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String password1
    ) {
        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = Validate.password(oldPassword, "原密码");
        if (msg != null) {
            jsonResult.error.put("oldPassword", msg);
        }
        msg = Validate.password(password, "新密码");
        if (msg == null) {
            if (password.equals(oldPassword)) {
                jsonResult.error.put("password", "新旧密码相同");
            }
        } else {
            jsonResult.error.put("password", msg);
        }

        if (jsonResult.error.size() == 0 && !password.equals(password1)) {
            jsonResult.error.put("password1", "重复密码与新密码不一致");
        }
        if (jsonResult.error.size() == 0) {
            String hash = Security.sha3_256(oldPassword + userToken.getSalt());
            if (!userToken.getPassword().equals(hash)) {
                jsonResult.error.put("oldPassword", "原密码错误");
            }
        }
        if (jsonResult.error.size() == 0) {
            UserService userService = AppConfig.getContext().getBean(UserService.class);
            String salt = userService.editPassword(userToken.getId(), password, Helper.getClientIp(request));
            if (salt != null) {
                jsonResult.setMsg("修改成功");
                jsonResult.setUrl("/user/central");
                userToken.setPassword(Security.sha3_256(password + salt));
                userToken.setSalt(salt);
                userToken.save(session);
            } else {
                jsonResult.setMsg("修改失败,请刷新重试  ");
            }
        }
        return jsonResult.toString();
    }

    @GetMapping(path = "register")
    public ModelAndView getRegister(HttpServletRequest request) {
        var modelAndView = Helper.newModelAndView("user/register", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 注册新用户");
        return modelAndView;
    }

    @PostMapping(path = "register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(
            HttpServletRequest request,
            Session session,
            @RequestParam(required = false) String captcha,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String password1
    ) {
        if (name != null) {
            name = name.toLowerCase();
        }
        JsonResult result = new JsonResult();
        String msg = Validate.name(name, "用户名");
        if (msg != null) {
            result.setMsg(msg)
                    .error.put("name", msg);
        }

        msg = Validate.password(password, "密码");
        if (msg != null) {
            result.setMsg(msg)
                    .error.put("password", msg);
        } else if (!password.equals(password1)) {
            msg = "重复密码与原密码不一致";
            result.setMsg(msg)
                    .error.put("password1", msg);
        }
        msg = Validate.captcha(captcha, session, "验证码");
        if (msg != null) {
            result.setMsg(msg)
                    .error.put("captcha", msg);
        }

        if (result.error.size() > 0) {
            return result.toString();
        }

        msg = userService.register(name, password, Helper.getClientIp(request));

        if (msg == null) {
            Captcha.clear(session);
            UserEntity userEntity = userRepository.findByName(name);
            UserToken userToken = new UserToken(userEntity);
            userToken.save(session);
            request.setAttribute(Helper.camelCase(UserToken.class), userToken);
            result.setUrl("/user/central").setMsg("注册会员成功");
            return Helper.msgPage(result, request);
        } else {
            result.setMsg(msg)
                    .error.put("name", msg);
        }
        return result.toString();
    }

    @GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getTest(HttpServletRequest request) {

        return Json.encode(request.getTrailerFields());
    }

}
