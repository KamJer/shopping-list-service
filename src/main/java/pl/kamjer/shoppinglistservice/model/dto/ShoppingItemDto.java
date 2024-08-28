package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;

@Getter
@Builder
public class ShoppingItemDto {

    private long shoppingItemId;
    private long itemAmountTypeId;
    private long itemCategoryId;
    @NotEmpty
    private String itemName;
    private Double amount;
    private boolean bought;
    private boolean deleted;
    private ModifyState modifyState;

    private long localId;
    private long localAmountTypeId;
    protected long localCategoryId;
}
