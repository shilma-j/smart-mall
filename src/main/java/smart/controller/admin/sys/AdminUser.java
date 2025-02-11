package smart.controller.admin.sys;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.config.AppConfig;
import smart.entity.AdminRoleEntity;
import smart.entity.UserEntity;
import smart.lib.*;
import smart.repository.AdminRoleRepository;
import smart.repository.UserRepository;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import smart.util.Helper;
import smart.util.Validate;

import java.util.HashMap;
import java.util.Map;

@Controller(value = "admin/sys/adminUser")
@RequestMapping(path = "/admin/sys/adminUser")
@Transactional
public class AdminUser {

    @Resource
    AdminRoleRepository adminRoleRepository;

    @Resource
    UserRepository userRepository;

    @PostMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postDelete(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        UserEntity userEntity = userRepository.findByIdForWrite(id);
        long num = AppConfig.getJdbcTemplate().update("delete from t_admin_user where user_id = " + id);
        if (num == 0 || userEntity == null) {
            jsonResult.setMsg("该账号不存在,请刷新页面重试");
            return jsonResult.toString();
        }
        jsonResult.setMsg("已删除管理员账号: " + userEntity.getName());
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);
    }

    @GetMapping(value = "edit")
    public ModelAndView getEdit(HttpServletRequest request) {
        ModelAndView modelAndView;
        long id = Helper.longValue(request.getParameter("id"));
        String sql = """
                select u.id,
                       u.name as name,
                       au.role_id as roleId
                from t_admin_user au
                         left join t_user u on au.user_id = u.id
                where au.user_id = ?""";
        Map<String, Object> user;
        if (id > 0) {
            try {
                user = AppConfig.getJdbcTemplate().queryForMap(sql, id);
            } catch (DataAccessException e) {
                return AdminHelper.msgPage("用户不存在", "list", request);
            }
        } else {
            user = new HashMap<>();
            user.put("name", "");
            user.put("roleId", 0L);
        }

        var roles = adminRoleRepository.findAll();
        AdminRoleEntity adminRoleEntity = new AdminRoleEntity();
        adminRoleEntity.setId(0L);
        adminRoleEntity.setName("超级管理员");
        roles.add(0, adminRoleEntity);

        modelAndView = Helper.newModelAndView("admin/sys/adminUser/edit", request);
        modelAndView.addObject("title", "管理员");
        modelAndView.addObject("id", id);
        modelAndView.addObject("roles", roles);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEdit(HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        long roleId = Helper.longValue(request.getParameter("role"));
        if (roleId != 0 && adminRoleRepository.findById(roleId).orElse(null) == null) {
            jsonResult.setMsg("指定的角色不存在");
            return jsonResult.toString();
        }
        if (id > 0) {
            AppConfig.getJdbcTemplate().update("update t_admin_user set role_id = ? where user_id = ?", roleId, id);
            jsonResult.setMsg("更新成功");
            jsonResult.setUrl("list");
            return AdminHelper.msgPage(jsonResult, request);
        }

        // create administrator
        var name = request.getParameter("name");
        var msg = Validate.name(name, "账号");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        UserEntity userEntity = userRepository.findByName(name);
        if (userEntity == null) {
            jsonResult.setMsg("新建失败,该账号不存在");
            return jsonResult.toString();
        }
        try {
            AppConfig.getJdbcTemplate().update("insert into t_admin_user set user_id=?,role_id=?",
                    userEntity.getId(), roleId);
        } catch (DuplicateKeyException e) {
            jsonResult.setMsg("新建失败,该账号已存在");
            return jsonResult.toString();
        }
        jsonResult.setMsg("已完成新建管理员: " + name);
        jsonResult.setUrl("list");
        return AdminHelper.msgPage(jsonResult, request);

    }

    @GetMapping(value = "list")
    public ModelAndView getIndex(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("admin/sys/adminUser/list", request);
        modelAndView.addObject("title", "管理员列表");
        String sql = """
                select u.id,
                       u.name    as name,
                       au.role_id,
                       ar.name   as role_name
                from t_admin_user au
                         left join t_user u on au.user_id = u.id
                         left join t_admin_role ar on au.role_id = ar.id""";
        Pagination pagination = Pagination.newBuilder(sql).page(request).build();
        for (var row : pagination.getRows()) {
            long roleId = Helper.longValue(row.get("roleId"));
            if (roleId == 0) {
                row.put("roleName", "超级管理员");
            }
        }
        modelAndView.addObject("pagination", pagination);
        return modelAndView;
    }
}
