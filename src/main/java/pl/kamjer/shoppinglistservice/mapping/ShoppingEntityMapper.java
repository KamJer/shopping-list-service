package pl.kamjer.shoppinglistservice.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = IdAdjuster.class)
public interface ShoppingEntityMapper {

    @Mapping(target = "amountTypeId", source = "dto.amountTypeId", qualifiedByName = "clientLongToId")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "savedTime", source = "savedTime")
    @Mapping(target = "typeName", source = "dto.typeName")
    @Mapping(target = "deleted", source = "dto.deleted")
    @Mapping(target = "localId", source = "dto.localId")
    @Mapping(target = "shoppingItemList", ignore = true)
    AmountType toAmountType(User user, AmountTypeDto dto, LocalDateTime savedTime);

    @Mapping(target = "categoryId", source = "dto.categoryId", qualifiedByName = "clientLongToId")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "savedTime", source = "savedTime")
    @Mapping(target = "categoryName", source = "dto.categoryName")
    @Mapping(target = "deleted", source = "dto.deleted")
    @Mapping(target = "localId", source = "dto.localId")
    @Mapping(target = "shoppingItemList", ignore = true)
    Category toCategory(User user, CategoryDto dto, LocalDateTime savedTime);

    @Mapping(target = "modifyState", source = "modifyState")
    AmountTypeDto toAmountTypeDto(AmountType entity, ModifyState modifyState);

    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "savedTime", source = "savedTime")
    AmountTypeDto toAmountTypeDto(AmountType entity, ModifyState modifyState, LocalDateTime savedTime);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "amountTypeId", source = "dto.amountTypeId")
    @Mapping(target = "typeName", source = "dto.typeName")
    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "localId", source = "dto.localId")
    AmountTypeDto copyAmountTypeDto(AmountTypeDto dto, ModifyState modifyState);

    @Mapping(target = "modifyState", source = "modifyState")
    CategoryDto toCategoryDto(Category entity, ModifyState modifyState);

    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "savedTime", source = "savedTime")
    CategoryDto toCategoryDto(Category entity, ModifyState modifyState, LocalDateTime savedTime);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "categoryId", source = "dto.categoryId")
    @Mapping(target = "categoryName", source = "dto.categoryName")
    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "localId", source = "dto.localId")
    CategoryDto copyCategoryDto(CategoryDto dto, ModifyState modifyState);

    UserDto toUserDto(User user);

    @Mapping(target = "itemAmountTypeId", source = "item.itemAmountType.amountTypeId")
    @Mapping(target = "itemCategoryId", source = "item.itemCategory.categoryId")
    @Mapping(target = "localId", source = "item.localShoppingItemId")
    @Mapping(target = "modifyState", source = "modifyState")
    ShoppingItemDto toShoppingItemDto(ShoppingItem item, ModifyState modifyState);

    @Mapping(target = "itemAmountTypeId", source = "item.itemAmountType.amountTypeId")
    @Mapping(target = "itemCategoryId", source = "item.itemCategory.categoryId")
    @Mapping(target = "localId", source = "item.localShoppingItemId")
    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "savedTime", source = "savedTime")
    ShoppingItemDto toShoppingItemDto(ShoppingItem item, ModifyState modifyState, LocalDateTime savedTime);

    @Mapping(target = "modifyState", source = "modifyState")
    @Mapping(target = "savedTime", source = "savedTime")
    ShoppingItemDto copyShoppingItemDto(ShoppingItemDto dto, ModifyState modifyState, LocalDateTime savedTime);

    @Mapping(target = "shoppingItemId", source = "dto.shoppingItemId", qualifiedByName = "clientLongToId")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "itemAmountType", source = "amountType")
    @Mapping(target = "itemCategory", source = "category")
    @Mapping(target = "itemName", source = "dto.itemName")
    @Mapping(target = "amount", source = "dto.amount")
    @Mapping(target = "bought", source = "dto.bought")
    @Mapping(target = "savedTime", source = "savedTime")
    @Mapping(target = "deleted", source = "dto.deleted")
    @Mapping(target = "localShoppingItemId", source = "dto.localId")
    @Mapping(target = "localAmountTypeId", source = "dto.localAmountTypeId")
    @Mapping(target = "localCategoryId", source = "dto.localCategoryId")
    ShoppingItem toShoppingItem(User user, ShoppingItemDto dto, LocalDateTime savedTime,
                                AmountType amountType, Category category);
}
