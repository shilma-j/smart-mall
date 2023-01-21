package smart.service;

import jakarta.annotation.Resource;
import smart.repository.SpecRepository;
import org.springframework.stereotype.Service;

@Service
public class SpecService {

    @Resource
    SpecRepository specRepository;

}
