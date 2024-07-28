package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ShoppingItemId;
import pl.kamjer.shoppinglistservice.model.User;

@Getter
@Builder
public class ShoppingItemDto {

    private Long shoppingItemId;

    private String userName;

    private Long itemAmountTypeId;

    private Long itemCategoryId;

    private String itemName;

    private Double amount;

    private boolean bought;

    private boolean movedToBought;
}
