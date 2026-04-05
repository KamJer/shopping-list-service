package pl.kamjer.shoppinglistservice.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShoppingItemResolver {

    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ShoppingEntityMapper shoppingEntityMapper;

    public ShoppingItem resolve(User user, Map<Long, AmountType> amountTypesByLocalId,
                                Map<Long, Category> categoriesByLocalId,
                                ShoppingItemDto dto,
                                LocalDateTime savedTime) {
        AmountType amountType = amountTypeRepository
                .findAmountTypeByUserNameAndAmountTypeId(user.getUserName(), dto.getItemAmountTypeId())
                .orElseGet(() -> amountTypesByLocalId.get(dto.getLocalAmountTypeId()));
        Category category = categoryRepository
                .findCategoryByUserNameAndCategoryId(user.getUserName(), dto.getItemCategoryId())
                .orElseGet(() -> categoriesByLocalId.get(dto.getLocalCategoryId()));
        if (amountType == null || category == null) {
            throw new NoResourcesFoundException("Amount type or category not found for shopping item");
        }
        return shoppingEntityMapper.toShoppingItem(user, dto, savedTime, amountType, category);
    }
}
