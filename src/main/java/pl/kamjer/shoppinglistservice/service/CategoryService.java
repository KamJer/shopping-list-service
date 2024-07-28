package pl.kamjer.shoppinglistservice.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CategoryService {
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    @PreAuthorize("#userName == authentication.principal.username")
    public List<CategoryDto> getCategoryByUser(String userName) throws NoResourcesFoundException {
        userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No Such User"));
        return categoryRepository.findAllByCategoryIdUserUserName(userName).stream().map(DatabaseUtil::toCategoryDto).collect(Collectors.toList());
    }

    @PreAuthorize("#categoryDto.userName == authentication.principal.username")
    public Long insertCategory(CategoryDto categoryDto) throws NoResourcesFoundException {
        return categoryRepository.save(DatabaseUtil.toCategory(userRepository, categoryDto)).getCategoryId().getCategoryId();
    }

    @PreAuthorize("#categoryDto.userName == authentication.principal.username")
    public void updateCategory(CategoryDto categoryDto) throws NoResourcesFoundException {
        DatabaseUtil.toCategoryDto(categoryRepository.save(DatabaseUtil.toCategory(userRepository, categoryDto)));
    }

    @PreAuthorize("#categoryDto.userName == authentication.principal.username")
    public void deleteCategory(CategoryDto categoryDto) throws NoResourcesFoundException {
        categoryRepository.delete(DatabaseUtil.toCategory(userRepository, categoryDto));
    }
}
