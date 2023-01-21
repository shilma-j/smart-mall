package smart.authentication;

import jakarta.annotation.Resource;
import smart.config.AdminAuthority;
import smart.config.AdminMenu;
import smart.lib.Helper;
import smart.repository.AdminUserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AdminAuth extends Auth {

    @Resource
    AdminUserRepository adminUserRepository;

    @Pointcut("execution(public * smart.controller.admin..*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void auth(JoinPoint joinPoint) throws AdminAuthException, UserAuthException {
        HttpServletRequest request = getRequest();
        UserToken userToken = (UserToken) request.getAttribute(Helper.camelCase(UserToken.class));
        if (userToken == null) {
            throw new UserAuthException();
        }
        var adminUserEntity = adminUserRepository.findById(userToken.getId()).orElse(null);
        if (adminUserEntity == null) {
            throw new AdminAuthException(false);
        }
        AdminAuthority adminAuthority = new AdminAuthority(adminUserEntity.getRoleId());
        String curAction = request.getMethod().toLowerCase() + '@' + request.getServletPath();
        if (!adminAuthority.actions.contains(curAction)
                && !curAction.equals("get@/admin/msg")
                && !curAction.equals("get@/admin")
                && !curAction.equals("get@/admin/")) {
            throw new AdminAuthException(true);
        }
        AdminMenu adminMenu = new AdminMenu(adminAuthority.actions);
        request.setAttribute("adminMenu", adminMenu.menu);
    }
}
