package pl.kamjer.shoppinglistservice.service.websocketservice;

import lombok.Setter;
import lombok.extern.java.Log;
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
    public WebSocketCategoryService(UserRepository userRepository, WebSocketDataHolder webSocketDataHolder, CategoryRepository categoryRepository) {
        super(userRepository, webSocketDataHolder);
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto putCategory(CategoryDto categoryDto) {
        Category categoryToPut = DatabaseUtil.toCategory(getUserFromAuth(), categoryDto, LocalDateTime.now());
        categoryRepository.save(categoryToPut);
        return DatabaseUtil.toCategoryDto(categoryToPut, ModifyState.UPDATE);
    }

    public CategoryDto postCategory(CategoryDto categoryDto) {
        Optional<Category> optionalCategory = categoryRepository.findCategoryByUserUserNameAndCategoryId(getUserFromAuth().getUserName(), categoryDto.getCategoryId());
        if (optionalCategory.isPresent()) {
            Category categoryToPost = optionalCategory.get();
            categoryToPost.setCategoryName(categoryDto.getCategoryName());
            categoryToPost.setDeleted(categoryDto.isDeleted());
            categoryToPost.setSavedTime(LocalDateTime.now());
            return DatabaseUtil.toCategoryDto(categoryToPost, ModifyState.UPDATE);
        }
        return putCategory(categoryDto);
    }

    public CategoryDto deleteCategory(CategoryDto categoryDto) {
        Optional<Category> amountTypeOptional = categoryRepository.findCategoryByUserUserNameAndCategoryId(getUserFromAuth().getUserName(), categoryDto.getCategoryId());
        if (amountTypeOptional.isPresent()) {
            Category amountTypeToDelete = amountTypeOptional.get();
            amountTypeToDelete.setDeleted(categoryDto.isDeleted());
            return DatabaseUtil.toCategoryDto(amountTypeToDelete, ModifyState.DELETE);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return DatabaseUtil.categoryDtoToCategoryDto(categoryDto, ModifyState.DELETE);
    }
}
