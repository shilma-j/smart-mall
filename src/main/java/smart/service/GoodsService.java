package smart.service;

import smart.repository.GoodsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsService {
    @Resource
    private GoodsRepository goodsRepository;
}
