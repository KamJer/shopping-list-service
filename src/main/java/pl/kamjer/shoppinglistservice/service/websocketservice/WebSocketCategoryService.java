package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapper;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j2
public class WebSocketCategoryService extends WebsocketCustomService {

    private final CategoryRepository categoryRepository;
    private final ShoppingEntityMapper shoppingEntityMapper;

    public WebSocketCategoryService(SecClient secClient,
                                    WebSocketDataHolder webSocketDataHolder,
                                    CategoryRepository categoryRepository,
                                    ObjectMapper objectMapper,
                                    ShoppingEntityMapper shoppingEntityMapper) {
        super(webSocketDataHolder, secClient, objectMapper);
        this.categoryRepository = categoryRepository;
        this.shoppingEntityMapper = shoppingEntityMapper;
    }

    @Transactional
    public CategoryDto putCategory(CategoryDto categoryDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Category categoryToPut = shoppingEntityMapper.toCategory(user, categoryDto, savedTime);
        categoryRepository.save(categoryToPut);
        categoryToPut.setLocalId(categoryDto.getLocalId());
        categoryToPut.setSavedTime(savedTime);
        user.setSavedTime(savedTime);
        secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
        return shoppingEntityMapper.toCategoryDto(categoryToPut, ModifyState.UPDATE, savedTime);
    }

    @Transactional
    public CategoryDto postCategory(CategoryDto categoryDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<Category> optionalCategory =
                categoryRepository.findCategoryByUserNameAndCategoryId(user.getUserName(), categoryDto.getCategoryId());
        if (optionalCategory.isPresent()) {
            Category categoryToPost = optionalCategory.get();
            categoryToPost.setCategoryName(categoryDto.getCategoryName());
            categoryToPost.setDeleted(categoryDto.isDeleted());
            categoryToPost.setLocalId(categoryDto.getLocalId());
            categoryToPost.setSavedTime(savedTime);
            user.setSavedTime(savedTime);
            secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
            return shoppingEntityMapper.toCategoryDto(categoryToPost, ModifyState.UPDATE, savedTime);
        }
        return putCategory(categoryDto);
    }

    @Transactional
    public CategoryDto deleteCategory(CategoryDto categoryDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<Category> categoryOptional =
                categoryRepository.findCategoryByUserNameAndCategoryId(user.getUserName(), categoryDto.getCategoryId());
        if (categoryOptional.isPresent()) {
            Category categoryToDelete = categoryOptional.get();
            categoryToDelete.setDeleted(categoryDto.isDeleted());
            categoryToDelete.setLocalId(categoryDto.getLocalId());
            categoryToDelete.setSavedTime(savedTime);
            categoryToDelete.getShoppingItemList().forEach(shoppingItem -> shoppingItem.setDeleted(true));
            user.setSavedTime(savedTime);
            secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
            return shoppingEntityMapper.toCategoryDto(categoryToDelete, ModifyState.DELETE, savedTime);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return shoppingEntityMapper.copyCategoryDto(categoryDto, ModifyState.DELETE);
    }
}
