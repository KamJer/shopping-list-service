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
        List<AmountTypeDto> amountTypeDtosFromClientProcessed = (allDto.getAmountTypeDtoList())
                .stream()
                .map(amountTypeDto -> {
                    switch (amountTypeDto.getModifyState()) {
                        case INSERT -> {
                            AmountType amountTypeFromDb = amountTypeRepository.save(DatabaseUtil.toAmountType(getUserFromAuth(), amountTypeDto, savedTime));
                            amountTypeFromDb.setLocalId(amountTypeDto.getLocalId());
                            return DatabaseUtil.toAmountTypeDto(amountTypeFromDb, ModifyState.UPDATE);
                        }
                        case UPDATE -> {
                            amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(
                                            user.getUserName(), amountTypeDto.getAmountTypeId())
                                    .ifPresent(amountType1 -> {
                                        amountType1.setTypeName(amountTypeDto.getTypeName());
                                        amountType1.setDeleted(amountTypeDto.isDeleted());
                                        amountType1.setSavedTime(savedTime);
                                    });
//                            return null, it does not need to be returned, client already have current version
                            return null;
                        }
                        case DELETE -> {
                            return DatabaseUtil.toAmountTypeDto(amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(
                                            user.getUserName(), amountTypeDto.getAmountTypeId())
                                    .map(amountType1 -> {
                                        amountType1.setTypeName(amountTypeDto.getTypeName());
                                        amountType1.setDeleted(amountTypeDto.isDeleted());
                                        amountType1.setSavedTime(savedTime);
                                        return amountType1;
                                    }).orElseThrow(), ModifyState.DELETE);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        List<CategoryDto> categoryDtosFromClientProcessed = (allDto.getCategoryDtoList())
                .stream()
                .map(categoryDto -> {
                    switch (categoryDto.getModifyState()) {
                        case INSERT -> {
                            Category categoryFromDb = categoryRepository.save(DatabaseUtil.toCategory(getUserFromAuth(), categoryDto, savedTime));
                            categoryFromDb.setLocalId(categoryDto.getLocalId());
                            return DatabaseUtil.toCategoryDto(categoryFromDb, ModifyState.UPDATE);
                        }
                        case UPDATE -> {
                            categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(
                                            user.getUserName(), categoryDto.getCategoryId())
                                    .ifPresent(category -> {
                                        category.setCategoryName(categoryDto.getCategoryName());
                                        category.setDeleted(categoryDto.isDeleted());
                                        category.setSavedTime(savedTime);
                                    });
//                            return null, it does not need to be returned, client already have current version
                            return null;
                        }
                        case DELETE -> {
                            return DatabaseUtil.toCategoryDto(categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(
                                            user.getUserName(), categoryDto.getCategoryId())
                                    .map(category -> {
                                        category.setCategoryName(categoryDto.getCategoryName());
                                        category.setDeleted(categoryDto.isDeleted());
                                        category.setSavedTime(savedTime);
                                        return category;
                                    }).orElseThrow(), ModifyState.DELETE);
                        }
                    }
                    return null;
                })
//                filtering null values
                .filter(Objects::nonNull)
                .toList();

        List<ShoppingItemDto> shoppingItemDtosFromClientProcessed = (allDto.getShoppingItemDtoList())
                .stream()
                .map(shoppingItemDto -> {
                    switch (shoppingItemDto.getModifyState()) {
                        case INSERT -> {
                            ShoppingItem categoryFromDb = shoppingItemRepository.save(DatabaseUtil.toShoppingItem(getUserFromAuth(), amountTypeRepository, categoryRepository, shoppingItemDto, savedTime));
                            categoryFromDb.setLocalShoppingItemId(shoppingItemDto.getLocalId());
                            categoryFromDb.setLocalCategoryId(shoppingItemDto.getLocalCategoryId());
                            categoryFromDb.setLocalAmountTypeId(shoppingItemDto.getLocalAmountTypeId());
//                            returning update state so client will update with server ids
                            return DatabaseUtil.toShoppingItemDto(categoryFromDb, ModifyState.UPDATE);
                        }
                        case UPDATE -> {
                            shoppingItemRepository.findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(
                                            user.getUserName(), shoppingItemDto.getShoppingItemId())
                                    .ifPresent(hoppingItem -> {
//                                        finding relevant data
                                        AmountType amountTypeDb = amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId())
                                                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType:" + shoppingItemDto.getItemAmountTypeId()));
                                        Category categoryDb = categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId())
                                                .orElseThrow(() -> new NoResourcesFoundException("no such Category:" + shoppingItemDto.getItemCategoryId()));

                                        hoppingItem.setItemAmountType(amountTypeDb);
                                        hoppingItem.setItemCategory(categoryDb);
                                        hoppingItem.setBought(shoppingItemDto.isBought());
                                        hoppingItem.setAmount(shoppingItemDto.getAmount());
                                        hoppingItem.setItemName(shoppingItemDto.getItemName());
                                        hoppingItem.setDeleted(shoppingItemDto.isDeleted());
                                        hoppingItem.setSavedTime(savedTime);
                                    });
//                            return null, it does not need to be returned, client already have current version
                            return null;
                        }
                        case DELETE -> {
                            return DatabaseUtil.toShoppingItemDto(shoppingItemRepository.findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(
                                            user.getUserName(), shoppingItemDto.getShoppingItemId())
                                    .map(shoppingItem -> {
                                        shoppingItem.setDeleted(shoppingItemDto.isDeleted());
                                        shoppingItem.setSavedTime(savedTime);
                                        return shoppingItem;
                                    }).orElseThrow(), ModifyState.DELETE);
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
                    } else if (amountTypesFromClient.contains(amountType)) {
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
                    } else if (categoriesFromClient.contains(category)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toCategoryDto(category, modifyState);
                })
                .toList();

        List<ShoppingItemDto> shoppingItemsFromDataBaserProcessed = (shoppingItemsFromDb)
                .stream()
                .map(shoppingItem -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (shoppingItem.isDeleted()) {
                        modifyState = ModifyState.DELETE;
                    } else if (shoppingItemsFromClient.contains(shoppingItem)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toShoppingItemDto(shoppingItem, modifyState);
                })
                .toList();

        List<AmountTypeDto> amountTypeDtosToSend = new ArrayList<>();
        amountTypeDtosToSend.addAll(amountTypesFromDbProcessed);
//        amountTypeDtosToSend.addAll(amountTypeDtosFromClientProcessed);

        List<CategoryDto> categoryDtosToSend = new ArrayList<>();
        categoryDtosToSend.addAll(categoriesFromDatabaseProcessed);
//        categoryDtosToSend.addAll(categoryDtosFromClientProcessed);

        List<ShoppingItemDto> shoppingItemDtosToSend = new ArrayList<>();
        shoppingItemDtosToSend.addAll(shoppingItemsFromDataBaserProcessed);
//        shoppingItemDtosToSend.addAll(shoppingItemDtosFromClientProcessed);

        user.setSavedTime(savedTime);
        userRepository.save(user);

        return AllDto.builder()
                .amountTypeDtoList(amountTypeDtosToSend)
                .categoryDtoList(categoryDtosToSend)
                .shoppingItemDtoList(shoppingItemDtosToSend)
                .savedTime(savedTime)
                .build();
    }
}
