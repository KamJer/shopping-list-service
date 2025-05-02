package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
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
import pl.kamjer.shoppinglistservice.service.CustomService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WebSocketService extends CustomService {

    private final WebSocketDataHolder webSocketDataHolder;
    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    public WebSocketService(UserRepository userRepository,
                            AmountTypeRepository amountTypeRepository,
                            CategoryRepository categoryRepository,
                            ShoppingItemRepository shoppingItemRepository,
                            WebSocketDataHolder webSocketDataHolder,
                            EntityManager entityManager) {
        super(userRepository);
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.webSocketDataHolder = webSocketDataHolder;
    }

    @Override
    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }

    @Transactional
    public AllDto synchronizeWebSocket(AllDto allDto) {
        //        saving time and getting user
        LocalDateTime savedTime = LocalDateTime.now();
        User user = getUserFromAuth();

//        getting date and time from user, if time from user is null take the oldest possible time
        LocalDateTime userSavedTime = Optional.ofNullable(allDto.getSavedTime()).orElseGet(() -> LocalDateTime.of(1000, 1, 1, 0, 0));
//        handling saving data from client
        List<AmountType> amountTypeToInsert = new ArrayList<>();
        Set<AmountType> amountTypeDtosFromClientProcessed = new HashSet<>();

        Map<Long, AmountType> existingAmountTypes = amountTypeRepository.findAllById(
                        allDto.getAmountTypeDtoList().stream()
                                .map(AmountTypeDto::getAmountTypeId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(AmountType::getAmountTypeId, Function.identity()));

        for (AmountTypeDto dto : allDto.getAmountTypeDtoList()) {
            log.log(Level.DEBUG, "Attempting to {} data: {}", dto.getModifyState(), dto.getAmountTypeId());
            AmountType newEntity = DatabaseUtil.toAmountType(user, dto, savedTime);
            switch (dto.getModifyState()) {
                case INSERT -> {
                    newEntity.setLocalId(dto.getLocalId());
                    amountTypeToInsert.add(newEntity);
                }
                case UPDATE -> {
                    Optional<AmountType> amountTypeToUpdateOptional = Optional.ofNullable(existingAmountTypes.get(dto.getAmountTypeId()));
                    if (amountTypeToUpdateOptional.isEmpty()) {
                        newEntity.setLocalId(dto.getLocalId());
                        amountTypeToInsert.add(newEntity);
                    } else {
                        AmountType amountTypeToUpdate = amountTypeToUpdateOptional.get();
                        amountTypeToUpdate.setTypeName(dto.getTypeName());
                        amountTypeToUpdate.setDeleted(dto.isDeleted());
                        amountTypeToUpdate.setSavedTime(savedTime);
                        amountTypeDtosFromClientProcessed.add(amountTypeToUpdate);
                    }
                }
                case DELETE -> {
                    Optional<AmountType> amountTypeToDeleteOptional = Optional.ofNullable(existingAmountTypes.get(dto.getAmountTypeId()));
                    if (amountTypeToDeleteOptional.isEmpty()) {
                        newEntity.setLocalId(dto.getLocalId());
                        amountTypeToInsert.add(newEntity);
                    } else {
                        AmountType amountTypeToDelete = amountTypeToDeleteOptional.get();
                        amountTypeToDelete.setDeleted(true);
                        amountTypeToDelete.setSavedTime(savedTime);
                        amountTypeDtosFromClientProcessed.add(amountTypeToDelete);
//                        deletes all items that are related to that amount type
                        List<ShoppingItem> shoppingItemsToDelete = shoppingItemRepository.findShoppingItemByUserUserNameAndItemAmountType(user.getUserName(), amountTypeToDeleteOptional.get());
                        shoppingItemsToDelete.forEach(shoppingItem -> shoppingItem.setDeleted(true));

                    }
                }
                case NONE ->
//                        adds entities for future reference
                        Optional.ofNullable(existingAmountTypes.get(dto.getAmountTypeId()))
                                .ifPresent(amountTypeDtosFromClientProcessed::add);
            }
        }
        amountTypeDtosFromClientProcessed.addAll(amountTypeRepository.saveAll(amountTypeToInsert));

        List<Category> categoriesToInsert = new ArrayList<>();
        Set<Category> categoryDtosFromClientProcessed = new HashSet<>();

        Map<Long, Category> existingCategories = categoryRepository.findAllById(
                        allDto.getCategoryDtoList().stream()
                                .map(CategoryDto::getCategoryId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        for (CategoryDto dto : allDto.getCategoryDtoList()) {
            log.log(Level.DEBUG, "Attempting to {} data: {}", dto.getModifyState(), dto.getCategoryId());
            Category newEntity = DatabaseUtil.toCategory(user, dto, savedTime);
            switch (dto.getModifyState()) {
                case INSERT -> {
                    newEntity.setLocalId(dto.getLocalId());
                    categoriesToInsert.add(newEntity);
                }
                case UPDATE -> {
                    Optional<Category> categoryToUpdateOptional = Optional.ofNullable(existingCategories.get(dto.getCategoryId()));
                    if (categoryToUpdateOptional.isEmpty()) {
                        newEntity.setLocalId(dto.getLocalId());
                        categoriesToInsert.add(newEntity);
                    } else {
                        Category categoryToUpdate = categoryToUpdateOptional.get();
                        categoryToUpdate.setCategoryName(dto.getCategoryName());
                        categoryToUpdate.setDeleted(dto.isDeleted());
                        categoryToUpdate.setSavedTime(savedTime);
                        categoryDtosFromClientProcessed.add(categoryToUpdate);
                    }
                }
                case DELETE -> {
                    Optional<Category> categoryToDeleteOptional = Optional.ofNullable(existingCategories.get(dto.getCategoryId()));
                    if (categoryToDeleteOptional.isEmpty()) {
                        newEntity.setLocalId(dto.getLocalId());
                        categoriesToInsert.add(newEntity);
                    } else {
                        Category categoryToDelete = categoryToDeleteOptional.get();
                        categoryToDelete.setDeleted(dto.isDeleted());
                        categoryToDelete.setSavedTime(savedTime);
                        categoryDtosFromClientProcessed.add(categoryToDelete);
                        List<ShoppingItem> shoppingItemsToDelete = shoppingItemRepository.findShoppingItemByUserUserNameAndItemCategory(user.getUserName(), categoryToDeleteOptional.get());
                        shoppingItemsToDelete.forEach(shoppingItem -> shoppingItem.setDeleted(true));
                    }
                }
                case NONE ->
//                        adds entities for future reference
                        Optional.ofNullable(existingCategories.get(dto.getCategoryId()))
                                .ifPresent(categoryDtosFromClientProcessed::add);

            }
        }

        categoryDtosFromClientProcessed.addAll(categoryRepository.saveAll(categoriesToInsert));

        List<ShoppingItem> shoppingItemToInsert = new ArrayList<>();
        Set<ShoppingItem> shoppingItemDtosFromClientProcessed = new HashSet<>();

        Map<Long, ShoppingItem> existingShoppingItems = shoppingItemRepository.findAllById(
                        allDto.getShoppingItemDtoList()
                                .stream()
                                .map(ShoppingItemDto::getShoppingItemId)
                                .toList())
                .stream()
                .collect(Collectors.toMap(ShoppingItem::getShoppingItemId, Function.identity()));

        for (ShoppingItemDto dto : allDto.getShoppingItemDtoList()) {
            log.log(Level.DEBUG, "Attempting to {} data: {}", dto.getModifyState(), dto.getShoppingItemId());
            ShoppingItem newEntity = DatabaseUtil.toShoppingItem(user, amountTypeRepository, categoryRepository, dto, savedTime);

            switch (dto.getModifyState()) {
                case INSERT -> {
                    newEntity.setLocalShoppingItemId(dto.getLocalId());
                    newEntity.setLocalCategoryId(dto.getLocalCategoryId());
                    newEntity.setLocalAmountTypeId(dto.getLocalAmountTypeId());
                    shoppingItemToInsert.add(newEntity);
                }
                case UPDATE -> {
//                    finding relevant data
                    Optional<ShoppingItem> shoppingItemOptional = Optional.ofNullable(existingShoppingItems.get(dto.getShoppingItemId()));
                    if (shoppingItemOptional.isEmpty()) {
                        newEntity.setLocalShoppingItemId(dto.getLocalId());
                        newEntity.setLocalCategoryId(dto.getLocalCategoryId());
                        newEntity.setLocalAmountTypeId(dto.getLocalAmountTypeId());
                        shoppingItemToInsert.add(newEntity);
                    } else {
                        ShoppingItem shoppingItemToUpdate = shoppingItemOptional.get();
                        AmountType amountTypeDb = amountTypeRepository.findAmountTypeByUserUserNameAndAmountTypeId(user.getUserName(), dto.getItemAmountTypeId())
                                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType:" + dto.getItemAmountTypeId()));
                        Category categoryDb = categoryRepository.findCategoryByUserUserNameAndCategoryId(user.getUserName(), dto.getItemCategoryId())
                                .orElseThrow(() -> new NoResourcesFoundException("no such Category:" + dto.getItemCategoryId()));

                        shoppingItemToUpdate.setItemAmountType(amountTypeDb);
                        shoppingItemToUpdate.setItemCategory(categoryDb);
                        shoppingItemToUpdate.setBought(dto.isBought());
                        shoppingItemToUpdate.setAmount(dto.getAmount());
                        shoppingItemToUpdate.setItemName(dto.getItemName());
                        shoppingItemToUpdate.setDeleted(dto.isDeleted());
                        shoppingItemToUpdate.setSavedTime(savedTime);
                        shoppingItemDtosFromClientProcessed.add(shoppingItemToUpdate);
                    }
                }
                case DELETE -> {
                    Optional<ShoppingItem> shoppingItemOptional = Optional.ofNullable(existingShoppingItems.get(dto.getShoppingItemId()));
                    if (shoppingItemOptional.isEmpty()) {
                        newEntity.setLocalShoppingItemId(dto.getLocalId());
                        newEntity.setLocalCategoryId(dto.getLocalCategoryId());
                        newEntity.setLocalAmountTypeId(dto.getLocalAmountTypeId());
                        shoppingItemToInsert.add(newEntity);
                    } else {
                        ShoppingItem shoppingItemToDelete = shoppingItemOptional.get();
                        shoppingItemToDelete.setDeleted(dto.isDeleted());
                        shoppingItemToDelete.setSavedTime(savedTime);
                        shoppingItemDtosFromClientProcessed.add(shoppingItemToDelete);
                    }
                }
                case NONE ->
//                        adds entities for future reference
                        Optional.ofNullable(existingShoppingItems.get(dto.getShoppingItemId()))
                                .ifPresent(shoppingItemDtosFromClientProcessed::add);
            }
        }
        shoppingItemDtosFromClientProcessed.addAll(shoppingItemRepository.saveAll(shoppingItemToInsert));

        //        data from database (data user does not have) it needs to be inserted, updated or deleted from local database, server needs to figure that out
        List<AmountType> amountTypesFromDb = amountTypeRepository.findAmountTypeByUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<Category> categoriesFromDb = categoryRepository.findCategoryByUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);
        List<ShoppingItem> shoppingItemsFromDb = shoppingItemRepository.findShoppingItemByUserUserNameAndSavedTimeAfter(user.getUserName(), userSavedTime);

        //        data after processing can be sent to a client
        List<AmountTypeDto> amountTypesFromDbProcessed = (amountTypesFromDb)
                .stream()
                .filter(amountType -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                    if (amountType.isDeleted()) {
                        return amountTypeDtosFromClientProcessed.contains(amountType);
                    }
                    return true;
                })
                .map(amountType -> {
                    ModifyState modifyState = ModifyState.INSERT;
//                    if entity flagged as deleted tell client to delete data
                    if (amountType.isDeleted()) {
                        modifyState = ModifyState.DELETE;
//                        if clients database contains that data but its timestamp happens later than the last
//                        contact client had with server it needs to be updated (data was updated)
                    } else if (amountTypeDtosFromClientProcessed.contains(amountType)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toAmountTypeDto(amountType, modifyState);
                })
                .toList();

        List<CategoryDto> categoriesFromDatabaseProcessed = (categoriesFromDb)
                .stream()
                .filter(category -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                    if (category.isDeleted()) {
                        return categoryDtosFromClientProcessed.contains(category);
                    }
                    return true;
                })
                .map(category -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (category.isDeleted()) {
                        modifyState = ModifyState.DELETE;
                    } else if (categoryDtosFromClientProcessed.contains(category)) {
                        modifyState = ModifyState.UPDATE;
                    }
                    return DatabaseUtil.toCategoryDto(category, modifyState);
                })
                .toList();

        List<ShoppingItemDto> shoppingItemsFromDataBaseProcessed = (shoppingItemsFromDb)
                .stream()
                .filter(shoppingItem -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                    if (shoppingItem.isDeleted()) {
                        return shoppingItemDtosFromClientProcessed.contains(shoppingItem);
                    }
                    return true;
                })
                .map(shoppingItem -> {
                    ModifyState modifyState = ModifyState.INSERT;
                    if (shoppingItem.isDeleted()) {
                        modifyState = ModifyState.DELETE;
                    } else if (shoppingItemDtosFromClientProcessed.contains(shoppingItem)) {
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
