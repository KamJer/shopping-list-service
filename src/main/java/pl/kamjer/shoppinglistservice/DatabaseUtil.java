package pl.kamjer.shoppinglistservice;

import lombok.extern.slf4j.Slf4j;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;

import java.time.LocalDateTime;

@Slf4j
public class DatabaseUtil {

    public static AmountType toAmountType(User user,  AmountTypeDto amountTypeDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return AmountType.builder()
                .amountTypeId(adjustId(amountTypeDto.getAmountTypeId()))
                .userName(user.getUserName())
                .typeName(amountTypeDto.getTypeName())
                .savedTime(savedTime)
                .deleted(amountTypeDto.isDeleted())
                .localId(amountTypeDto.getLocalId())
                .build();
    }

    public static AmountTypeDto amountTypeDtoToAmountTypeDto(AmountTypeDto amountTypeDto, ModifyState modifyState) {
        return AmountTypeDto.builder()
                .amountTypeId(amountTypeDto.getAmountTypeId())
                .typeName(amountTypeDto.getTypeName())
                .modifyState(modifyState)
                .localId(amountTypeDto.getLocalId())
                .build();
    }

    public static AmountTypeDto toAmountTypeDto(AmountType amountType, ModifyState modifyState) {
        return AmountTypeDto.builder()
                .amountTypeId(amountType.getAmountTypeId())
                .typeName(amountType.getTypeName())
                .modifyState(modifyState)
                .localId(amountType.getLocalId())
                .build();
    }

    public static AmountTypeDto toAmountTypeDto(AmountType amountType, ModifyState modifyState, LocalDateTime savedTime) {
        return AmountTypeDto.builder()
                .amountTypeId(amountType.getAmountTypeId())
                .typeName(amountType.getTypeName())
                .modifyState(modifyState)
                .localId(amountType.getLocalId())
                .savedTime(savedTime)
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .userName(user.getUserName())
                .savedTime(user.getSavedTime())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category, ModifyState modifyState) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .modifyState(modifyState)
                .localId(category.getLocalId())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category, ModifyState modifyState, LocalDateTime savedTime) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .modifyState(modifyState)
                .localId(category.getLocalId())
                .savedTime(savedTime)
                .build();
    }

    public static CategoryDto categoryDtoToCategoryDto(CategoryDto categoryDto, ModifyState modifyState) {
        return CategoryDto.builder()
                .categoryId(categoryDto.getCategoryId())
                .categoryName(categoryDto.getCategoryName())
                .modifyState(modifyState)
                .localId(categoryDto.getLocalId())
                .build();
    }

    public static Category toCategory(User user, CategoryDto categoryDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return Category.builder()
                .categoryId(adjustId(categoryDto.getCategoryId()))
                .userName(user.getUserName())
                .categoryName(categoryDto.getCategoryName())
                .savedTime(savedTime)
                .deleted(categoryDto.isDeleted())
                .localId(categoryDto.getLocalId())
                .build();
    }

    /**
     * Converts 0 in id to nulls for hibernates
     */
    public static Long adjustId(Long aLong) {
        return (aLong != null && aLong > 0) ? aLong : null;
    }


    public static ShoppingItemDto toShoppingItemDto(ShoppingItem shoppingItem, ModifyState modifyState) {
        return ShoppingItemDto.builder()
                .shoppingItemId(shoppingItem.getShoppingItemId())
                .itemAmountTypeId(shoppingItem.getItemAmountType().getAmountTypeId())
                .itemCategoryId(shoppingItem.getItemCategory().getCategoryId())
                .itemName(shoppingItem.getItemName())
                .amount(shoppingItem.getAmount())
                .bought(shoppingItem.isBought())
                .modifyState(modifyState)
                .localId(shoppingItem.getLocalShoppingItemId())
                .localAmountTypeId(shoppingItem.getLocalAmountTypeId())
                .localCategoryId(shoppingItem.getLocalCategoryId())
                .build();
    }

    public static ShoppingItemDto toShoppingItemDto(ShoppingItem shoppingItem, ModifyState modifyState, LocalDateTime savedTime) {
        return ShoppingItemDto.builder()
                .shoppingItemId(shoppingItem.getShoppingItemId())
                .itemAmountTypeId(shoppingItem.getItemAmountType().getAmountTypeId())
                .itemCategoryId(shoppingItem.getItemCategory().getCategoryId())
                .itemName(shoppingItem.getItemName())
                .amount(shoppingItem.getAmount())
                .bought(shoppingItem.isBought())
                .modifyState(modifyState)
                .localId(shoppingItem.getLocalShoppingItemId())
                .localAmountTypeId(shoppingItem.getLocalAmountTypeId())
                .localCategoryId(shoppingItem.getLocalCategoryId())
                .savedTime(savedTime)
                .build();
    }

    public static ShoppingItemDto fromShoppingItemDtoToShoppingItemDto(ShoppingItemDto shoppingItemDto, ModifyState modifyState, LocalDateTime savedTime) {
        return ShoppingItemDto.builder()
                .shoppingItemId(shoppingItemDto.getShoppingItemId())
                .itemAmountTypeId(shoppingItemDto.getItemAmountTypeId())
                .itemCategoryId(shoppingItemDto.getItemCategoryId())
                .itemName(shoppingItemDto.getItemName())
                .amount(shoppingItemDto.getAmount())
                .bought(shoppingItemDto.isBought())
                .modifyState(modifyState)
                .localId(shoppingItemDto.getLocalId())
                .localAmountTypeId(shoppingItemDto.getLocalAmountTypeId())
                .localCategoryId(shoppingItemDto.getLocalCategoryId())
                .savedTime(savedTime)
                .build();
    }

    public static ShoppingItem toShoppingItem(User user, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository, ShoppingItemDto shoppingItemDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return ShoppingItem.builder()
                .shoppingItemId(adjustId(shoppingItemDto.getShoppingItemId()))
                .userName(user.getUserName())
                .itemAmountType(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId()).orElseThrow(() -> new NoResourcesFoundException("No such AmountType found: " + shoppingItemDto.getItemAmountTypeId())))
                .itemCategory(categoryRepository.findCategoryByUserNameAndCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId()).orElseThrow(() -> new NoResourcesFoundException("No such Category found: " + shoppingItemDto.getItemCategoryId())))
                .itemName(shoppingItemDto.getItemName())
                .amount(shoppingItemDto.getAmount())
                .bought(shoppingItemDto.isBought())
                .savedTime(savedTime)
                .deleted(shoppingItemDto.isDeleted())
                .localShoppingItemId(shoppingItemDto.getLocalId())
                .localAmountTypeId(shoppingItemDto.getLocalAmountTypeId())
                .localCategoryId(shoppingItemDto.getLocalCategoryId())
                .build();
    }
}
