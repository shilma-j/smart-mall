package smart.controller.admin.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import smart.entity.UserEntity;
import smart.lib.AdminHelper;
import smart.util.Helper;
import smart.lib.Pagination;
import smart.util.Validate;
import smart.repository.UserRepository;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Controller(value = "admin/user/user")
@RequestMapping(path = "/admin/user/user")
@Transactional
public class User {

    @Resource
    UserRepository userRepository;

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        long id = Helper.longValue(request.getParameter("id"));
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return AdminHelper.msgPage("用户不存在", "/admin/user/user/list", request);
        }
        ModelAndView modelAndView = Helper.newModelAndView("admin/user/user/edit", request);
        modelAndView.addObject("title", "用户信息");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request,
                                 @RequestParam(required = false) String name) {
        long page = Helper.longValue(request.getParameter("page"), 1);
        String sqlWhere = "";
        Set<Object> params = new LinkedHashSet<>();
        Map<String, String> query = new HashMap<>();


        if (name != null) {
            name = name.trim();
            if (name.length() > 0) {
                if (Validate.letterOrNumber(name, "") == null) {
                    sqlWhere += " and name like ?";
                    params.add('%' + name + '%');
                } else {
                    sqlWhere += " and false";
                }

                query.put("name", name);
            }
        }
        if (sqlWhere.startsWith(" and ")) {
            sqlWhere = " where " + sqlWhere.substring(4);
        }
        Pagination pagination = Pagination.newBuilder("select * from t_user" + sqlWhere + " order by id desc", params.toArray())
                .page(request).query(query).build();
        ModelAndView modelAndView = Helper.newModelAndView("admin/user/user/list", request);
        modelAndView.addObject("title", "会员列表");
        modelAndView.addObject("name", name);
        modelAndView.addObject("pagination", pagination);
        modelAndView.addObject("rows", pagination.getRows(UserEntity.class));
        return modelAndView;
    }

}
