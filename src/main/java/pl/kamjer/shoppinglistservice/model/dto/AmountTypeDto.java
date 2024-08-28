package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;

@Builder
@Getter
public class AmountTypeDto {

    private long amountTypeId;
    @NotEmpty
    private String typeName;
    private boolean deleted;
    private ModifyState modifyState;

    private long localId;

}
