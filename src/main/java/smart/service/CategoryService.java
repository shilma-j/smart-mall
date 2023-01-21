package smart.service;

import jakarta.annotation.Resource;
import smart.entity.CategoryEntity;
import smart.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Resource
    CategoryRepository categoryRepository;

    public List<CategoryEntity> findAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "parentId")
                .and(Sort.by(Sort.Direction.DESC, "recommend"))
                .and(Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(sort);
    }
}
