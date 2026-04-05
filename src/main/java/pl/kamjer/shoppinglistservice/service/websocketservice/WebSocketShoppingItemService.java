package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapper;
import pl.kamjer.shoppinglistservice.mapping.ShoppingItemResolver;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Service
public class WebSocketShoppingItemService extends WebsocketCustomService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingEntityMapper shoppingEntityMapper;
    private final ShoppingItemResolver shoppingItemResolver;

    public WebSocketShoppingItemService(SecClient secClient,
                                        WebSocketDataHolder webSocketDataHolder,
                                        ShoppingItemRepository shoppingItemRepository,
                                        AmountTypeRepository amountTypeRepository,
                                        CategoryRepository categoryRepository,
                                        ObjectMapper objectMapper,
                                        ShoppingEntityMapper shoppingEntityMapper,
                                        ShoppingItemResolver shoppingItemResolver) {
        super(webSocketDataHolder, secClient, objectMapper);
        this.shoppingItemRepository = shoppingItemRepository;
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
        this.shoppingEntityMapper = shoppingEntityMapper;
        this.shoppingItemResolver = shoppingItemResolver;
    }

    @Transactional
    public ShoppingItemDto putShoppingItem(ShoppingItemDto shoppingItemDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        ShoppingItem shoppingItem =
                shoppingItemResolver.resolve(user, new HashMap<>(), new HashMap<>(), shoppingItemDto, savedTime);
        shoppingItemRepository.save(shoppingItem);
        shoppingItem.setLocalShoppingItemId(shoppingItemDto.getLocalId());
        shoppingItem.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
        shoppingItem.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
        shoppingItem.setSavedTime(savedTime);
        user.setSavedTime(savedTime);
        secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
        return shoppingEntityMapper.toShoppingItemDto(shoppingItem, ModifyState.UPDATE, savedTime);
    }

    @Transactional
    public ShoppingItemDto postShoppingItem(ShoppingItemDto shoppingItemDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<ShoppingItem> shoppingItemOptional = shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId(user.getUserName(), shoppingItemDto.getShoppingItemId());
        if (shoppingItemOptional.isPresent()) {
            Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId());
            Optional<Category> categoryOptional = categoryRepository.findCategoryByUserNameAndCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId());
            if (amountTypeOptional.isPresent() && categoryOptional.isPresent()) {
                ShoppingItem shoppingItem = shoppingItemOptional.get();
                shoppingItem.setItemName(shoppingItemDto.getItemName());
                shoppingItem.setItemCategory(categoryOptional.get());
                shoppingItem.setItemAmountType(amountTypeOptional.get());
                shoppingItem.setAmount(shoppingItemDto.getAmount());
                shoppingItem.setDeleted(shoppingItemDto.isDeleted());
                shoppingItem.setBought(shoppingItemDto.isBought());
                shoppingItem.setSavedTime(savedTime);
                shoppingItem.setLocalShoppingItemId(shoppingItemDto.getLocalId());
                shoppingItem.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
                shoppingItem.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
                shoppingItem.setSavedTime(savedTime);
                user.setSavedTime(savedTime);
                secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
                return shoppingEntityMapper.toShoppingItemDto(shoppingItem, ModifyState.UPDATE, savedTime);
            } else {
                throw new NoResourcesFoundException("Such Amount Type or Category does not exist");
            }
        } else {
            return putShoppingItem(shoppingItemDto);
        }
    }

    @Transactional
    public ShoppingItemDto deleteShoppingItem(ShoppingItemDto shoppingItemDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<ShoppingItem> shoppingItemOptional = shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId(user.getUserName(), shoppingItemDto.getShoppingItemId());
        if (shoppingItemOptional.isPresent()) {
            ShoppingItem shoppingItemToDelete = shoppingItemOptional.get();
            shoppingItemToDelete.setDeleted(shoppingItemDto.isDeleted());
            shoppingItemToDelete.setLocalShoppingItemId(shoppingItemDto.getLocalId());
            shoppingItemToDelete.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
            shoppingItemToDelete.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
            shoppingItemToDelete.setSavedTime(savedTime);
            user.setSavedTime(savedTime);
            secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
            return shoppingEntityMapper.toShoppingItemDto(shoppingItemToDelete, ModifyState.DELETE);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return shoppingEntityMapper.copyShoppingItemDto(shoppingItemDto, ModifyState.DELETE, savedTime);

    }
}
