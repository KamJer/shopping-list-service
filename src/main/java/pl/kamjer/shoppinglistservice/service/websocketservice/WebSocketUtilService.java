package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WebSocketUtilService extends WebsocketCustomService {

    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    public WebSocketUtilService(SecClient secClient,
                                AmountTypeRepository amountTypeRepository,
                                CategoryRepository categoryRepository,
                                ShoppingItemRepository shoppingItemRepository,
                                WebSocketDataHolder webSocketDataHolder) {
        super(webSocketDataHolder, secClient);
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
        this.shoppingItemRepository = shoppingItemRepository;
    }

    @Transactional
    public AllDto synchronizeWebSocket(AllDto allDto, String auth) {
//        saving time and getting user
        LocalDateTime savedTime = LocalDateTime.now();
        User user = getUserFromAuth();

//        getting date and time from user, if time from user is null take the oldest possible time
        LocalDateTime userSavedTime = Optional.ofNullable(allDto.getSavedTime()).orElseGet(() -> LocalDateTime.of(1000, 1, 1, 0, 0));

        if (user.getSavedTime().isAfter(userSavedTime) || !allDto.getAmountTypeDtoList().isEmpty() || !allDto.getCategoryDtoList().isEmpty() || !allDto.getShoppingItemDtoList().isEmpty()) {

            List<AmountType> amountTypesFromDb = amountTypeRepository.findByUserName(user.getUserName());
            List<Category> categoriesFromDb = categoryRepository.findByUserName(user.getUserName());
            List<ShoppingItem> shoppingItemsFromDb = shoppingItemRepository.findByUserName(user.getUserName());

//        list of entities that does exist on a connected device;
            Set<AmountType> amountTypesBeforeAndSend = new HashSet<>(allDto
                    .getAmountTypeDtoList()
                    .stream()
                    .map(amountTypeDto ->
                            DatabaseUtil.toAmountType(user, amountTypeDto, savedTime))
                    .toList());
//        list of entities that does not exist on a connected device (list of entities to insert and update)
            List<AmountType> amountTypesAfterAndSend = new ArrayList<>(amountTypesFromDb.stream()
                    .filter(amountType -> amountType.getSavedTime().isAfter(userSavedTime))
                    .toList());

            //        handling saving data from client
            List<AmountType> amountTypeToInsert = new ArrayList<>();

            Map<Long, AmountType> existingAmountTypes = amountTypesFromDb
                    .stream()
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
                            amountTypesBeforeAndSend.add(amountTypeToUpdate);
                            amountTypesAfterAndSend.add(amountTypeToUpdate);
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
                            amountTypesBeforeAndSend.add(amountTypeToDelete);
                            amountTypesAfterAndSend.add(amountTypeToDelete);

//                        deletes all items that are related to that amount type
                            List<ShoppingItem> shoppingItemsToDelete = shoppingItemsFromDb
                                    .stream()
                                    .filter(shoppingItem -> shoppingItem.getItemAmountType()
                                            .equals(amountTypeToDelete))
                                    .toList();
                            shoppingItemsToDelete.forEach(shoppingItem -> shoppingItem.setDeleted(true));
                        }
                    }
                }
            }
//        list of entities from client (this device)
            List<AmountType> amountTypes = amountTypeRepository.saveAll(amountTypeToInsert);
//        adding send data to the list
            amountTypesBeforeAndSend.addAll(amountTypes);
//        updated list of entities on server to send to client
            amountTypesAfterAndSend.addAll(amountTypes);

//        list of entities that does exist on a connected device;
            Set<Category> categoriesBeforeAndSend = new HashSet<>(allDto
                    .getCategoryDtoList()
                    .stream()
                    .map(categoryDto ->
                            DatabaseUtil.toCategory(user, categoryDto, savedTime))
                    .toList());
//        list of entities that does not exist on a connected device (list of entities to insert and update)
            List<Category> categoriesAfterAndSend = new ArrayList<>(categoriesFromDb
                    .stream()
                    .filter(category ->
                            category.getSavedTime().isAfter(userSavedTime))
                    .toList());

            List<Category> categoriesToInsert = new ArrayList<>();

            Map<Long, Category> existingCategories = categoriesFromDb
                    .stream()
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
                            categoriesAfterAndSend.add(categoryToUpdate);
                            categoriesBeforeAndSend.add(categoryToUpdate);
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
                            categoriesAfterAndSend.add(categoryToDelete);
                            categoriesBeforeAndSend.add(categoryToDelete);
                            List<ShoppingItem> shoppingItemsToDelete = shoppingItemsFromDb
                                    .stream()
                                    .filter(shoppingItem -> shoppingItem.getItemCategory().equals(categoryToDelete))
                                    .toList();
                            shoppingItemsToDelete.forEach(shoppingItem -> shoppingItem.setDeleted(true));
                        }
                    }
                }
            }
//        list of entities from client (this device)
            List<Category> categories = categoryRepository.saveAll(categoriesToInsert);
            categoriesBeforeAndSend.addAll(categories);
//        updated list of entities on server
            categoriesAfterAndSend.addAll(categories);

//        list of entities that does exist on a connected device;
            Set<ShoppingItem> shoppingItemsBeforeAndSend = new HashSet<>(allDto
                    .getShoppingItemDtoList()
                    .stream()
                    .map(shoppingItemDto ->
                            DatabaseUtil.toShoppingItem(user, amountTypeRepository, categoryRepository, shoppingItemDto, savedTime))
                    .toList());
//        list of entities that does not exist on a connected device (list of entities to insert and update)
            List<ShoppingItem> shoppingItemsAfterAndSend = new ArrayList<>(shoppingItemsFromDb
                    .stream()
                    .filter(shoppingItem -> shoppingItem.getSavedTime()
                            .isAfter(userSavedTime))
                    .toList());

            List<ShoppingItem> shoppingItemToInsert = new ArrayList<>();
            Map<Long, ShoppingItem> existingShoppingItems = shoppingItemsFromDb
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
//                        getting potential new amount types
                            AmountType amountTypeDb = Optional.ofNullable(existingAmountTypes.get(dto.getItemAmountTypeId()))
                                    .orElseThrow(() -> new NoResourcesFoundException("No such AmountType:" + dto.getItemAmountTypeId()));
                            Category categoryDb = Optional.ofNullable(existingCategories.get(dto.getItemCategoryId()))
                                    .orElseThrow(() -> new NoResourcesFoundException("no such Category:" + dto.getItemCategoryId()));

                            shoppingItemToUpdate.setItemAmountType(amountTypeDb);
                            shoppingItemToUpdate.setItemCategory(categoryDb);
                            shoppingItemToUpdate.setBought(dto.isBought());
                            shoppingItemToUpdate.setAmount(dto.getAmount());
                            shoppingItemToUpdate.setItemName(dto.getItemName());
                            shoppingItemToUpdate.setDeleted(dto.isDeleted());
                            shoppingItemToUpdate.setSavedTime(savedTime);
                            shoppingItemsBeforeAndSend.add(shoppingItemToUpdate);
                            shoppingItemsAfterAndSend.add(shoppingItemToUpdate);
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
                            shoppingItemsBeforeAndSend.add(shoppingItemToDelete);
                            shoppingItemsAfterAndSend.add(shoppingItemToDelete);
                        }
                    }
                }
            }
            List<ShoppingItem> shoppingItems = shoppingItemRepository.saveAll(shoppingItemToInsert);
            shoppingItemsBeforeAndSend.addAll(shoppingItems);
            shoppingItemsAfterAndSend.addAll(shoppingItems);
//        if last new data on server is newer than that on the device update it,

//        data after processing can be sent to a client
            List<AmountTypeDto> amountTypesFromDbProcessed = (amountTypesAfterAndSend)
                    .stream()
                    .filter(amountType -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                        if (amountType.isDeleted()) {
                            return amountTypesBeforeAndSend.contains(amountType);
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
                        } else if (amountTypesBeforeAndSend.contains(amountType)) {
                            modifyState = ModifyState.UPDATE;
                        }
                        return DatabaseUtil.toAmountTypeDto(amountType, modifyState);
                    })
                    .toList();

            List<CategoryDto> categoriesFromDatabaseProcessed = (categoriesAfterAndSend)
                    .stream()
                    .filter(category -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                        if (category.isDeleted()) {
                            return categoriesBeforeAndSend.contains(category);
                        }
                        return true;
                    })
                    .map(category -> {
                        ModifyState modifyState = ModifyState.INSERT;
                        if (category.isDeleted()) {
                            modifyState = ModifyState.DELETE;
                        } else if (categoriesBeforeAndSend.contains(category)) {
                            modifyState = ModifyState.UPDATE;
                        }
                        return DatabaseUtil.toCategoryDto(category, modifyState);
                    })
                    .toList();

            List<ShoppingItemDto> shoppingItemsFromDataBaseProcessed = (shoppingItemsAfterAndSend)
                    .stream()
                    .filter(shoppingItem -> {
//                    if entity is deleted check if it exists on a list from client, if it does exist it means
//                    client still has that entity, and it needs to be deleted, if client doesn't have that data
//                    it means it was already deleted and can be filtered, if it is not deleted return pass data further
                        if (shoppingItem.isDeleted()) {
                            return shoppingItemsBeforeAndSend.contains(shoppingItem);
                        }
                        return true;
                    })
                    .map(shoppingItem -> {
                        ModifyState modifyState = ModifyState.INSERT;
                        if (shoppingItem.isDeleted()) {
                            modifyState = ModifyState.DELETE;
                        } else if (shoppingItemsBeforeAndSend.contains(shoppingItem)) {
                            modifyState = ModifyState.UPDATE;
                        }
                        return DatabaseUtil.toShoppingItemDto(shoppingItem, modifyState);
                    })
                    .toList();

            user.setSavedTime(savedTime);
            secClient.putUser(DatabaseUtil.toUserDto(user), auth);

            return AllDto.builder()
                    .amountTypeDtoList(amountTypesFromDbProcessed)
                    .categoryDtoList(categoriesFromDatabaseProcessed)
                    .shoppingItemDtoList(shoppingItemsFromDataBaseProcessed)
                    .savedTime(savedTime)
                    .build();
        } else {
            return AllDto.builder()
                    .amountTypeDtoList(new ArrayList<>())
                    .categoryDtoList(new ArrayList<>())
                    .shoppingItemDtoList(new ArrayList<>())
                    .build();
        }
    }
}
