package smart.service;

import smart.repository.AdminRoleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AdminRoleService {
    @Resource
    AdminRoleRepository adminRoleRepository;

    public static boolean hasRule(String ruleName) {
        return false;
    }
}
