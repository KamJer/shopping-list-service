package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log
public class WebSocketCategoryService extends WebsocketCustomService{

    private final CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    public WebSocketCategoryService(UserRepository userRepository, WebSocketDataHolder webSocketDataHolder, CategoryRepository categoryRepository) {
        super(userRepository, webSocketDataHolder);
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDto putCategory(CategoryDto categoryDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Category categoryToPut = DatabaseUtil.toCategory(getUserFromAuth(), categoryDto, LocalDateTime.now());
        categoryRepository.save(categoryToPut);
        categoryToPut.setLocalId(categoryDto.getLocalId());
        categoryToPut.setSavedTime(savedTime);
        return DatabaseUtil.toCategoryDto(categoryToPut, ModifyState.UPDATE, savedTime);
    }

    @Transactional
    public CategoryDto postCategory(CategoryDto categoryDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<Category> optionalCategory = categoryRepository.findCategoryByUserUserNameAndCategoryId(getUserFromAuth().getUserName(), categoryDto.getCategoryId());
        if (optionalCategory.isPresent()) {
            Category categoryToPost = optionalCategory.get();
            categoryToPost.setCategoryName(categoryDto.getCategoryName());
            categoryToPost.setDeleted(categoryDto.isDeleted());
            categoryToPost.setLocalId(categoryDto.getLocalId());
            categoryToPost.setSavedTime(savedTime);
            return DatabaseUtil.toCategoryDto(categoryToPost, ModifyState.UPDATE, savedTime);
        }
        return putCategory(categoryDto);
    }

    @Transactional
    public CategoryDto deleteCategory(CategoryDto categoryDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<Category> amountTypeOptional = categoryRepository.findCategoryByUserUserNameAndCategoryId(getUserFromAuth().getUserName(), categoryDto.getCategoryId());
        if (amountTypeOptional.isPresent()) {
            Category categoryToDelete = amountTypeOptional.get();
            categoryToDelete.setDeleted(categoryDto.isDeleted());
            categoryToDelete.setLocalId(categoryDto.getLocalId());
            categoryToDelete.setSavedTime(savedTime);
            return DatabaseUtil.toCategoryDto(categoryToDelete, ModifyState.DELETE, savedTime);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return DatabaseUtil.categoryDtoToCategoryDto(categoryDto, ModifyState.DELETE);
    }
}
