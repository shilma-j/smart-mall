package smart.service;

import jakarta.annotation.Resource;
import smart.repository.AdminRoleRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminRoleService {
    @Resource
    AdminRoleRepository adminRoleRepository;

    public static boolean hasRule(String ruleName) {
        return false;
    }
}
