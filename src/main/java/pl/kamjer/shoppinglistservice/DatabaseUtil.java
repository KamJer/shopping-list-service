package pl.kamjer.shoppinglistservice;

import lombok.extern.java.Log;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;

import java.time.LocalDateTime;

@Log
public class DatabaseUtil {

    public static AmountType toAmountType(User user,  AmountTypeDto amountTypeDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return AmountType.builder()
                .amountTypeId(AmountTypeId.builder()
                        .user(user)
                        .amountTypeId(amountTypeDto.getAmountTypeId())
                        .build())
                .typeName(amountTypeDto.getTypeName())
                .savedTime(savedTime)
                .deleted(amountTypeDto.isDeleted())
                .localAmountTypeId(amountTypeDto.getLocalAmountTypeId())
                .build();
    }

    public static AmountTypeDto toAmountTypeDto(AmountType amountType, ModifyState modifyState) {
        return AmountTypeDto.builder()
                .amountTypeId(amountType.getAmountTypeId().getAmountTypeId())
                .typeName(amountType.getTypeName())
                .modifyState(modifyState)
                .localAmountTypeId(amountType.getLocalAmountTypeId())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .userName(userDto.getUserName())
                .password(userDto.getPassword())
                .savedTime(LocalDateTime.now())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category, ModifyState modifyState) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId().getCategoryId())
                .categoryName(category.getCategoryName())
                .modifyState(modifyState)
                .localCategoryId(category.getLocalCategoryId())
                .build();
    }

    public static Category toCategory(User user, CategoryDto categoryDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return Category.builder()
                .categoryId(CategoryId.builder()
                        .user(user)
                        .categoryId(categoryDto.getCategoryId())
                        .build())
                .categoryName(categoryDto.getCategoryName())
                .savedTime(savedTime)
                .deleted(categoryDto.isDeleted())
                .localCategoryId(categoryDto.getLocalCategoryId())
                .build();
    }

    public static ShoppingItemDto toShoppingItemDto(ShoppingItem shoppingItem, ModifyState modifyState) {
        return ShoppingItemDto.builder()
                .shoppingItemId(shoppingItem.getShoppingItemId().getShoppingItemId())
                .itemAmountTypeId(shoppingItem.getItemAmountType().getAmountTypeId().getAmountTypeId())
                .itemCategoryId(shoppingItem.getItemCategory().getCategoryId().getCategoryId())
                .itemName(shoppingItem.getItemName())
                .amount(shoppingItem.getAmount())
                .bought(shoppingItem.isBought())
                .modifyState(modifyState)
                .localShoppingItemId(shoppingItem.getLocalShoppingItemId())
                .localAmountTypeId(shoppingItem.getLocalAmountTypeId())
                .localCategoryId(shoppingItem.getLocalCategoryId())
                .build();
    }

    public static ShoppingItem toShoppingItem(User user, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository, ShoppingItemDto shoppingItemDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return ShoppingItem.builder()
                .shoppingItemId(ShoppingItemId.builder()
                        .user(user)
                        .shoppingItemId(shoppingItemDto.getShoppingItemId())
                        .build())
                .itemAmountType(amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId()).orElseThrow(() -> new NoResourcesFoundException("No such AmountType found")))
                .itemCategory(categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId()).orElseThrow(() -> new NoResourcesFoundException("No such Category found")))
                .itemName(shoppingItemDto.getItemName())
                .amount(shoppingItemDto.getAmount())
                .bought(shoppingItemDto.isBought())
                .savedTime(savedTime)
                .deleted(shoppingItemDto.isDeleted())
                .localShoppingItemId(shoppingItemDto.getLocalShoppingItemId())
                .localAmountTypeId(shoppingItemDto.getLocalAmountTypeId())
                .localCategoryId(shoppingItemDto.getLocalCategoryId())
                .build();
    }
}
