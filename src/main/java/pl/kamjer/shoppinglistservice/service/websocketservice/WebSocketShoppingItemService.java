package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WebSocketShoppingItemService extends WebsocketCustomService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;

    public WebSocketShoppingItemService(SecClient secClient, WebSocketDataHolder webSocketDataHolder, ShoppingItemRepository shoppingItemRepository, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository) {
        super(webSocketDataHolder, secClient);
        this.shoppingItemRepository = shoppingItemRepository;
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ShoppingItemDto putShoppingItem(ShoppingItemDto shoppingItemDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        ShoppingItem shoppingItem = DatabaseUtil.toShoppingItem(getUserFromAuth(), amountTypeRepository, categoryRepository, shoppingItemDto, LocalDateTime.now());
        shoppingItemRepository.save(shoppingItem);
        shoppingItem.setLocalShoppingItemId(shoppingItemDto.getLocalId());
        shoppingItem.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
        shoppingItem.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
        shoppingItem.setSavedTime(savedTime);
        getUserFromAuth().setSavedTime(savedTime);
        return DatabaseUtil.toShoppingItemDto(shoppingItem, ModifyState.UPDATE, savedTime);

    }

    @Transactional
    public ShoppingItemDto postShoppingItem(ShoppingItemDto shoppingItemDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<ShoppingItem> shoppingItemOptional = shoppingItemRepository.findShoppingItemByUserUserNameAndShoppingItemId(getUserFromAuth().getUserName(), shoppingItemDto.getShoppingItemId());
        if (shoppingItemOptional.isPresent()) {
            Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserUserNameAndAmountTypeId(getUserFromAuth().getUserName(), shoppingItemDto.getItemAmountTypeId());
            Optional<Category> categoryOptional = categoryRepository.findCategoryByUserUserNameAndCategoryId(getUserFromAuth().getUserName(), shoppingItemDto.getItemCategoryId());
            if (amountTypeOptional.isPresent() && categoryOptional.isPresent()) {
                ShoppingItem shoppingItem = shoppingItemOptional.get();
                shoppingItem.setItemName(shoppingItemDto.getItemName());
                shoppingItem.setItemCategory(categoryOptional.get());
                shoppingItem.setItemAmountType(amountTypeOptional.get());
                shoppingItem.setAmount(shoppingItemDto.getAmount());
                shoppingItem.setDeleted(shoppingItemDto.isDeleted());
                shoppingItem.setBought(shoppingItemDto.isBought());
                shoppingItem.setSavedTime(LocalDateTime.now());
                shoppingItem.setLocalShoppingItemId(shoppingItemDto.getLocalId());
                shoppingItem.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
                shoppingItem.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
                shoppingItem.setSavedTime(savedTime);
                getUserFromAuth().setSavedTime(savedTime);
                return DatabaseUtil.toShoppingItemDto(shoppingItem, ModifyState.UPDATE, savedTime);
            } else {
                throw new NoResourcesFoundException("Such Amount Type or Category does not exist");
            }
        } else {
            return putShoppingItem(shoppingItemDto);
        }
    }

    @Transactional
    public ShoppingItemDto deleteShoppingItem(ShoppingItemDto shoppingItemDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        getUserFromAuth().setSavedTime(savedTime);
        Optional<ShoppingItem> amountTypeOptional = shoppingItemRepository.findShoppingItemByUserUserNameAndShoppingItemId(getUserFromAuth().getUserName(), shoppingItemDto.getShoppingItemId());
        if (amountTypeOptional.isPresent()) {
            ShoppingItem amountTypeToDelete = amountTypeOptional.get();
            amountTypeToDelete.setDeleted(shoppingItemDto.isDeleted());
            amountTypeToDelete.setLocalShoppingItemId(shoppingItemDto.getLocalId());
            amountTypeToDelete.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
            amountTypeToDelete.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
            amountTypeToDelete.setSavedTime(savedTime);
            return DatabaseUtil.toShoppingItemDto(amountTypeToDelete, ModifyState.DELETE);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return DatabaseUtil.fromShoppingItemDtoToShoppingItemDto(shoppingItemDto, ModifyState.DELETE, savedTime);

    }
}
