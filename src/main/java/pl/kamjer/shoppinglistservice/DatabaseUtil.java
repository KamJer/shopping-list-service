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
                .user(user)
                .typeName(amountTypeDto.getTypeName())
                .savedTime(savedTime)
                .deleted(amountTypeDto.isDeleted())
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

    public static User toUser(UserDto userDto) {
        return User.builder()
                .userName(userDto.getUserName())
                .password(userDto.getPassword())
                .savedTime(LocalDateTime.now())
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

    public static Category toCategory(User user, CategoryDto categoryDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return Category.builder()
                .categoryId(adjustId(categoryDto.getCategoryId()))
                .user(user)
                .categoryName(categoryDto.getCategoryName())
                .savedTime(savedTime)
                .deleted(categoryDto.isDeleted())
                .localId(categoryDto.getLocalId())
                .build();
    }

    /**
     * Converts 0 in id to nulls for hibernates
     * @param aLong
     * @return
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

    public static ShoppingItem toShoppingItem(User user, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository, ShoppingItemDto shoppingItemDto, LocalDateTime savedTime) throws NoResourcesFoundException {
        return ShoppingItem.builder()
                .shoppingItemId(adjustId(shoppingItemDto.getShoppingItemId()))
                .user(user)
                .itemAmountType(amountTypeRepository.findAmountTypeByUserUserNameAndAmountTypeId(user.getUserName(), shoppingItemDto.getItemAmountTypeId()).orElseThrow(() -> new NoResourcesFoundException("No such AmountType found")))
                .itemCategory(categoryRepository.findCategoryByUserUserNameAndCategoryId(user.getUserName(), shoppingItemDto.getItemCategoryId()).orElseThrow(() -> new NoResourcesFoundException("No such Category found")))
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
