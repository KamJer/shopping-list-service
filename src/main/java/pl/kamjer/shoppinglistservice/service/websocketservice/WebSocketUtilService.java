package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapper;
import pl.kamjer.shoppinglistservice.mapping.ShoppingItemResolver;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.functional_interface.TriFunction;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WebSocketUtilService extends WebsocketCustomService {

    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingEntityMapper shoppingEntityMapper;
    private final ShoppingItemResolver shoppingItemResolver;

    public WebSocketUtilService(SecClient secClient,
                                AmountTypeRepository amountTypeRepository,
                                CategoryRepository categoryRepository,
                                ShoppingItemRepository shoppingItemRepository,
                                WebSocketDataHolder webSocketDataHolder,
                                ObjectMapper objectMapper,
                                ShoppingEntityMapper shoppingEntityMapper,
                                ShoppingItemResolver shoppingItemResolver) {
        super(webSocketDataHolder, secClient, objectMapper);
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.shoppingEntityMapper = shoppingEntityMapper;
        this.shoppingItemResolver = shoppingItemResolver;
    }

    @Transactional
    public AllDto synchronizeWebSocket(AllDto allDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = requireAuthenticatedUser();

        LocalDateTime userSavedTime = Optional.ofNullable(allDto.getSavedTime())
                .orElse(LocalDateTime.of(1000, 1, 1, 0, 0));

        List<AmountType> amountTypesFromDb = amountTypeRepository.findByUserName(user.getUserName());
        List<Category> categoriesFromDb = categoryRepository.findByUserName(user.getUserName());
        List<ShoppingItem> shoppingItemsFromDb = shoppingItemRepository.findByUserName(user.getUserName());

        Set<AmountType> clientAmountTypes = buildClientEntities(
                allDto.getAmountTypeDtoList(),
                user,
                shoppingEntityMapper::toAmountType
        );

        Set<Category> clientCategories = buildClientEntities(
                allDto.getCategoryDtoList(),
                user,
                shoppingEntityMapper::toCategory
        );

        Set<ShoppingItem> clientShoppingItems = buildClientEntities(
                allDto.getShoppingItemDtoList(),
                user,
                (u, dto, time) -> shoppingItemResolver.resolve(u, new HashMap<>(), new HashMap<>(), dto, time)
        );

        boolean dirty = isDirty(allDto.getAmountTypeDtoList(), amountTypesFromDb, user, shoppingEntityMapper::toAmountType)
                || isDirty(allDto.getCategoryDtoList(), categoriesFromDb, user, shoppingEntityMapper::toCategory)
                || isDirty(allDto.getShoppingItemDtoList(), shoppingItemsFromDb, user,
                (u, dto, time) -> shoppingItemResolver.resolve(u, new HashMap<>(), new HashMap<>(), dto, time));

        if (dirty) {
            return AllDto.builder()
                    .amountTypeDtoList(amountTypesFromDb.stream()
                            .map(a -> shoppingEntityMapper.toAmountTypeDto(a, ModifyState.INSERT)).toList())
                    .categoryDtoList(categoriesFromDb.stream()
                            .map(c -> shoppingEntityMapper.toCategoryDto(c, ModifyState.INSERT)).toList())
                    .shoppingItemDtoList(shoppingItemsFromDb.stream()
                            .map(s -> shoppingEntityMapper.toShoppingItemDto(s, ModifyState.INSERT)).toList())
                    .dirty(true)
                    .build();
        }

        syncEntities(allDto.getAmountTypeDtoList(), amountTypesFromDb, user, savedTime,
                amountTypeRepository::save, shoppingEntityMapper::toAmountType, AmountType::getAmountTypeId,
                AmountType::setSavedTime, AmountType::setDeleted);

        syncEntities(allDto.getCategoryDtoList(), categoriesFromDb, user, savedTime,
                categoryRepository::save, shoppingEntityMapper::toCategory, Category::getCategoryId,
                Category::setSavedTime, Category::setDeleted);

        Map<Long, AmountType> amountTypeMap = amountTypesFromDb.stream()
                .collect(Collectors.toMap(AmountType::getAmountTypeId, Function.identity()));
        Map<Long, Category> categoryMap = categoriesFromDb.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        syncEntities(allDto.getShoppingItemDtoList(), shoppingItemsFromDb, user, savedTime,
                shoppingItemRepository::save,
                (u, dto, time) -> shoppingItemResolver.resolve(u, amountTypeMap, categoryMap, dto, time),
                ShoppingItem::getShoppingItemId,
                ShoppingItem::setSavedTime,
                ShoppingItem::setDeleted);

        user.setSavedTime(savedTime);
        secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());

        return AllDto.builder()
                .amountTypeDtoList(processForClient(clientAmountTypes, amountTypesFromDb, userSavedTime, shoppingEntityMapper::toAmountTypeDto))
                .categoryDtoList(processForClient(clientCategories, categoriesFromDb, userSavedTime, shoppingEntityMapper::toCategoryDto))
                .shoppingItemDtoList(processForClient(clientShoppingItems, shoppingItemsFromDb, userSavedTime, shoppingEntityMapper::toShoppingItemDto))
                .savedTime(savedTime)
                .dirty(false)
                .build();
    }

    private <E, D> Set<E> buildClientEntities(List<D> dtos, User user,
                                              TriFunction<User, D, LocalDateTime, E> mapper) {
        return dtos.stream()
                .filter(dto -> ((Dto) dto).getModifyState() != ModifyState.INSERT)
                .map(dto -> mapper.apply(user, dto, ((Dto) dto).getSavedTime()))
                .collect(Collectors.toSet());
    }

    // --- DIRTY ---
    private <E, D> boolean isDirty(List<D> dtos, List<E> dbList, User user,
                                   TriFunction<User, D, LocalDateTime, E> mapper) {
        for (D dto : dtos) {
            Dto d = (Dto) dto;
            if (d.getModifyState() != ModifyState.INSERT &&
                    !dbList.contains(mapper.apply(user, dto, d.getSavedTime()))) {
                return true;
            }
        }
        return false;
    }

    <E, D, ID> void syncEntities(
            List<D> dtos,
            List<E> dbList,
            User user,
            LocalDateTime savedTime,
            Function<E, E> saveFunction,
            TriFunction<User, D, LocalDateTime, E> toEntityFunction,
            Function<E, ID> idGetter,
            BiConsumer<E, LocalDateTime> setSavedTime,
            BiConsumer<E, Boolean> setDeleted
    ) {
        Map<ID, E> existingMap = dbList.stream().collect(Collectors.toMap(idGetter, Function.identity()));
        List<E> toInsert = new ArrayList<>();

        for (D dto : dtos) {
            E entity = toEntityFunction.apply(user, dto, savedTime);
            ModifyState state = ((Dto) dto).getModifyState();

            if (state == null) {
                throw new IllegalArgumentException("Modify state can't be null");
            }

            switch (state) {
                case INSERT -> toInsert.add(entity);
                case UPDATE -> {
                    E existing = existingMap.get(idGetter.apply(entity));
                    if (existing == null) {
                        toInsert.add(entity);
                    } else {
                        copyProperties(entity, existing, savedTime);
                    }
                }
                case DELETE -> {
                    E existing = existingMap.get(idGetter.apply(entity));
                    if (existing != null) {
                        setDeleted.accept(existing, true);
                        setSavedTime.accept(existing, savedTime);
                    }
                }
            }
        }

        toInsert.forEach(saveFunction::apply);
    }

    private <E> void copyProperties(E source, E target, LocalDateTime savedTime) {
        if (target instanceof AmountType t && source instanceof AmountType s) {
            t.setTypeName(s.getTypeName());
            t.setDeleted(s.isDeleted());
            t.setSavedTime(savedTime);
        } else if (target instanceof Category t && source instanceof Category s) {
            t.setCategoryName(s.getCategoryName());
            t.setDeleted(s.isDeleted());
            t.setSavedTime(savedTime);
        } else if (target instanceof ShoppingItem t && source instanceof ShoppingItem s) {
            t.setItemName(s.getItemName());
            t.setBought(s.isBought());
            t.setAmount(s.getAmount());
            t.setItemCategory(s.getItemCategory());
            t.setItemAmountType(s.getItemAmountType());
            t.setDeleted(s.isDeleted());
            t.setSavedTime(savedTime);
        }
    }

    private <E extends ShoppingEntity, D> List<D> processForClient(
            Set<E> clientEntities,
            List<E> serverEntities,
            LocalDateTime userSavedTime,
            BiFunction<E, ModifyState, D> mapper) {

        return serverEntities.stream()
                .filter(e -> e.getSavedTime().isAfter(userSavedTime))
                .filter(e -> !e.isDeleted() || clientEntities.contains(e))
                .map(e -> {
                    ModifyState state;
                    if (e.isDeleted()) {
                        state = ModifyState.DELETE;
                    } else if (clientEntities.contains(e)) {
                        state = ModifyState.UPDATE;
                    } else {
                        state = ModifyState.INSERT;
                    }
                    return mapper.apply(e, state);
                })
                .toList();
    }
}
