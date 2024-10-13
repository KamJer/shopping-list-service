package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;

@Getter
@Builder
public class ShoppingItemDto extends Dto {

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
