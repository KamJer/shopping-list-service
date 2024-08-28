package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class CategoryService extends CustomService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        super(userRepository);
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public AddDto insertCategory(CategoryDto categoryDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        return AddDto.builder()
                .newId(categoryRepository.save(DatabaseUtil.toCategory(user, categoryDto, savedTime)).getCategoryId().getCategoryId())
                .savedTime(savedTime)
                .build();
    }

    @Transactional
    public LocalDateTime updateCategory(CategoryDto categoryDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        Category categoryToUpdate = categoryRepository
                .findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), categoryDto.getCategoryId())
                .orElseThrow(() -> new NoResourcesFoundException("No such Category found: " + user.getUserName() + ", " + categoryDto.getCategoryId()));
        categoryToUpdate.setCategoryName(categoryDto.getCategoryName());
        categoryToUpdate.setDeleted(categoryDto.isDeleted());
        categoryToUpdate.setSavedTime(savedTime);
        return savedTime;
    }

    @Transactional
    public LocalDateTime deleteCategory(Long categoryId) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        Category categoryToDelete = categoryRepository
                .findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), categoryId)
                .orElseThrow(() -> new NoResourcesFoundException("No such Category found: " + user.getUserName() + ", " + categoryId));
        categoryToDelete.setDeleted(true);
        return savedTime;
    }
}
