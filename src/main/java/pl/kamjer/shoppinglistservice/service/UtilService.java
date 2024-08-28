package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllIdDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UtilService extends CustomService {

    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    public UtilService(UserRepository userRepository,
                       AmountTypeRepository amountTypeRepository,
                       CategoryRepository categoryRepository,
                       ShoppingItemRepository shoppingItemRepository) {
        super(userRepository);
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
        this.shoppingItemRepository = shoppingItemRepository;
    }

    @Transactional
    public AllDto synchronizeDto(AllDto allDto) {
//        saving time and getting user
        LocalDateTime savedTime = LocalDateTime.now();
        User user = getUserFromAuth();

//        getting date and time from user, if time from user is null take the oldest possible time
        LocalDateTime userSavedTime = Optional.ofNullable(allDto.getSavedTime()).orElseGet(() -> LocalDateTime.of(1000, 1, 1, 0, 0));

//        getting data from dto and converting it to entity
        List<AmountType> amountTypesFromClient = allDto.getAmountTypeDtoList().stream()
                .map(amountTypeDto -> DatabaseUtil.toAmountType(user, amountTypeDto, savedTime))
                .toList();
        List<Category> categoriesFromClient = allDto.getCategoryDtoList().stream()
                .map(categoryDto -> DatabaseUtil.toCategory(user, categoryDto, savedTime))
                .toList();
        List<ShoppingItem> shoppingItemsFromClient = allDto.getShoppingItemDtoList().stream()
                .map(shoppingItemDto -> DatabaseUtil.toShoppingItem(user, amountTypeRepository, categoryRepository, shoppingItemDto, savedTime))
                .toList();

//        fetching data from database to compare to
        List<AmountType> amountTypesFromDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<Category> categoriesFromDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<ShoppingItem> shoppingItemsFromDb = shoppingItemRepository.findShoppingItemByShoppingItemIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);

        List<AmountTypeDto> amountTypesToCheck = amountTypesFromDb
                .stream()
                .filter(amountType -> {
                    if (amountType.isDeleted()) {
                        return amountTypesFromClient.contains(amountType);
                    }
                    return true;
                })
                .map(amountType -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (amountType.isDeleted() && amountTypesFromClient.contains(amountType)) {
                        modifyState = ModifyState.DELETE;
                    } else if (amountTypesFromClient.contains(amountType)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toAmountTypeDto(amountType, modifyState);
                })
                .toList();

        List<CategoryDto> categoriesToCheck = categoriesFromDb
                .stream()
                .filter(category -> {
                    if (category.isDeleted()) {
                        return categoriesFromClient.contains(category);
                    }
                    return true;
                })
                .map(category -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (category.isDeleted() && categoriesFromClient.contains(category)) {
                        modifyState = ModifyState.DELETE;
                    } else if (categoriesFromClient.contains(category)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toCategoryDto(category, modifyState);
                })
                .toList();

        List<ShoppingItemDto> shoppingItemsToCheck = shoppingItemsFromDb
                .stream()
                .filter(shoppingItem -> {
                    if (shoppingItem.isDeleted()) {
                        return shoppingItemsFromClient.contains(shoppingItem);
                    }
                    return true;
                })
                .map(shoppingItem -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (shoppingItem.isDeleted() && shoppingItemsFromClient.contains(shoppingItem)) {
                        modifyState = ModifyState.DELETE;
                    } else if (shoppingItemsFromClient.contains(shoppingItem)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toShoppingItemDto(shoppingItem, modifyState);
                })
                .toList();

//        because server does not delete data only saves it as deleted this code also "deletes" data
        List<AmountType> amountTypesFromClientAfterFilter = amountTypesFromClient.stream()
                .map(amountType -> {
                    AmountType amountTypeDb = amountType;
                    if (amountType.getAmountTypeId().getAmountTypeId() != 0L) {
                        amountTypeDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(
                                        user.getUserName(),
                                        amountType.getAmountTypeId().getAmountTypeId())
                                .map(amountType1 -> {
                                    amountType1.setAmountTypeId(amountType.getAmountTypeId());
                                    amountType1.setTypeName(amountType.getTypeName());
                                    amountType1.setDeleted(amountType.isDeleted());
                                    amountType1.setSavedTime(amountType.getSavedTime());
                                    return amountType1;
                                }).orElse(amountType);
                    }
                    amountTypeDb = amountTypeRepository.save(amountTypeDb);
                    amountTypeDb.setLocalId(amountType.getLocalId());
                    return amountTypeDb;
                })
                .toList();
        List<AmountTypeDto> amountTypeDtoToSend = new ArrayList<>(amountTypesFromClientAfterFilter
                .stream()
                .map(amountType -> {
                    AmountTypeDto amountTypeDto;
                    if (amountType.isDeleted()) {
                        amountTypeDto = DatabaseUtil.toAmountTypeDto(amountType, ModifyState.DELETE);
                    } else {
                        amountTypeDto = DatabaseUtil.toAmountTypeDto(amountType, ModifyState.UPDATE);
                    }
                    return amountTypeDto;
                })
                .toList());
        amountTypeDtoToSend.addAll(amountTypesToCheck);

        List<Category> categoriesFromClientAfterFilter = categoriesFromClient.stream()
                .map(category -> {
                    Category categoryDb = category;
                    if (category.getCategoryId().getCategoryId() != 0L) {
                        categoryDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(
                                        user.getUserName(),
                                        category.getCategoryId().getCategoryId())
                                .map(category1 -> {
                                    category1.setCategoryId(category.getCategoryId());
                                    category1.setCategoryName(category.getCategoryName());
                                    category1.setDeleted(category.isDeleted());
                                    category1.setSavedTime(category.getSavedTime());
                                    return category1;
                                }).orElse(category);
                    }
                    categoryDb = categoryRepository.save(categoryDb);
                    categoryDb.setLocalId(category.getLocalId());
                    return categoryDb;

                })
                .toList();
        List<CategoryDto> categoryDtoToSend = new ArrayList<>(categoriesFromClientAfterFilter
                .stream()
                .map(category -> {
                    CategoryDto categoryDto;
                    if (category.isDeleted()) {
                        categoryDto = DatabaseUtil.toCategoryDto(category, ModifyState.DELETE);
                    } else {
                        categoryDto = DatabaseUtil.toCategoryDto(category, ModifyState.UPDATE);
                    }
                    return categoryDto;
                })
                .toList());
        categoryDtoToSend.addAll(categoriesToCheck);

        List<ShoppingItem> shoppingItemsFromClientAfterFilter = shoppingItemsFromClient.stream()
                .map(shoppingItem -> {
                    ShoppingItem shoppingItemDb = shoppingItem;
                    if (shoppingItem.getShoppingItemId().getShoppingItemId() != 0L) {
                        shoppingItemDb = shoppingItemRepository.findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(user.getUserName(), shoppingItem.getShoppingItemId().getShoppingItemId())
                                .orElseThrow(() -> new NoResourcesFoundException("no such ShoppingItem:" + shoppingItem.getShoppingItemId()));
                        AmountType amountTypeDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), shoppingItem.getItemAmountType().getAmountTypeId().getAmountTypeId())
                                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType:" + shoppingItem.getItemAmountType().getAmountTypeId()));
                        Category categoryDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), shoppingItem.getItemCategory().getCategoryId().getCategoryId())
                                .orElseThrow(() -> new NoResourcesFoundException("no such Category:" + shoppingItem.getItemCategory().getCategoryId()));
                        shoppingItemDb.setItemAmountType(amountTypeDb);
                        shoppingItemDb.setItemCategory(categoryDb);
                        shoppingItemDb.setItemName(shoppingItem.getItemName());
                        shoppingItemDb.setBought(shoppingItem.isBought());
                        shoppingItemDb.setAmount(shoppingItem.getAmount());
                        shoppingItemDb.setSavedTime(shoppingItem.getSavedTime());
                        shoppingItemDb.setDeleted(shoppingItem.isDeleted());
                    }
                    ShoppingItem insertedShoppingItem = shoppingItemRepository.save(shoppingItemDb);
                    insertedShoppingItem.setLocalShoppingItemId(shoppingItem.getLocalShoppingItemId());
                    insertedShoppingItem.setLocalCategoryId(shoppingItem.getLocalCategoryId());
                    insertedShoppingItem.setLocalAmountTypeId(shoppingItem.getLocalAmountTypeId());
                    return insertedShoppingItem;
                })
                .toList();
        List<ShoppingItemDto> shoppingItemToSend = new ArrayList<>((shoppingItemsFromClientAfterFilter)
                .stream()
                .map(shoppingItem -> {
                    ShoppingItemDto shoppingItemDto;
                    if (shoppingItem.isDeleted()) {
                        shoppingItemDto = DatabaseUtil.toShoppingItemDto(shoppingItem, ModifyState.DELETE);
                    } else {
                        shoppingItemDto = DatabaseUtil.toShoppingItemDto(shoppingItem, ModifyState.UPDATE);
                    }
                    return shoppingItemDto;
                })
                .toList());
        shoppingItemToSend.addAll(shoppingItemsToCheck);

        user.setSavedTime(savedTime);
        userRepository.save(user);

        return AllDto.builder()
                .amountTypeDtoList(amountTypeDtoToSend)
                .categoryDtoList(categoryDtoToSend)
                .shoppingItemDtoList(shoppingItemToSend)
                .savedTime(savedTime)
                .build();
    }
}
