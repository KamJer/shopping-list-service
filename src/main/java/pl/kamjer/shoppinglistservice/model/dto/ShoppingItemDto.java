package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShoppingItemDto implements Dto {

    private long shoppingItemId;
    private long itemAmountTypeId;
    private long itemCategoryId;
    @NotEmpty
    private String itemName;
    private Double amount;
    private boolean bought;
    private boolean deleted;
    @NotNull
    private ModifyState modifyState;

    private long localId;
    private long localAmountTypeId;
    protected long localCategoryId;
    private LocalDateTime savedTime;
}
