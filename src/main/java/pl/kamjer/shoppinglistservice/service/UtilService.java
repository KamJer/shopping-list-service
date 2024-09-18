package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        //        handling saving data from client
        List<AmountType> amountTypeDtosFromClientProcessed = (allDto.getAmountTypeDtoList())
                .stream()
                .map(amountTypeDto -> {
                    switch (amountTypeDto.getModifyState()) {
                        case INSERT -> {
                            AmountType amountTypeFromDb = amountTypeRepository.save(DatabaseUtil.toAmountType(getUserFromAuth(), amountTypeDto, savedTime));
                            amountTypeFromDb.setLocalId(amountTypeDto.getLocalId());
                            return amountTypeFromDb;
                        }
                        case UPDATE -> {
                            return amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(
                                            user.getUserName(), amountTypeDto.getAmountTypeId())
                                    .map(amountType1 -> {
                                        amountType1.setTypeName(amountTypeDto.getTypeName());
                                        amountType1.setDeleted(amountTypeDto.isDeleted());
                                        amountType1.setSavedTime(savedTime);
                                        return amountType1;
                                    }).orElseThrow();
                        }
                        case DELETE -> {
                            return amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(
                                            user.getUserName(), amountTypeDto.getAmountTypeId())
                                    .map(amountType1 -> {
                                        amountType1.setDeleted(amountTypeDto.isDeleted());
                                        amountType1.setSavedTime(savedTime);
                                        return amountType1;
                                    }).orElseThrow();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        List<Category> categoryDtosFromClientProcessed = (allDto.getCategoryDtoList())
                .stream()
                .map(categoryDto -> {
                    switch (categoryDto.getModifyState()) {
                        case INSERT -> {
                            Category categoryFromDb = categoryRepository.save(DatabaseUtil.toCategory(getUserFromAuth(), categoryDto, savedTime));
                            categoryFromDb.setLocalId(categoryDto.getLocalId());
                            return categoryFromDb;
                        }
                        case UPDATE -> {
                            return categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(
                                            user.getUserName(), categoryDto.getCategoryId())
                                    .map(category -> {
                                        category.setCategoryName(categoryDto.getCategoryName());
                                        category.setDeleted(categoryDto.isDeleted());
                                        category.setSavedTime(savedTime);
                                        return category;
                                    }).orElseThrow();
                        }
                        case DELETE -> {
                            return categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(
                                            user.getUserName(), categoryDto.getCategoryId())
                                    .map(category -> {
                                        category.setDeleted(categoryDto.isDeleted());
                                        category.setSavedTime(savedTime);
                                        return category;
                                    }).orElseThrow();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        List<ShoppingItem> shoppingItemDtosFromClientProcessed = (allDto.getShoppingItemDtoList())
                .stream()
                .map(shoppingItemDto -> {
                    switch (shoppingItemDto.getModifyState()) {
                        case INSERT -> {
                            ShoppingItem shoppingItemFromDb = shoppingItemRepository.save(DatabaseUtil.toShoppingItem(getUserFromAuth(), amountTypeRepository, categoryRepository, shoppingItemDto, savedTime));
                            shoppingItemFromDb.setLocalShoppingItemId(shoppingItemDto.getLocalId());
                            shoppingItemFromDb.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
                            shoppingItemFromDb.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
//                            returning update state so client will update with server ids
                            return shoppingItemFromDb;
                        }
                        case UPDATE -> {
                            return shoppingItemRepository.findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(
                                            user.getUserName(), shoppingItemDto.getShoppingItemId())
                                    .map(shoppingItem -> {
//                                        finding relevant data
                                        AmountType amountTypeDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId())
                                                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType:" + shoppingItemDto.getItemAmountTypeId()));
                                        Category categoryDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId())
                                                .orElseThrow(() -> new NoResourcesFoundException("no such Category:" + shoppingItemDto.getItemCategoryId()));

                                        shoppingItem.setItemAmountType(amountTypeDb);
                                        shoppingItem.setItemCategory(categoryDb);
                                        shoppingItem.setBought(shoppingItemDto.isBought());
                                        shoppingItem.setAmount(shoppingItemDto.getAmount());
                                        shoppingItem.setItemName(shoppingItemDto.getItemName());
                                        shoppingItem.setDeleted(shoppingItemDto.isDeleted());
                                        shoppingItem.setSavedTime(savedTime);
                                        return shoppingItem;
                                    }).orElseThrow();
                        }
                        case DELETE -> {
                            return shoppingItemRepository.findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(
                                            user.getUserName(), shoppingItemDto.getShoppingItemId())
                                    .map(shoppingItem -> {
                                        shoppingItem.setDeleted(shoppingItemDto.isDeleted());
                                        shoppingItem.setSavedTime(savedTime);
                                        return shoppingItem;
                                    }).orElseThrow();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        List<AmountType> amountTypesFromClient = allDto.getAmountTypeDtoList().stream()
                .map(amountTypeDto -> DatabaseUtil.toAmountType(user, amountTypeDto, savedTime))
                .toList();

        List<Category> categoriesFromClient = allDto.getCategoryDtoList().stream()
                .map(categoryDto -> DatabaseUtil.toCategory(user, categoryDto, savedTime))
                .toList();

        List<ShoppingItem> shoppingItemsFromClient = allDto.getShoppingItemDtoList().stream()
                .map(shoppingItemDto -> DatabaseUtil.toShoppingItem(user, amountTypeRepository, categoryRepository, shoppingItemDto, savedTime))
                .toList();

//        data from database (data user does not have) it needs to be inserted, updated or deleted from local database, server needs to figure that out
        List<AmountType> amountTypesFromDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<Category> categoriesFromDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<ShoppingItem> shoppingItemsFromDb = shoppingItemRepository.findShoppingItemByShoppingItemIdUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);

//        data after processing can be sent to a client
        List<AmountTypeDto> amountTypesFromDbProcessed = (amountTypesFromDb)
                .stream()
                .map(amountType -> {
                    ModifyState modifyState = ModifyState.INSERT;
//                    if entity flagged as deleted tell client to delete data
                    if (amountType.isDeleted()) {
                        modifyState = ModifyState.DELETE;
//                        if clients database contains that data but its timestamp happens later than the last
//                        contact client had with server it needs to be updated (data was updated)
                    } else if (amountTypesFromClient.contains(amountType) || amountTypeDtosFromClientProcessed.contains(amountType)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toAmountTypeDto(amountType, modifyState);
                })
                .toList();

        List<CategoryDto> categoriesFromDatabaseProcessed = (categoriesFromDb)
                .stream()
                .map(category -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (category.isDeleted()) {
                        modifyState = ModifyState.DELETE;
                    } else if (categoriesFromClient.contains(category) || categoryDtosFromClientProcessed.contains(category)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toCategoryDto(category, modifyState);
                })
                .toList();

        List<ShoppingItemDto> shoppingItemsFromDataBaseProcessed = (shoppingItemsFromDb)
                .stream()
                .map(shoppingItem -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (shoppingItem.isDeleted()) {
                        modifyState = ModifyState.DELETE;
                    } else if (shoppingItemsFromClient.contains(shoppingItem) || shoppingItemDtosFromClientProcessed.contains(shoppingItem)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toShoppingItemDto(shoppingItem, modifyState);
                })
                .toList();

        user.setSavedTime(savedTime);
        userRepository.save(user);

        return AllDto.builder()
                .amountTypeDtoList(amountTypesFromDbProcessed)
                .categoryDtoList(categoriesFromDatabaseProcessed)
                .shoppingItemDtoList(shoppingItemsFromDataBaseProcessed)
                .savedTime(savedTime)
                .build();
    }
}
