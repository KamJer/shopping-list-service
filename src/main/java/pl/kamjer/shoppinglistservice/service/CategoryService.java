package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CategoryService {
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

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
        categoryRepository.save(DatabaseUtil.toCategory(user, categoryDto, savedTime));
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
        categoryRepository.save(categoryToDelete);
        return savedTime;
    }

    private User updateSaveTimeInUser(LocalDateTime localDateTime) throws NoResourcesFoundException {
        User user = getUserFromAuth();
        user.setSavedTime(localDateTime);
        userRepository.save(user);
        return user;
    }

    private User getUserFromAuth() throws NoResourcesFoundException {
        String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }
}
