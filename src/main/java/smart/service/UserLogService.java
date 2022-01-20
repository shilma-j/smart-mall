package smart.service;

import smart.repository.UserLogRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserLogService {

    @Resource
    private UserLogRepository userLogRepository;
}
