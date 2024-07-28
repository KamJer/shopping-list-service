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
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.logging.Level;

@Log
public class DatabaseUtil {

    public static AmountType toAmountType(UserRepository userRepository, AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        return AmountType.builder()
                .amountTypeId(AmountTypeId.builder()
                        .user(userRepository.findByUserName(amountTypeDto.getUserName()).orElseThrow(() -> new NoResourcesFoundException("No such User found")))
                        .amountTypeId(amountTypeDto.getAmountTypeId())
                        .build())
                .typeName(amountTypeDto.getTypeName())
                .build();
    }

    public static AmountTypeDto toAmountTypeDto(AmountType amountType) {
        return AmountTypeDto.builder()
                .userName(amountType.getAmountTypeId().getUser().getUserName())
                .amountTypeId(amountType.getAmountTypeId().getAmountTypeId())
                .typeName(amountType.getTypeName())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .userName(userDto.getUserName())
                .password(userDto.getPassword())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .userName(user.getUserName())
                .password(user.getPassword())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .userName(category.getCategoryId().getUser().getUserName())
                .categoryId(category.getCategoryId().getCategoryId())
                .categoryName(category.getCategoryName())
                .build();
    }

    public static Category toCategory(UserRepository userRepository, CategoryDto categoryDto) throws NoResourcesFoundException {
        return Category.builder()
                .categoryId(CategoryId.builder()
                        .user(userRepository.findByUserName(categoryDto.getUserName()).orElseThrow(() -> new NoResourcesFoundException("No such User found")))
                        .categoryId(categoryDto.getCategoryId())
                        .build())
                .categoryName(categoryDto.getCategoryName())
                .build();
    }

    public static ShoppingItemDto toShoppingItemDto(ShoppingItem shoppingItem) {
        return ShoppingItemDto.builder()
                .shoppingItemId(shoppingItem.getShoppingItemId().getShoppingItemId())
                .userName(shoppingItem.getShoppingItemId().getUser().getUserName())
                .itemAmountTypeId(shoppingItem.getItemAmountType().getAmountTypeId().getAmountTypeId())
                .itemCategoryId(shoppingItem.getItemCategory().getCategoryId().getCategoryId())
                .itemName(shoppingItem.getItemName())
                .amount(shoppingItem.getAmount())
                .bought(shoppingItem.isBought())
                .movedToBought(shoppingItem.isMovedToBought())
                .build();
    }

    public static ShoppingItem toShoppingItem(UserRepository userRepository, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository, ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        ShoppingItem shoppingItem = ShoppingItem.builder()
                .shoppingItemId(ShoppingItemId.builder()
                        .user(userRepository.findByUserName(shoppingItemDto.getUserName()).orElseThrow(() -> new NoResourcesFoundException("No such User found")))
                        .shoppingItemId(shoppingItemDto.getShoppingItemId())
                        .build())
                .itemAmountType(amountTypeRepository.findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(shoppingItemDto.getUserName(), shoppingItemDto.getItemAmountTypeId()).orElseThrow(() -> new NoResourcesFoundException("No such User found")))
                .itemCategory(categoryRepository.findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(shoppingItemDto.getUserName(), shoppingItemDto.getItemCategoryId()).orElseThrow(() -> new NoResourcesFoundException("No such User found")))
                .itemName(shoppingItemDto.getItemName())
                .amount(shoppingItemDto.getAmount())
                .bought(shoppingItemDto.isBought())
                .movedToBought(shoppingItemDto.isMovedToBought())
                .build();
        return shoppingItem;
    }
}
